/**
 * 
 */
package jimmy.icq;

import java.util.Vector;
import java.io.*;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.net.*;

/**
 * @author dejan
 *
 */
public class ICQProtocol extends Protocol{
	
	private final String AUTH_SERVER = "login.oscar.aol.com";
	private final int AUTH_SERVER_PORT = 5555;
	private String bos = "";
	//private ServerHandler auth = null;
	private ServerHandler conn = null;
	
	private String user;
	private String pass;
	
	public ICQProtocol(){
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
		l.setFlap((short)0x0000);
		ICQTlv t = new ICQTlv();
		t.setHeader((short)0x0001,(short)(this.user.getBytes()).length);
		t.setContent(this.user.getBytes());
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0002,(short)(this.pass.getBytes()).length);
		t.setContent(this.roast(this.pass.getBytes()));
		l.addTlv(t);
		
		t = new ICQTlv();
		String v = new String("Jimmy IM, 0.0.01");
		t.setHeader((short)0x0003,(short)(v.getBytes()).length);
		t.setContent(this.roast(v.getBytes()));
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x01;
		t.setHeader((short)0x0016,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x00;
		t.setHeader((short)0x0017,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x00;
		t.setHeader((short)0x0018,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x01;
		t.setHeader((short)0x0019,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		

		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x05;
		t.setHeader((short)0x001A,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x05;
		t.setHeader((short)0x001A,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x05;
		t.setHeader((short)0x001A,(short)0x0002);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		ver[0] = (byte)0x00;
		ver[1] = (byte)0x01;
		t.setHeader((short)0x0014,(short)0x0004);
		t.setContent(ver);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x000f,(short)0x0002);
		t.setContent((new String("si").getBytes()));
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x000e,(short)0x0002);
		t.setContent((new String("si").getBytes()));
		l.addTlv(t);
		
		l.setFlapSize(l.getSize()-ICQPackage.FLAP_HEADER_SIZE);
		//auth connection
		this.conn = new ServerHandler(this.AUTH_SERVER,this.AUTH_SERVER_PORT);
		this.conn.connect();
		System.out.println("Bla1");
		System.out.println(this.conn.getReply());
		System.out.println("Bla2");
		this.conn.sendRequest(l.getPackage());
		System.out.println("Bla3");
		System.out.println(this.conn.getReply());
		System.out.println("Bla4");
		//SECOND PART OF STAGE ONE (MD5 AUTH)
		//END STAGE ONE
		
		return false;
	}

	public void logout() {
		// TODO Auto-generated method stub
		
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
	
}
