/**
 * 
 */
package jimmy.icq;

/**
 * @author dejan
 *
 */
public class ICQLib {

	public static byte[] getUsedFamilies(){
		byte[] uf = {
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
		
		return uf;
	}
	
	public static byte[] getCaps(){
		byte[] caps = {(byte)0x09,(byte)0x46,(byte)0x00,(byte)0x00,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00  //unknown capability - copied from gaim
				,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4D,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00						//cross ICQ - AIM messaging
				,(byte)0x09,(byte)0x46,(byte)0x13,(byte)0x4E,(byte)0x4C,(byte)0x7F,(byte)0x11,(byte)0xD1,(byte)0x82,(byte)0x22,(byte)0x44,(byte)0x45,(byte)0x53,(byte)0x54,(byte)0x00,(byte)0x00};
		return caps;
	}
	
	/**
	 * Generates the first package to send to the server.
	 * @param user
	 * @param pass
	 * @param toast
	 * @param cli_id
	 * @param f_seq
	 * @return
	 */
	public static ICQPackage getFirstLoginPackage(String user, String pass, byte[] toast, String cli_id, short f_seq){
		
		ICQPackage l = new ICQPackage();
		l.setChannel((byte)0x01);
		byte[] ver = {(byte)0x00,(byte)0x00, (byte)0x00,(byte)0x01};
		l.setContent(ver);
		
		ICQTlv t = new ICQTlv();
		t.setHeader((short)0x0001,(short)(user.getBytes()).length);
		t.setContent(user.getBytes());
		l.addTlv(t);
		t = new ICQTlv();
		t.setHeader((short)0x0002,(short)(pass.getBytes()).length);
		t.setContent(toast);
		l.addTlv(t);
		
		t = new ICQTlv();
		t.setHeader((short)0x0003,(short)(cli_id.getBytes()).length);
		t.setContent(cli_id.getBytes());
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
		l.setFlap(++f_seq);
		l.setFlapSize(l.getSize()-ICQPackage.FLAP_HEADER_SIZE);
		
		return l;
	}
}
