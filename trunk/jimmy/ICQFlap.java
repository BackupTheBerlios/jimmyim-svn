/**
 * 
 */
package jimmy;

/**
 * @author dejan
 *
 */
public class ICQFlap extends ICQPackage {

	public void setChanell(short c){
		
		switch(c){
			case 1:
				this.header[1]=(byte)1;
				break;
			case 2:
				this.header[1]=(byte)2;
				break;
			case 3:
				this.header[1]=(byte)3;
				break;
			case 4:
				this.header[1]=(byte)4;
				break;
			case 5:
				this.header[1]=(byte)5;
				break;
		}
	}
	
	public void setLength(short l){
		byte a = 0x23;
	}
	
	public byte[] getHeader() {

		return this.header;
	}

	public void setHeader(byte[] head) {

		//this.header = head;
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

		return this.header.toString()+this.content.toString();
	}

	public void setContent(byte[] cont) {
		
		this.content = cont;
	}

	public void setContent(String cont) {
		
		this.content = cont.getBytes();
	}

	
}
