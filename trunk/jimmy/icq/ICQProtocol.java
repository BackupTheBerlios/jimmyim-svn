/**
 * 
 */
package jimmy.icq;

import java.util.Vector;
import java.io.*;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Jimmy;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.*;
import jimmy.util.*;

/**
 * @author dejan
 *
 */
public class ICQProtocol extends Protocol{
	
	private final String AUTH_SERVER = "login.oscar.aol.com";
	//private final String AUTH_SERVER = "zabica";
	private final int AUTH_SERVER_PORT = 5190;
	//private final int AUTH_SERVER_PORT = 6666;
	private final String AIM_MD5_STRING = "AOL Instant Messenger (SM)";
	private final String cli_id = new String("ICQ Inc. - Product of ICQ (TM).2003a.5.45.1.3777.85");
	private String bos = "";
	private byte[] cookie;
	private ICQPackage response;
	private ProtocolInteraction me;
	//private ServerHandler auth = null;
	private ServerHandler conn = null;
	
	private String user;
	private String pass;
	
	public ICQProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		this.connected_ = false;
	}
	
	public boolean login(Account account) {
		// TODO Auto-generated method stub
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
		l.setFlap((short)0x0001);
		l.setFlapSize(l.getSize()-ICQPackage.FLAP_HEADER_SIZE);
		
		//auth connection
		byte[] b = l.getNetPackage();
		this.conn = new ServerHandler(this.AUTH_SERVER,this.AUTH_SERVER_PORT);
		this.conn.connect();
		
		//check the connection acknowledgement
		if(this.conn.getReplyBytes() == null)
			System.out.println("Error: not responding.");
		
		//Send the first auth package
		this.conn.sendRequest(b);
		
		//get cookie or err
		this.response = new ICQPackage(this.conn.getReplyBytes());
		this.tlvDecode(this.response.getTlv(1));
		this.conn.disconnect();
		System.out.println(this.bos);
		//SECOND PART OF STAGE ONE (MD5 AUTH)
		l = new ICQPackage();
		l.setChannel((byte)0x02);
		l.setSnac(23,6,0,1);
		t = new ICQTlv();
		t.setHeader((short)1,(short)this.user.length());
		t.setContent(user);
		l.addTlv(t);
		t = new ICQTlv();
		t.setHeader((short)0x004B,(short)0x0000);
		l.addTlv(t);
		t = new ICQTlv();
		t.setHeader((short)0x005a,(short)0x0000);
		l.addTlv(t);
		l.setFlap((short)0x0002);
		
		this.conn.connect();
		if(this.conn.getReplyBytes() == null)
			System.out.println("Error: not responding.");
		
		this.conn.sendRequest(l.getNetPackage());
		this.response = new ICQPackage(this.conn.getReplyBytes());
		
		byte[] md5_key = this.response.getContent();
		System.out.println(Utils.byteArrayToHexString(md5_key));
		MD5 md5 = new MD5(md5_key);
		md5.update(this.pass.getBytes());
		md5.update(this.AIM_MD5_STRING.getBytes());
		byte[] md5_hash = md5.doFinal();
		System.out.println(Utils.byteArrayToHexString(md5_hash));
		l = new ICQPackage();
		l.setSnac(23,2,0,35);
		
		t = new ICQTlv();
		t.setHeader((short)1,(short)this.user.length());
		t.setContent(this.user.getBytes());
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0003,(short)this.cli_id.length());
		t.setContent(this.cli_id.getBytes());
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0025,(short)md5_hash.length);
		t.setContent(md5_hash);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x16,(short)0x02);
		ver = new byte[2];
		ver[0] = (byte)0x01;
		ver[1] = (byte)0x0a;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x17,(short)0x02);
		ver = new byte[2];
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x05;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x18,(short)0x02);
		ver = new byte[2];
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x2d;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x19,(short)0x02);
		ver = new byte[2];
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x01;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x1A,(short)0x02);
		ver = new byte[2];
		ver[0] = (byte)0x0e;
		ver[1] = (byte)0xc1;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x14,(short)0x04);
		ver = new byte[4];
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x00;
		ver[2] = (byte)0x00;
		ver[3] = (byte)0x55;
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0F,(short)0x02);
		t.setContent("en");
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0E,(short)0x02);
		t.setContent("us");
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x4A,(short)0x01);
		ver = new byte[1];
		ver[0] = (byte)0x01;
		t.setContent(ver);
		l.addTlv(t);
		l.setFlap((short)0x0035);
		//System.out.println(l.getNetPackage().length);
		this.conn.sendRequest(l.getNetPackage());
		
		System.out.println(Utils.byteArrayToHexString(this.conn.getReplyBytes()));
		//System.out.println(Utils.byteArrayToHexString(this.conn.getReplyBytes()));
		//END STAGE ONE
		
		return false;
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
			//System.out.println(Utils.byteArrayToHexString(this.cookie));
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

	/**
	 * add the interface handle
	 */
	public ProtocolInteraction addProtocolInteractionIF(Jimmy j) {
		return j;
	}
	
	public void run() {
    	
    }
}
