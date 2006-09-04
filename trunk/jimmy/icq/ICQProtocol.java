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
	//private final String AUTH_SERVER = "192.168.0.4";
	private int AUTH_SERVER_PORT = 5190;
	//private final int AUTH_SERVER_PORT = 6666;
	//private final String AIM_MD5_STRING = "AOL Instant Messenger (SM)";
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
	private byte[] capabilities = {(byte)0x09,(byte)0x46,(byte)0x00,(byte)0x00,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00  //unknown capability - copied from gaim
			,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4D,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00						//cross ICQ - AIM messaging
			,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4E,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00};
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
		
		if(por != 0)
			this.AUTH_SERVER_PORT = account.getPort();
		
		if(this.login(this.user,this.pass))
			return true;
		
		return false;
	}

	public boolean login(String username, String passwd) {
		this.user = username;
		this.pass = passwd;
		
		//TODO: Change login sequence to a more dinamic shape
		
		//STAGE ONE LOGIN (AUTH)
		//FIRST PART OF STAGE ONE
		//Generation of first login package
		ICQPackage l = new ICQPackage();
		l.setChannel((byte)0x01);
		byte[] ver = {(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x01};
		l.setContent(ver);
		
		ICQTlv t = new ICQTlv();
		t.setHeader((short)0x0001,(short)(this.user.getBytes()).length);
		t.setContent(this.user.getBytes());
		l.addTlv(t);
		t = new ICQTlv();
		t.setHeader((short)0x0002,(short)(this.pass.getBytes()).length);
		t.setContent(this.roast(this.pass.getBytes()));
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0003,(short)(this.cli_id.getBytes()).length);
		t.setContent(this.cli_id.getBytes());
		l.addTlv(t);
		
		ver = null;
		ver = new byte[2];
		t = new ICQTlv();
		ver[0] = (byte)0x01;
		ver[1] = (byte)0x0a;
		t.setHeader((short)0x0016,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		ver = null;
		ver = new byte[2];
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x05;
		t.setHeader((short)0x0017,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		ver = null;
		ver = new byte[2];
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x2d;
		t.setHeader((short)0x0018,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		ver = null;
		ver = new byte[2];
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x01;
		t.setHeader((short)0x0019,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		ver = null;
		ver = new byte[2];
		t = new ICQTlv();
		ver[0] = (byte)0x0e;
		ver[1] = (byte)0xc1;
		t.setHeader((short)0x001A,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver = new byte[4];
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x00;
		ver[2] = (byte)0x00;
		ver[3] = (byte)0x55;
		t.setHeader((short)0x0014,(short)0x0004);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x000f,(short)0x0002);
		t.setContent("en");
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x000e,(short)0x0002);
		t.setContent("us");
		l.addTlv(t);
		l.setFlap(++this.f_seq);
		l.setFlapSize(l.getSize()-ICQPackage.FLAP_HEADER_SIZE);
		
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
		
		//get cookie or err
		this.response = new ICQPackage(this.conn.getNextPackage());
		this.tlvDecode(this.response.getTlv(1));
		this.conn.disconnect();
//		System.out.println(this.bos);
		//END STAGE ONE
		
		//STAGE TWO
		
		l = new ICQPackage();
		byte[] ha = new byte[4];
		ha[0] = 0x00;
		ha[1] = 0x00;
		ha[2] = 0x00;
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
		this.bos_port = Integer.parseInt(this.bos.substring(this.bos.indexOf(':')+1,this.bos.length()));
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
		byte[] used_families ={
				(byte)0x00,(byte)0x15,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x04,(byte)0x7C,
				(byte)0x00,(byte)0x13,(byte)0x00,(byte)0x04,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29, 
				(byte)0x00,(byte)0x0C,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x01,
				(byte)0x00,(byte)0x0B,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x01,
				(byte)0x00,(byte)0x0A,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x09,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x04,(byte)0x00,(byte)0x01,
				(byte)0x00,(byte)0x07,(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x06,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x03,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x02,(byte)0x00,(byte)0x01,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29,
				(byte)0x00,(byte)0x01,(byte)0x00,(byte)0x03,(byte)0x01,(byte)0x10,(byte)0x06,(byte)0x29
				};
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
//		int pieces = 1;
		
		/*if(msg.length() >= 450){
			// TODO: ICBM rates!
		}else{
			
		}*/
		
		byte[] time = Utils.longToBytes(System.currentTimeMillis(),true);

		//enable store if offline
		ICQTlv off = new ICQTlv();
		off.setHeader((short)6,(short)0);
		ICQTlv m = new ICQTlv();
		byte[] stuff = new byte[msg.length()+13];
		stuff[0] = (byte)5;
		stuff[1] = (byte)1;
		stuff[2] = (byte)0;
		stuff[3] = (byte)1;
		stuff[4] = (byte)1;
		stuff[5] = (byte)1;
		stuff[6] = (byte)1;
		byte[] b = Utils.shortToBytes((short)(msg.length()+4),true);
		stuff[7] = b[0];
		stuff[8] = b[1];
		stuff[9] = (byte)0;
		stuff[10] = (byte)0;
		stuff[11] = (byte)0xff;
		stuff[12] = (byte)0xff;
		b = msg.getBytes();
		for(int j = 0; j < msg.length(); j++){
			stuff[j+13] = b[j];
		}
		m.setContent(stuff);
		m.setHeader((short)2,(short)m.getCLen());
		
		ICQTlv tri = new ICQTlv();
		tri.setHeader((short)3,(short)0);
		
		for(int i = 0; i < cl.size(); i++){
			ICQContact c = (ICQContact)cl.elementAt(i);
			byte[] uin = c.userID().getBytes();
			byte[] pak = new byte[11 + uin.length];
			for(int j = 0; j < time.length; j++){
				pak[j] = time[j];
			}
			pak[time.length] = MSG_TYPE[0];
			pak[time.length+1] = MSG_TYPE[1];
			pak[time.length+2] = (byte)uin.length;
			for(int j=time.length+3; j < pak.length; j++){
				pak[j] = uin[j-(time.length+3)];
			}
			ip.setChannel((byte)2);
			ip.setContent(pak);
			ip.setSnac(4,6,0,++this.s_seq);
			ip.addTlv(m);
			ip.addTlv(tri);
			ip.addTlv(off);
			ip.setFlap(++this.f_seq);
			
			this.conn.sendPackage(ip.getNetPackage());
//			System.out.println(Utils.byteArrayToHexString(ip.getNetPackage()));
		}
		off = null;
		m = null;
		tri = null;
		time = null;
		ip = null;
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
		case	 0:
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
			this.cookie = this.response.getTlv(2).getContent();
			break;
		case 6:
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
					byte[] uin = new byte[uin_l];
					for(byte i = 0; i < uin_l; i++){
						uin[i] = data[i+1];
					}
					byte[] l = new byte[2];
					short ntlvs = this.bytesToShort(data[uin_l+3],data[uin_l+4]);
					int start_point = uin_l+5;
					Vector tlvs = new Vector();
					ICQTlv status = null;
					for(short i = 0; i < ntlvs; i++){
						l = new byte[2];
						ICQTlv t = new ICQTlv();
						short tid = this.bytesToShort(data[start_point],data[start_point+1]);
						start_point +=2;
						short len = this.bytesToShort(data[start_point],data[start_point+1]);
						start_point +=2;
						t.setHeader(tid,len);
						l = new byte[len];
						for(short j = 0; j < len; j++){
							l[j] = data[start_point];
							start_point++;
						}
						t.setContent(l);
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
					byte uin_le = data1[0];
					byte[] uin1 = new byte[uin_le];
					for(byte i = 0; i < uin_le; i++){
						uin1[i] = data1[i+1];
					}
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
						byte[] uin = new byte[p[10]];
						for(byte j = 0; j < p[10]; j++){
							uin[j] = p[j+11];
						}
						int start_point = 11+(byte)p[10];
						
						start_point += 2;
						byte[] l = null;
						start_point += 2;
						Vector tlvs = new Vector();
						ICQTlv msg = null;
						
						for(int i = 0; start_point < p.length; i++){
							
							short tlvid = this.bytesToShort(p[start_point],p[start_point+1]);
							start_point += 2;
							short tlen = this.bytesToShort(p[start_point],p[start_point+1]);
							start_point += 2;
							
							l = new byte[tlen];
							for(short j = 0; j < tlen; j++){
								l[j] = p[start_point];
								start_point++;
							}
							ICQTlv bla = new ICQTlv();
							bla.setHeader(tlvid,tlen);
							bla.setContent(l);
							if(tlvid == (short)0x0002){
								msg = bla;
							}
							tlvs.addElement(bla);
						}
						byte[] c = msg.getContent();

						start_point = 12 + this.bytesToShort(c[2],c[3]);
						int charset = start_point -2;
						int ml = c.length - start_point;
						byte[] d = new byte[ml];
						for(int j = 0; j < ml; j++){
							d[j] = c[start_point];
							start_point++;
						}
						//Support for multi encoding messages
						charset = this.bytesToShort(c[charset],c[charset+1]);
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
				this.REPORT_INTERVAL = this.bytesToShort(pak.getContent()[0],pak.getContent()[1]);
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
					short items = this.bytesToShort(pkg[start_point],pkg[start_point+1]); 
					this.SSI_ITEMS = items;
					//it=null;
					start_point += 2;
					
					this.groups = new String[items+10];
					this.max_id = new short[items+10];
					this.contacts_ = new Vector();
					for(short i = 0; i < items; i++){
						int item_name_len = 0;
						//it = new byte[2];
												
						item_name_len = this.bytesToShort(pkg[start_point],pkg[start_point+1]);
						start_point += 2;
						
						byte[] item_name = new byte[item_name_len];
						
						for(int j = start_point; j < start_point+item_name_len; j++){
							item_name[j-start_point] = pkg[j];
						}
						
						start_point += item_name_len;
						
						int gid = this.bytesToShort(pkg[start_point],pkg[start_point+1]);
						
						start_point += 2;
						
						int iid = this.bytesToShort(pkg[start_point],pkg[start_point+1]);
						
						start_point += 2;
						
						int itid = this.bytesToShort(pkg[start_point],pkg[start_point+1]);
						
						start_point += 2;
						
						int data_len = this.bytesToShort(pkg[start_point],pkg[start_point+1]);
						
						start_point += 2;
						
						byte[] data = new byte[data_len];
						for(int j = start_point; j < start_point+data_len; j++){
							data[j-start_point] = pkg[j];
						}
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
								int l = this.bytesToShort(data[2],data[3]);
								byte[] nick = new byte[l];
								for(int k = 0; k < l; k++){
									nick[k] = data[k+4];
								}
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
					
					byte[] it = new byte[4];
					it[3] = pkg[pkg.length-1];
					it[2] = pkg[pkg.length-2];
					it[1] = pkg[pkg.length-3];
					it[0] = pkg[pkg.length-4];
					this.SSI_LAST_MODIFIED = Utils.bytesToLong(it,true);
					
					pkg = null;
					break;
				case 0x000e:
					//SSI ack
					System.out.println("Srv ack:\n"+Utils.byteArrayToHexString(pak.getNetPackage()));
					if(this.awaiting_auth.containsKey(new Integer(pak.getSnackReqID()))){
						ICQPackage out = (ICQPackage)this.awaiting_auth.remove(new Integer(pak.getSnackReqID()));
						out.setFlap(++this.f_seq);
						this.conn.sendPackage(out.getNetPackage());
						System.out.println(out.getNetPackage());
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
					int st_point = 2+this.bytesToShort(c[0],c[1]);
					byte[] sv = new byte[c[st_point]];
					st_point++;
					for(int i = 0; i<sv.length; i++){
						sv[i] = c[st_point];
						st_point++;
					}
					System.out.println("You were added by: "+new String(sv));
					
					if(this.findContact(new String(sv)) != null){
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
		byte[] b = null;
		byte[] h = null;
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
		out = new ICQPackage();
		out.setSnac(21,2,0,++this.s_seq);
		t = new ICQTlv();
		b = new byte[16];
		b[0] = (byte)0x0E;
		b[1] = (byte)0x00;
		h = Utils.intToBytes(Integer.parseInt(this.user),false);
		b[2] = h[0];
		b[3] = h[1];
		b[4] = h[2];
		b[5] = h[3];
		//request id
		b[6] = (byte)0xd0;
		b[7] = (byte)0x07;
		h = Utils.shortToBytes((short)this.s_seq,false);
		b[8] = h[0];
		b[9] = h[1];
		b[10] = (byte)0x24;
		b[11] = (byte)0x04;
		b[12] = (byte)0x01;
		b[13] = (byte)0x00;
		b[14] = (byte)0x01;
		b[15] = (byte)0x00;
		t.setContent(b);
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
    			if(this.groups[i]!=null){
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
    
    public short bytesToShort(byte b1, byte b2){
    		byte[] b = new byte[2];
    		b[0] = b1;
    		b[1] = b2;
    		
    		return Utils.bytesToShort(b,true);
    }
    
    public void addContact(Contact c){
	    // TODO: Send snack 0x0013,0x0008
    		c.setProtocol(this);
    		ICQContact ic = new ICQContact(c);
    		short gid = 0;
		int tmp_s = 0;
		ByteOperator bo = new ByteOperator();
    		if((gid = this.groupID(ic.groupName())) == -1){
    			
    			//temporarly fix TODO: fix :)
    			if(ic.groupName() == null)
    				ic.setGroupName("Top-level");
    			
    			//Default settings
    			gid = this.getFreeGID();
    			this.groups[gid] = ic.groupName();
    			this.max_id[gid] = (short)0x01;
    			ic.setIcqGID(gid);
    			ic.setIcqID(this.max_id[gid]);
    			this.contacts_.addElement(ic);
    			
    			//set group name len
    			bo.append(Utils.shortToBytes((short)ic.groupName().length(),true));
    			
    			//set group name
    			byte[] name = ic.groupName().getBytes();
    			bo.append(name);
    			//set GID
    			bo.append(Utils.shortToBytes(gid,true));
    			//set bID
    			//set type (0x0001 = group)
    			bo.append((byte)0,(byte)0,(byte)0,(byte)1);
    			//set group members tlv len -> first time created so 6 bytes
    			bo.append((byte)0,(byte)6,(byte)0,(byte)0xc8,(byte)0,(byte)2,(byte)0,(byte)1);
    				
    			//set id len
    			bo.append(Utils.shortToBytes((short)ic.userID().length(),true));
    			bo.append(ic.userID().getBytes());
    			bo.append(Utils.shortToBytes(gid,true));
    			bo.append((byte)0,(byte)1,(byte)0,(byte)0);
    			bo.append(Utils.shortToBytes((short)(4+ic.screenName().length()),true));
    			bo.append((byte)1,(byte)0x31);
    			bo.append(Utils.shortToBytes((short)ic.screenName().length(),true));
    			bo.append(ic.screenName().getBytes());
    			
    			ICQPackage ssi = new ICQPackage();
    			ssi.setChannel((byte)0x02);
    			ssi.setContent(bo.getBytes());
    			ssi.setSnac(19,8,0,++this.s_seq);
    			ssi.setFlap(++this.f_seq);
    			
    			tmp_s = this.s_seq;
    			
    			ICQPackage addEnd = new ICQPackage();
    			addEnd.setChannel((byte)0x02);
    			addEnd.setSnac(19,18,0,++this.s_seq);
    			addEnd.setFlap(++this.f_seq);
    			
    			this.awaiting_auth.put(new Integer(tmp_s),addEnd);
    			
    			this.conn.sendPackage(ssi.getNetPackage());
    			
    			System.out.println(Utils.byteArrayToHexString(ssi.getNetPackage()));
    			
    		}else{
    			//TODO: send add buddy and modify buddy using the awaiting auth hashtable
    			this.groups[gid] = ic.groupName();
    			this.max_id[gid] = (short)0x01;
    			ic.setIcqGID(gid);
    			ic.setIcqID(++this.max_id[gid]);
    			this.contacts_.addElement(ic);
    			
    			bo.append(Utils.shortToBytes((short)ic.userID().length(),true));
    			bo.append(ic.userID().getBytes());
    			bo.append(Utils.shortToBytes(gid,true));
    			bo.append(Utils.shortToBytes(this.max_id[gid],true));
    			bo.append((byte)0,(byte)0);
    			bo.append(Utils.shortToBytes((short)(4+ic.screenName().length()),true));
    			bo.append((byte)0x01,(byte)0x31);
    			bo.append(Utils.shortToBytes((short)ic.screenName().length(),true));
    			bo.append(ic.screenName().getBytes());
    			ICQPackage buddy = new ICQPackage();
    			buddy.setChannel((byte)0x02);
    			buddy.setContent(bo.getBytes());
    			
    			bo.clear();
    			
    			bo.append(Utils.shortToBytes((short)ic.groupName().length(),true));
    			bo.append(ic.groupName().getBytes());
    			bo.append(Utils.shortToBytes(gid,true));
    			bo.append((byte)0,(byte)0,(byte)0,(byte)1);
    			bo.append(Utils.shortToBytes((short)(4+this.max_id[gid]*2),true));
    			bo.append((byte)0x00,(byte)0xc8);
    			for(short i = 1; i <= this.max_id[gid]; i++){
    				bo.append(Utils.shortToBytes(i,true));
    			}
    			
    			ICQPackage grp = new ICQPackage();
    			grp.setChannel((byte)0x02);
    			grp.setContent(bo.getBytes());
    			
    			buddy.setSnac(19,8,0,++this.s_seq);
    			buddy.setFlap(++this.f_seq);
    			tmp_s = this.s_seq;
    			grp.setSnac(19,9,0,++this.s_seq);
    			
    			this.awaiting_auth.put(new Integer(tmp_s),grp);
    			
    			tmp_s = this.s_seq;
    			
    			ICQPackage addEnd = new ICQPackage();
    			addEnd.setChannel((byte)0x02);
    			addEnd.setSnac(19,18,0,++this.s_seq);
    			addEnd.setFlap(++this.f_seq);
    			
    			this.awaiting_auth.put(new Integer(tmp_s),addEnd);
    			
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
