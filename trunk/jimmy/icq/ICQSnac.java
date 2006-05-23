/**
 * 
 */
package jimmy.icq;



/**
 * @author dejan
 *
 */
public class ICQSnac extends ICQPackage{

	private static final int HEADER_SIZE = 10;
	private byte[] pkg;
	
	public ICQSnac(byte[] snac){
		this.pkg = snac;
	}
	
	public byte[] getHeader() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setHeader(byte[] head) {
		// TODO Auto-generated method stub
		
	}

	public byte[] getBPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getSPackage() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setContent(byte[] cont) {
		// TODO Auto-generated method stub
		
	}

	public void setContent(String cont) {
		// TODO Auto-generated method stub
		
	}

}
