/* JIMMY - Instant Mobile Messenger
 Copyright (C) 2006  JIMMY Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **********************************************************************
 File: jimmy/Account.java
 Version: pre-alpha  Date: 2006/05/21
 Author(s): Dejan Sakelsak
 */

/**
 * 
 */
package jimmy.icq;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Random;

//import java.io.*;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
//import jimmy.Jimmy;
//import jimmy.net.*;
import jimmy.util.*;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;

/**
 * @author dejan
 *
 */
public class ICQProtocol extends Protocol {
	
	private String AUTH_SERVER = "login.oscar.aol.com";
	private int AUTH_SERVER_PORT = 5190;
	//private final int AUTH_SERVER_PORT = 6666;
	private final String cli_id = new String("ICQ Inc. - Product of ICQ (TM).2003a.5.45.1.3777.85");
	private String bos = "";
	private int bos_port = 5190;
	private short f_seq = 0;
	private int s_seq = 1;
	private String[] groups;
	private short[] max_id;
	
	private long SSI_LAST_MODIFIED=0;
	private short SSI_ITEMS=0;
	private short REPORT_INTERVAL = 0;
	
	private Hashtable awaiting_auth = null;
	private byte[] cookie;
	private byte[] services;
	private byte[] service_ver;
	private byte[] capabilities = ICQLib.getCaps();
	private ICQPackage response;
	//private ICQPackage service_versions = null;
	private ICQConnector conn = null;
	
	private String user;
	private String pass;
	
	private boolean stop_;
	
	/**
	 * Creates a new instance of type ICQ with the interface to the core
	 * 
	 * @param jimmy interface to the core
	 */
	public ICQProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		this.status_ = DISCONNECTED;
		this.protocolType_ = ICQ;
		this.awaiting_auth = new Hashtable();
	}
	
	/**
	 * Logs in via the known account
	 */
	public boolean login(Account account) {
		
		this.account_ = account;
		this.user = account.getUser();
		this.pass = account.getPassword();
		
		String acc = account.getServer();
		int por = account.getPort();
		if(acc != null && !acc.equals(""))
			this.AUTH_SERVER = account.getServer();
		
		//TODO: Change login sequence to a more dinamic shape
		
		//STAGE ONE LOGIN (AUTH)
		//FIRST PART OF STAGE ONE
		//Generation of first login package
		ICQPackage l = ICQLib.getFirstLoginPackage(this.user, this.pass, this.roast(this.pass.getBytes()), this.cli_id, this.f_seq);
		
		//auth connection
		byte[] b = l.getNetPackage();
		this.conn = new ICQConnector(this.AUTH_SERVER,this.AUTH_SERVER_PORT);
		this.conn.connect();
		
		//check the connection acknowledgement
		if(this.conn.getNextPackage() == null){
			System.out.println("Error: not responding.");
			return false;
		}
		
		//Send the first auth package
		this.conn.sendPackage(b);
		
		this.response = new ICQPackage(this.conn.getNextPackage());
		
		//decode BOS TLV
		this.tlvDecode(this.response.getTlv(2));
		
		//decode cookie TLV
		this.tlvDecode(this.response.getTlv(3));
		
		this.conn.disconnect();
		
		System.out.println(this.bos);
		
		//END STAGE ONE
		
		//STAGE TWO
		ICQTlv t = null;
		l = new ICQPackage();
		byte[] ha = new byte[4];
		ha[3] = 0x01;
		l.setContent(ha);
		t = new ICQTlv();
		t.setContent(this.cookie);
		t.setHeader((short)0x06,(short)t.getCLen());
		l.addTlv(t);
		l.setChannel((byte)0x01);
		l.setFlap((short)1);
		ha=null;
		
		//Now change the connection server to recieved BOS address
		
		try {
			this.bos_port = Integer.parseInt(this.bos.substring(this.bos
					.indexOf(':') + 1, this.bos.length()));
		} catch (NumberFormatException e) {

			// Add exception forwarding
			System.out
					.println("[DEBUG] Server response changed or too many connection retries.");
			return false;
		}
		
		
		this.bos = this.bos.substring(0,this.bos.indexOf(":"));
		this.conn.setPort(this.bos_port);
		this.conn.setURL(this.bos);
		
		
		this.conn.connect();
		this.conn.sendPackage(l.getNetPackage());
		
//		System.out.println(Utils.byteArrayToHexString(this.conn.getNextPackage()));
		this.conn.getNextPackage();
//		System.out.println("recieving auth...");
		byte[] hahaha = this.conn.getNextPackage();
		ICQPackage in = new ICQPackage(hahaha);
		this.services = in.getServices();
		hahaha=null;
		in = null;
		this.service_ver = new byte[2*this.services.length];
		Random rng = new Random();
		
		int j = 0;
		for(int i = 0; i < this.service_ver.length-3; i=i+4){
			this.service_ver[i] = this.services[j];
			this.service_ver[i+1] = this.services[j+1];
			this.service_ver[i+2] = 0;
			this.service_ver[i+3] = (byte)Math.abs(rng.nextInt());
			j=j+2;
		}
		ICQPackage ver_req = new ICQPackage(this.service_ver.length,(byte)0x02);
		ver_req.setContent(this.service_ver);
		ver_req.setSnac(1,23,0,++this.s_seq);
		ver_req.setFlap(++this.f_seq);
		byte[] bla = ver_req.getNetPackage();
		this.conn.sendPackage(bla);
		
		b = this.conn.getNextPackage();

		if(b != null){
//			System.out.println("Service versions");
			in = new ICQPackage(b);
			this.pkgDecode(in);
		}else{
			return false;
		}
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);

		/***************************************************/
		/*	TODO or not: rate info negotiation			   */
		/* has to use those rates - for now overriden	   */
		/***************************************************/
		
		ICQPackage out = new ICQPackage();
		out.setSnac(1,6,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);

		//END STAGE TWO
		//STAGE THREE
		out = new ICQPackage();
		out.setSnac(2,2,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);
		
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);
		
		out = new ICQPackage();
		out.setSnac(19,4,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		//Contact list
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		//System.out.println(Utils.byteArrayToHexString(b));
		this.pkgDecode(in);
		
		
		out = new ICQPackage();
		out.setChannel((byte)0x02);
		byte[] c = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0B,(byte)0x1F,(byte)0x40,(byte)0x03,(byte)0xE7,(byte)0x03,(byte)0xE7,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		out.setContent(c);
		out.setSnac(4,2,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		c = null;
		
		//END STAGE THREE
		//STAGE FOUR

		
		out = new ICQPackage();
		byte[] used_families = ICQLib.getUsedFamilies();
		
		out.setChannel((byte)0x02);
		out.setContent(used_families);
		out.setSnac(1,2,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		//END STAGE FOUR
		used_families=null;
		
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		//System.out.println("Got packet\n"+Utils.byteArrayToHexString(b));
		this.pkgDecode(in);
		
		out = new ICQPackage();
		out.setSnac(1,30,0,++this.s_seq);
		t = new ICQTlv();
		byte[] h = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		t.setContent(h);
		t.setHeader((short)0x0006,(short)t.getCLen());
		out.addTlv(t);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		out = new ICQPackage();
		out.setChannel((byte)0x02);
		out.setContent(h);
		out.setSnac(1,17,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);

		//send contacts to UI
		this.jimmy_.addContacts(this.contacts_);
		
		//Set the connected status
		this.status_ = Protocol.CONNECTED;
		this.thread_.start();
		
		return true;
	}

	/**
	 * Logs out of this protocol
	 */
	public void logout() {
		
		this.stop_=true; //Kills the thread by stopping the main loop
		this.conn.disconnect(); //Disconnects from the server
		this.status_ = Protocol.DISCONNECTED; //Sets the disconnected state
	}

	/**
	 * Creates a new chat session
	 */
	public ChatSession startChatSession(Contact user) {
		ChatSession cs = new ChatSession(this);
		this.chatSessionList_.addElement(cs);
		
		cs.addContact(user);
		
		return cs;
	}

	/**
	 * Sends a message
	 */
	public void sendMsg(String msg, ChatSession session) {
		
		final byte[] MSG_TYPE = {(byte)0x00,(byte)0x01};
		Vector cl = session.getContactsList();
		ICQPackage ip = new ICQPackage();
		
		
		//TODO: message fragmentation
		// TODO: ICBM rates!
		
		byte[] time = Utils.longToBytes(System.currentTimeMillis(),true);

		//enable store if offline
		ICQTlv off = new ICQTlv();
		off.setHeader((short)6,(short)0);
		ICQTlv m = new ICQTlv();
		ByteOperator bo = new ByteOperator();
		bo.append((byte)5,(byte)1,(byte)0,(byte)1,(byte)1,(byte)1,(byte)1);
		bo.append(Utils.shortToBytes((short)(msg.length()+4),true));
		bo.append((byte)0,(byte)0,(byte)0xff,(byte)0xff);
		bo.append(msg.getBytes());
		m.setContent(bo.getBytes());
		m.setHeader((short)2,(short)m.getCLen());
		
		ICQTlv tri = new ICQTlv();
		tri.setHeader((short)3,(short)0);
		bo.clear();
		for(int i = 0; i < cl.size(); i++){
			ICQContact c = (ICQContact)cl.elementAt(i);
			byte[] uin = c.userID().getBytes();
			bo.append(time);
			bo.append(MSG_TYPE[0],MSG_TYPE[1],(byte)uin.length);
			bo.append(uin);
			ip.setChannel((byte)2);
			ip.setContent(bo.getBytes());
			ip.setSnac(4,6,0,++this.s_seq);
			ip.addTlv(m);
			ip.addTlv(tri);
			ip.addTlv(off);
			ip.setFlap(++this.f_seq);
			
			this.conn.sendPackage(ip.getNetPackage());
//			System.out.println(Utils.byteArrayToHexString(ip.getNetPackage()));
			bo.clear();
		}
		off = null;
		m = null;
		tri = null;
		time = null;
		ip = null;
		bo=null;
	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Decodes a tlv package
	 * 
	 * @param t TLV package
	 * @return true if successful
	 */
	public boolean tlvDecode(ICQTlv t){
		
		switch(t.getType()){
		case 0:
			break;
		case 1:
			break;
		case 2:
			break;
		case 3:
			break;
		case 4:
			break;
		case 5:
//			System.out.println("Got Cookie");
			this.bos = new String(t.getContent());
//			this.cookie = this.response.getTlv(2).getContent();
			break;
		case 6:
			this.cookie = t.getContent();
			break;
		case 7:
			break;
		case 8:
			System.out.print("Error: ");
			switch(Utils.bytesToInt(t.getContent(),true)){
				case 0x1E:
						System.out.println("Incorrect uin &/or password");
						this.jimmy_.stopProtocol(this);
					break;
			}
			//Drops auth error message
			break;
		default:
			break;
				
		}
		
		return true;
	}
	
	/**
	 * Decodes the package
	 *  
	 * @param pak the package to decode
	 * @return if can't decode returns false
	 */
	public boolean pkgDecode(ICQPackage pak){
		if(pak.getChannel() == (byte)0x02){
			int type = pak.getSnackType();
			int subtype = pak.getSnackSubType();
			switch(type){
			case 0x0001:
				switch(subtype){
				case 0x0001:
					return false;
				case 0x0007:
					//TODO: rate limit info classes
					byte[] c = pak.getContent();
					ICQPackage ack = new ICQPackage((int)(c[1]*2),(byte)0x02);
					byte[] n = new byte[(int)(c[1]*2)];
					for(byte i = 1;i <= c[1]; i++){
						n[2*i-1] = i;
					}
					ack.setSnac(1,8,0,pak.getSnackReqID());
					ack.setContent(n);
					ack.setFlap(++this.f_seq);
					this.conn.sendPackage(ack.getNetPackage());
					break;
				case 0x000F:
					break;
				case 0x0013:
					//TODO: MOTD
//					System.out.println("MOTD");
					break;
				case 0x0018:
					//TODO: Set the service version numbers
					//this.service_versions = pak;
//					System.out.println("decoding 18");
					break;
				}
				break;
			case 0x0002:
				switch(subtype){
				case 0x0001:
					return false;
				case 0x0003:
					//TODO: Location service limitations/capabilities
					ICQTlv cap = new ICQTlv();
					cap.setHeader((short)0x0005,(short)this.capabilities.length);
					cap.setContent(this.capabilities);
					ICQPackage cp = new ICQPackage();
					cp.setSnac(2,4,0,++this.s_seq);
					cp.addTlv(cap);
					cp.setFlap(++this.f_seq);
					this.conn.sendPackage(cp.getNetPackage());
					cap = null;
					cp = null;
					break;
				}
				break;
			case 0x0003:
				//Contact list actions
				switch(subtype){
				case 0x000A:
					//Contact refused to add him
					//TODO: remove contact from list
					break;
				case 0x000B:
					//user status changed OL
					byte[] data = pak.getContent();
					byte uin_l = data[0];
					byte[] uin = ByteOperator.slice(1,data,uin_l);
					short ntlvs = ByteOperator.bytesToShort(data[uin_l+3],data[uin_l+4]);
					int start_point = uin_l+5;
					Vector tlvs = new Vector();
					ICQTlv status = null;
					for(short i = 0; i < ntlvs; i++){
						ICQTlv t = new ICQTlv();
						short tid = ByteOperator.bytesToShort(data[start_point],data[start_point+1]);
						short len = ByteOperator.bytesToShort(data[start_point+2],data[start_point+3]);
						t.setHeader(tid,len);
						start_point += 4;
						t.setContent(ByteOperator.slice(start_point,data,start_point+len-1));
						start_point += len;
						if(tid == (short)0x0006)
							status = t;
						tlvs.addElement(t);
					}
					//jimmy status codes
					byte[] bi = status.getContent();
					int st = 0;
					if(bi[2] == (byte)0x00){
						switch(bi[3]){
						case 0x00:
							st = Contact.ST_ONLINE;
							break;
						case 0x01:
							st = Contact.ST_AWAY;
							break;
						case 0x02:
							st = Contact.ST_BUSY;
							break;
						case 0x03:
							st = Contact.ST_BUSY;
							break;
						case 0x04:
							st = Contact.ST_AWAY;
							break;
						case 0x05:
							st = Contact.ST_AWAY;
							break;
						case 0x06:
							st = Contact.ST_AWAY;
							break;
						case 0x07:
							st = Contact.ST_AWAY;
							break;
						case 0x10:
							st = Contact.ST_BUSY;
							break;
						case 0x20:
							st = Contact.ST_ONLINE;
							break;
						case 0x30:
							st = Contact.ST_BUSY;
							break;
						}
					}else{
						st = Contact.ST_OFFLINE;
					}
					//let's change the status to a specific contact
					for(int i = 0; i < this.contacts_.size(); i++){
						ICQContact c = (ICQContact)this.contacts_.elementAt(i);
						if(c.userID().equals(new String(uin))){
							c.setStatus(st);
							this.jimmy_.changeContactStatus(c);
							break;
						}
					}
					break;
				case 0x000C:
					//user status changed OFFL
					byte[] data1 = pak.getContent();
					byte[] uin1 = ByteOperator.slice(1,data1,data1[0]);
					for(int i = 0; i < this.contacts_.size(); i++){
						ICQContact c = (ICQContact)this.contacts_.elementAt(i);
						if(c.userID().equals(new String(uin1))){
							c.setStatus(Contact.ST_OFFLINE);
							this.jimmy_.changeContactStatus(c);
							break;
						}
					}
					break;
				}
				break;
			case 0x0004:
				switch(subtype){
				case 0x0001:
					return false;
				case 0x0007:
					byte[] p = pak.getContent();
					if(p[9] == (byte)0x01){
						byte[] uin = ByteOperator.slice(11,p,10+p[10]);
						int start_point = 11+(byte)p[10];
						
						byte[] l = null;
						start_point += 4;
						Vector tlvs = new Vector();
						ICQTlv msg = null;
						
						for(int i = 0; start_point < p.length; i++){
							
							short tlvid = ByteOperator.bytesToShort(p[start_point],p[start_point+1]);
							short tlen = ByteOperator.bytesToShort(p[start_point+2],p[start_point+3]);
							start_point += 4;
							
							l = ByteOperator.slice(start_point,p,start_point+tlen-1);
							start_point += tlen;
							ICQTlv bla = new ICQTlv();
							bla.setHeader(tlvid,tlen);
							bla.setContent(l);
							if(tlvid == (short)0x0002){
								msg = bla;
							}
							tlvs.addElement(bla);
						}
						byte[] c = msg.getContent();

						start_point = 12 + ByteOperator.bytesToShort(c[2],c[3]);
						int charset = start_point -2;
						int ml = c.length - start_point;
						byte[] d = ByteOperator.slice(start_point,c,start_point+ml-1);
						start_point += ml;
						//Support for multi encoding messages
						charset = ByteOperator.bytesToShort(c[charset],c[charset+1]);
						String m = null;
						String enc = null;
						try{
							if(charset == (int)0x0000){
								enc = "ISO-8859-1";
								m = new String(d,enc);
							}else if(charset == (int)0x0002){
								enc = "UTF-8";
								m = new String(d,0,d.length,enc);
							}
						}catch (UnsupportedEncodingException ex){
							ex.printStackTrace();
							System.out.println("Unsupported encoding: "+enc);
						}
						ICQContact cont = this.findContact(new String(uin));
						ChatSession csess = this.getChatSession(cont);
						this.jimmy_.msgRecieved(csess == null ? csess=this.startChatSession(cont) : csess ,cont,m);
//						System.out.print("Msg sent by UIN: "+new String(uin)+"\nMSG: \n");
//						System.out.println(m);
						//System.out.println("Msg bytes: "+Utils.byteArrayToHexString(m.getBytes()));
//						try{
//							System.out.println("Msg back in "+enc+" bytes: "+Utils.byteArrayToHexString(m.getBytes(enc)));
//					}catch (UnsupportedEncodingException ex){
//						ex.printStackTrace();
//						System.out.println("Unsupported encoding: "+enc);
//					}
					}else{
						System.out.println("Channel not supported");
					}
					break;
				case 0x000C:
//					System.out.println("Msg Ack");
					break;
				}
				break;
			case 0x000B:
				this.REPORT_INTERVAL = ByteOperator.bytesToShort(pak.getContent()[0],pak.getContent()[1]);
				break;
			case 0x0013:
				//SSI reply part
				switch(subtype){
				case 0x0001:
					return false;
				case 0x0006://ROSTER reply
					System.out.println(Utils.byteArrayToHexString(pak.getNetPackage()));
					byte[] pkg = pak.getContent();
					
					int start_point = 1;
					
					//byte[] it = new byte[2];
					short items = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]); 
					this.SSI_ITEMS = items;
					//it=null;
					start_point += 2;
					
					this.groups = new String[items+10];
					this.max_id = new short[items+10];
					this.contacts_ = new Vector();
					for(short i = 0; i < items; i++){
						int item_name_len = 0;
						item_name_len = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						
						byte[] item_name = ByteOperator.slice(start_point,pkg,start_point+item_name_len-1);
						start_point += item_name_len;
						int gid = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						int iid = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						int itid = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						int data_len = ByteOperator.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						
						byte[] data = ByteOperator.slice(start_point,pkg,start_point+data_len-1);
						start_point += data_len;
						
						/*System.out.println("item:");
						System.out.print("#items:");
						System.out.println(items);
						System.out.print("Item name len: ");
						System.out.println(item_name_len);
						System.out.print("Item name: ");
						System.out.println(new String(item_name));
						System.out.print("GID: ");
						System.out.println(gid);
						System.out.print("IID: ");
						System.out.println(iid);
						System.out.print("ITID:");
						System.out.println(itid);
						System.out.print("Data len:");
						System.out.println(data_len);
						System.out.print("Data:");
						System.out.println(Utils.byteArrayToHexString(data));
						*/
						
						//here we can check the groups that can see you but on the GSM...
						if(itid == 1){ //if group
							this.groups[gid] = new String(item_name);
						}else if(itid == 0){ //if buddy 
							//if the id of the maximum id of the group is lower then this contacts one change it to this
							if(this.max_id[gid] > iid)
								this.max_id[gid] = (short)iid;
							ICQContact ct = new ICQContact(new String(item_name),this);
							if(data[0] == (byte)0x01 && data[1] == (byte)0x31){
								int l = ByteOperator.bytesToShort(data[2],data[3]);
								byte[] nick = ByteOperator.slice(4,data,3+l);
//								System.out.println(new String(item_name)+" : "+new String(nick)
//										+" in group: "+ groups[gid]);
								ct.setScreenName(new String(nick));
								ct.setIcqID((short)iid);
								ct.setGroupName(this.groups[gid]);
								ct.setIcqGID((short)gid);
								ct.setProtocol(this);
								ct.setStatus(Contact.ST_OFFLINE);
								this.contacts_.addElement(ct);
							}
							
						}
					}
//					it = null;
					
					ByteOperator bo = new ByteOperator();
					bo.append(pkg[pkg.length-4],pkg[pkg.length-3],pkg[pkg.length-2],pkg[pkg.length-1]);
					this.SSI_LAST_MODIFIED = Utils.bytesToLong(bo.getBytes(),true);
					
					pkg = null;
					break;
				case 0x000e:
					//SSI ack
					//System.out.println("Srv ack:\n"+Utils.byteArrayToHexString(pak.getNetPackage()));
					if(this.awaiting_auth.containsKey(new Integer(pak.getSnackReqID()))){
						ICQPackage out = (ICQPackage)this.awaiting_auth.remove(new Integer(pak.getSnackReqID()));
						out.setFlap(++this.f_seq);
						this.conn.sendPackage(out.getNetPackage());
						//System.out.println(out.getNetPackage());
					}
					break;
				case 0x0019:
					//Contact auth request
					break;
				case 0x001B:
					//Contact auth reply
					break;
				case 0x001C:
					//"You were added"
					byte[] c = pak.getContent();
					int st_point = 2+ByteOperator.bytesToShort(c[0],c[1]);
					byte[] sv = ByteOperator.slice(st_point+1,c,st_point+c[st_point]);
					/*new byte[c[st_point]];
					st_point++;
					for(int i = 0; i<sv.length; i++){
						sv[i] = c[st_point];
						st_point++;
					}*/
					System.out.println("You were added by: "+new String(sv));
					
					if(this.findContact(new String(sv)) == null){
						//Temporary fix
						this.addContact(new Contact(new String(sv),this));
					}
					
					break;
				}
				break;
			case 0x0015:
				switch(subtype){
				case 0x0003:
					//service negotiations
					//OFFL messages & stuff...
					break;
				}
				break;
			}
		
		}
		
		return true;
	}
	
	/**
	 * Byte array roaster. XOR the byte[] with the modulo bytes in the roasting array. 
	 * 
	 * @param a byte array to be roasted (ex. password)
	 * @return roasted byte[]
	 */
	public byte[] roast(byte[] a){
		byte[] ra = {(byte)0xF3, (byte)0x26, (byte)0x81, (byte)0xC4, (byte)0x39, (byte)0x86, (byte)0xDB, (byte)0x92, (byte)0x71, (byte)0xA3, (byte)0xB9, (byte)0xE6, (byte)0x53, (byte)0x7A, (byte)0x95, (byte)0x7C};
		byte[] roasted = new byte[a.length];
		for(int i = 0; i < a.length; i++){
			roasted[i] = (byte)(a[i] ^ ra[i%ra.length]);
		}
		return roasted;
	}

	public String getServer(){return this.AUTH_SERVER;}
	
	public ICQContact findContact(String uID){
		ICQContact c = null;
		for(int i = 0; i < this.contacts_.size(); i++){
			c = (ICQContact)this.contacts_.elementAt(i);
			if(c.userID().equals(uID)){
				return c;
			}
		}
		return null;
	}
	
	public void run() {
		
//		System.out.println("Running...");
		
		ICQPackage out = null;
		ICQTlv t = null;
//		Offline messages req
		//TODO: enable in V1.1
		/*out = new ICQPackage();
		out.setSnac(21,2,0,++this.s_seq);
		t = new ICQTlv();
		b = new byte[10];
		b[0] = (byte)0x08;
		b[1] = (byte)0x00;
		h = Utils.intToBytes(Integer.parseInt(this.user),false);
		b[2] = h[0];
		b[3] = h[1];
		b[4] = h[2];
		b[5] = h[3];
		//request id
		b[6] = (byte)0x3c;
		b[7] = (byte)0x00;
		h = Utils.shortToBytes((short)this.s_seq,false);
		b[8] = h[0];
		b[9] = h[1];
		t.setContent(b);
		t.setHeader((short)1,(short)10);
		out.addTlv(t);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());*/

		//User info permissions
		ByteOperator bo = new ByteOperator();
		out = new ICQPackage();
		out.setSnac(21,2,0,++this.s_seq);
		t = new ICQTlv();
		bo.append((byte)0x0E,(byte)0x00);
		bo.append(Utils.intToBytes(Integer.parseInt(this.user),false));
		//request id
		bo.append((byte)0xd0,(byte)0x07);
		bo.append(Utils.shortToBytes((short)this.s_seq,false));
		bo.append((byte)0x24,(byte)0x04,(byte)0x01,(byte)0x00,(byte)0x01,(byte)0x00);
		t.setContent(bo.getBytes());
		t.setHeader((short)1,(short)10);
		out.addTlv(t);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		//rquest buddy statuses
		out = new ICQPackage();
		out.setSnac(19,7,0,++this.s_seq);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		
		this.stop_ = false;
		while (!this.stop_) {		
			ICQPackage in = null;
			byte[] next = null;
			while(true){
				next = this.conn.getNextPackage();
				if(next != null){

					in = new ICQPackage(next);
//					System.out.println("I GOT:\n"+Utils.byteArrayToHexString(next));
					if(!this.pkgDecode(in)){
						
						//Some kind of error report
						
					}
				}
				if(this.conn.getNumPackages() == 0)
					break;
			}
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns the GID of @param g
	 * 
	 * @param g group
	 * @return GID
	 */
    protected short groupID(String g){
    		for(short i = 0; i < this.groups.length; i++){
    			if(this.groups[i] != null){
    				if(this.groups[i].equals(g))
    					return i;
    			}
    		}
    		return -1;
    }
    
    /**
     * It gets a free GID, if not possible, then extends the group array
     * 
     * @return free GID
     */
    protected short getFreeGID(){
    		for(short i = 0; i < this.groups.length; i++)
    			if(this.groups == null)
    				return i;
    		String[] grp = new String[this.groups.length+10];
    		short[] mid = new short[this.groups.length+10];
    		for(int i = 0; i < this.groups.length;i++){
    			grp[i] = this.groups[i];
    			mid[i] = this.max_id[i];
    		}
    		short id = (short)this.groups.length;
    		this.groups = grp;
    		this.max_id = mid;
    		return id;
    }
    
    public void addContact(Contact c) {
		
    	short gid = 0;
		int tmp_s = 0;
    	
    	// TODO: Send snack 0x0013,0x0008
		//let's set the protocol instance to which we are related
    	c.setProtocol(this);
		
    	//set the default screen name (nickname) to the userID (username) if not set
		ICQContact ic = new ICQContact(c);
		if (ic.screenName() == null) {
			ic.setScreenName(ic.userID());
		}
		
		ByteOperator bo = new ByteOperator();
		if ((gid = this.groupID(ic.groupName())) == -1) {

			// temporarly fix TODO: fix :) 
			//if no group selected find the first one that exists in the list
			if (ic.groupName() == null) {
				for (int i = 1; i < this.groups.length; i++)
					if (this.groups[i] != null) {
						ic.setGroupName(this.groups[i]);
						break;
					}
			}
			
			// Default settings
			//If the group doesn't exist create a new one
			gid = this.getFreeGID();
			this.groups[gid] = ic.groupName();
			this.max_id[gid] = (short) 0x01;
			ic.setIcqGID(gid);
			ic.setIcqID(this.max_id[gid]);
			this.contacts_.addElement(ic);

			//set group name len
			bo.append(Utils.shortToBytes((short) ic.groupName().length(),true));

			//set group name
			byte[] name = ic.groupName().getBytes();
			bo.append(name);
			//set GID
			bo.append(Utils.shortToBytes(gid, true));
			//set bID
			//set type (0x0001 = group)
			bo.append((byte) 0, (byte) 0, (byte) 0, (byte) 1);
			//set group members tlv len -> first time created so 6 bytes
			bo.append((byte) 0, (byte) 6, (byte) 0, (byte) 0xc8, (byte) 0,
					(byte) 2, (byte) 0, (byte) 1);

			//set id len
			bo.append(Utils.shortToBytes((short) ic.userID().length(), true));
			bo.append(ic.userID().getBytes());
			bo.append(Utils.shortToBytes(gid, true));
			bo.append((byte) 0, (byte) 1, (byte) 0, (byte) 0);
			bo.append(Utils.shortToBytes(
					(short) (4 + ic.screenName().length()), true));
			bo.append((byte) 1, (byte) 0x31);
			bo.append(Utils
					.shortToBytes((short) ic.screenName().length(), true));
			bo.append(ic.screenName().getBytes());

			ICQPackage ssi = new ICQPackage();
			ssi.setChannel((byte) 0x02);
			ssi.setContent(bo.getBytes());
			ssi.setSnac(19, 8, 0, ++this.s_seq);
			ssi.setFlap(++this.f_seq);

			tmp_s = this.s_seq;

			ICQPackage addEnd = new ICQPackage();
			addEnd.setChannel((byte) 0x02);
			addEnd.setSnac(19, 18, 0, ++this.s_seq);
			addEnd.setFlap(++this.f_seq);

			this.awaiting_auth.put(new Integer(tmp_s), addEnd);
			System.out.println(Utils.byteArrayToHexString(ssi.getNetPackage()));
			this.conn.sendPackage(ssi.getNetPackage());

			//System.out.println(Utils.byteArrayToHexString(ssi.getNetPackage()));

		} else {
			//TODO: send add buddy and modify buddy using the awaiting auth hashtable
			this.groups[gid] = ic.groupName();
			this.max_id[gid] = (short) 0x01;
			ic.setIcqGID(gid);
			ic.setIcqID(++this.max_id[gid]);
			this.contacts_.addElement(ic);

			bo.append(Utils.shortToBytes((short) ic.userID().length(), true));
			bo.append(ic.userID().getBytes());
			bo.append(Utils.shortToBytes(gid, true));
			bo.append(Utils.shortToBytes(this.max_id[gid], true));
			bo.append((byte) 0, (byte) 0);
			bo.append(Utils.shortToBytes(
					(short) (4 + ic.screenName().length()), true));
			bo.append((byte) 0x01, (byte) 0x31);
			bo.append(Utils
					.shortToBytes((short) ic.screenName().length(), true));
			bo.append(ic.screenName().getBytes());
			ICQPackage buddy = new ICQPackage();
			buddy.setChannel((byte) 0x02);
			buddy.setContent(bo.getBytes());

			bo.clear();

			bo.append(Utils.shortToBytes((short) ic.groupName().length(), true));
			bo.append(ic.groupName().getBytes());
			bo.append(Utils.shortToBytes(gid, true));
			bo.append((byte) 0, (byte) 0, (byte) 0, (byte) 1);
			bo.append(Utils.shortToBytes((short) (4 + this.max_id[gid] * 2), true));
			bo.append((byte) 0x00, (byte) 0xc8);
			for (short i = 1; i <= this.max_id[gid]; i++) {
				bo.append(Utils.shortToBytes(i, true));
			}

			ICQPackage grp = new ICQPackage();
			grp.setChannel((byte) 0x02);
			grp.setContent(bo.getBytes());

			buddy.setSnac(19, 8, 0, ++this.s_seq);
			buddy.setFlap(++this.f_seq);
			tmp_s = this.s_seq;
			grp.setSnac(19, 9, 0, ++this.s_seq);

			this.awaiting_auth.put(new Integer(tmp_s), grp);

			tmp_s = this.s_seq;

			ICQPackage addEnd = new ICQPackage();
			addEnd.setChannel((byte) 0x02);
			addEnd.setSnac(19, 18, 0, ++this.s_seq);
			addEnd.setFlap(++this.f_seq);

			this.awaiting_auth.put(new Integer(tmp_s), addEnd);
			System.out.println(Utils.byteArrayToHexString(buddy.getNetPackage()));
			this.conn.sendPackage(buddy.getNetPackage());
		}

	}

	public void updateContactProperties(Contact c) {
		
	}

	public boolean removeContact(Contact c) {
		// TODO: Send snack 0x0013, 0x000a
		return false;
	}

	
}
