/**
 * 
 */
package jimmy.icq;



/**
 * @author dejan
 *
 */
public class ICQSnac extends ICQPackage{
	
	public ICQSnac(byte[] h,byte[] snac){
		this.content = snac;
		this.header = h;
	}
	
	public byte[] getHeader() {

		return this.header;
	}

	public void setHeader(byte[] head) {
		this.header = head;
	}

	public byte[] getBPackage() {
		byte[] pkg = new byte[this.header.length + this.content.length];
		
		for(int i=0; i<this.header.length; i++)
			pkg[i]=this.header[i];
		
		for(int i=0; i<this.content.length; i++)
			pkg[this.header.length + i]=this.content[i];
		
		return pkg;
	}

	public String getSPackage() {
		return new String(this.header)+new String(this.content);
	}

	public void setContent(byte[] cont) {
		this.content = cont;
	}

	public void setContent(String cont) {
		this.content = cont.getBytes();
	}

}
