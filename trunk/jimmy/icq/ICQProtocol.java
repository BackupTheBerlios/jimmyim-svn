/**
 * 
 */
package jimmy.icq;

import java.util.Vector;
import java.util.Random;

import com.sun.midp.io.Util;
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
	private byte[] cookie;
	private byte[] services;
	private byte[] service_ver;
	private byte[] capabilities = {(byte)0x09,(byte)0x46,(byte)0x00,(byte)0x00,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00  //unknown capability - copied from gaim
			,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4D,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00						//cross ICQ - AIM messaging
			,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4E,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00};
	private ICQPackage response;
	private ICQPackage service_versions = null;
	private ProtocolInteraction me;
	//private ServerHandler auth = null;
	private ICQConnector conn = null;
	
	private String user;
	private String pass;
	
	public ICQProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		this.status_ = DISCONNECTED;
		this.protocolType_ = ICQ;
	}
	
	public boolean login(Account account) {

		if(account.getServer() != null)
			this.AUTH_SERVER = account.getServer();
		
		if(account.getPort() != 0)
			this.AUTH_SERVER_PORT = account.getPort();
		
		if(login(account.getUser(),account.getPassword()))
			return true;
		
		return false;
	}

	public boolean login(String username, String passwd) {
		this.user = username;
		this.pass = passwd;
		
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
		System.out.println(this.bos);
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
		
		//I SHOULD take the port toke too but...
		this.bos_port = Integer.parseInt(this.bos.substring(this.bos.indexOf(':')+1,this.bos.length()));
		this.bos = this.bos.substring(0,this.bos.indexOf(":"));
		this.conn.setPort(this.bos_port);
		this.conn.setURL(this.bos);
		
		
		this.conn.connect();
		this.conn.sendPackage(l.getNetPackage());
		
		System.out.println(Utils.byteArrayToHexString(this.conn.getNextPackage()));
		System.out.println("recieving auth...");
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
		ver_req.setSnac(1,23,0,220);
		ver_req.setFlap(++this.f_seq);
		byte[] bla = ver_req.getNetPackage();
		this.conn.sendPackage(bla);
		
		b = this.conn.getNextPackage();

		if(b != null){
			System.out.println("Service versions");
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
		out.setSnac(1,6,0,2);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);

		//END STAGE TWO
		//STAGE THREE
		out = new ICQPackage();
		out.setSnac(2,2,0,3);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		this.pkgDecode(in);
		
		out = new ICQPackage();
		out.setChannel((byte)0x02);
		byte[] c = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0B,(byte)0x1F,(byte)0x40,(byte)0x03,(byte)0xE7,(byte)0x03,(byte)0xE7,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		out.setContent(c);
		out.setSnac(4,2,0,5);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		c = null;
		
		//END STAGE THREE
		//STAGE FOUR
		
		out = new ICQPackage();
		out.setSnac(1,30,0,6);
		t = new ICQTlv();
		byte[] d = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		t.setContent(d);
		t.setHeader((short)0x0006,(short)t.getCLen());
		out.addTlv(t);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		d=null;
		
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
		out.setSnac(1,2,0,7);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		//END STAGE FOUR
		used_families=null;
		
		System.out.println("Testiramo: \n"+Utils.byteArrayToHexString(this.conn.getNextPackage()));
		
		out = new ICQPackage();
		out.setSnac(1,30,0,6);
		t = new ICQTlv();
		byte[] h = {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};
		t.setContent(h);
		t.setHeader((short)0x0006,(short)t.getCLen());
		out.addTlv(t);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		
		
		
		b = this.conn.getNextPackage();
		
		System.out.println(Utils.byteArrayToHexString(b));
		
		out = new ICQPackage();
		out.setSnac(19,4,0,157);
		out.setFlap(++this.f_seq);
		this.conn.sendPackage(out.getNetPackage());
		

		
		b = this.conn.getNextPackage();
		System.out.println(Utils.byteArrayToHexString(b));
		b = this.conn.getNextPackage();
		System.out.println(Utils.byteArrayToHexString(b));
		
		
		//Contact list
		b = this.conn.getNextPackage();
		in = new ICQPackage(b);
		System.out.println(Utils.byteArrayToHexString(b));
		this.pkgDecode(in);
		
		
		//Awaiting for next package !!!! TEMPORARY !!!!
		b = this.conn.getNextPackage();
		System.out.println(Utils.byteArrayToHexString(b));
		System.out.println(this.conn.getNumPackages());
		
		return true;
	}

	public void logout() {
		// TODO Auto-generated method stub
		this.conn.disconnect();
	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMsg(String msg, ChatSession session) {
		// TODO Auto-generated method stub
		
	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub
		
	}
	
	public void tlvDecode(ICQTlv t){
		
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
			System.out.println("Got Cookie");
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
						this.me.stopProtocol(this);
					break;
			}
			//Drops auth error message
			break;
		default:
			break;
				
		}
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
				case 0x0013:
					//TODO: MOTD
					System.out.println("MOTD");
					break;
				case 0x0018:
					//TODO: Set the service version numbers
					this.service_versions = pak;
					System.out.println("decoding 18");
					break;
				}
				break;
			case 0x0002:
				switch(subtype){
				case 0x0003:
					//TODO: Location service limitations/capabilities
					ICQTlv cap = new ICQTlv();
					cap.setHeader((short)0x0005,(short)this.capabilities.length);
					cap.setContent(this.capabilities);
					ICQPackage cp = new ICQPackage();
					cp.setSnac(2,4,0,4);
					cp.addTlv(cap);
					cp.setFlap(++this.f_seq);
					this.conn.sendPackage(cp.getNetPackage());
					cap = null;
					cp = null;
					break;
				}
				break;
			case 0x0013:
				switch(subtype){
				case 0x0006://ROSTER reply
					byte[] pkg = pak.getContent();
					
					int start_point = 9;
					
					byte[] it = new byte[2];
					it[0] = pkg[start_point];
					it[1] = pkg[start_point+1];
					short items = Utils.bytesToShort(it,true); 
					it=null;
					start_point += 2;
					
					String[] groups = new String[items];
					this.contacts_ = new Vector();
					for(short i = 0; i < items; i++){
						int item_name_len = 0;
						it = new byte[2];
						it[0] = pkg[start_point];
						it[1] = pkg[start_point+1];
						start_point += 2;
						item_name_len = Utils.bytesToInt(it,true);
						
						byte[] item_name = new byte[item_name_len];
						
						for(int j = start_point; j < start_point+item_name_len; j++){
							item_name[j-start_point] = pkg[j];
						}
						
						start_point += item_name_len;
						it[0] = pkg[start_point];
						it[1] = pkg[start_point+1];
						int gid = Utils.bytesToInt(it,true);
						
						start_point += 2;
						
						it[0] = pkg[start_point];
						it[1] = pkg[start_point+1];
						int iid = Utils.bytesToInt(it,true);
						
						start_point += 2;
						
						it[0] = pkg[start_point];
						it[1] = pkg[start_point+1];
						int itid = Utils.bytesToInt(it,true);
						
						start_point += 2;
						
						it[0] = pkg[start_point];
						it[1] = pkg[start_point+1];
						int data_len = Utils.bytesToInt(it,true);
						
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
							groups[gid] = new String(item_name);
						}else if(itid == 0){ //if buddy 
							Contact ct = new Contact(new String(item_name),this);
							if(data[0] == (byte)0x01 && data[1] == (byte)0x31){
								it[0] = data[2];
								it[1] = data[3];
								int l = Utils.bytesToInt(it,true);
								byte[] nick = new byte[l];
								for(int k = 0; k < l; k++){
									nick[k] = data[k+4];
								}
								System.out.println(new String(item_name)+" : "+new String(nick)
										+" in group: "+ groups[gid]);
								ct.setScreenName(new String(nick));
								ct.setGroupName(groups[gid]);
								this.contacts_.addElement(ct);
							}
							
						}
					}
					groups = null;
					pkg = null;
					break;
				}
				break;
			}
		
		}
		
		return false;
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

	public void run() {
    	
    }

    
    
    public void addContact(Contact c){
	    
    }

	public boolean removeContact() {
		// TODO Auto-generated method stub
		return false;
	}
}
