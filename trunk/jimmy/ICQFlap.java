/**
 * 
 */
package jimmy;


/**
 * @author dejan
 *
 */
public class ICQFlap extends ICQPackage {

	public ICQFlap(){
		this.header[0] = 0x2A;
	}
	
	public void setChannel(short c){
		
		switch(c){
			case 1:
				this.header[1]=0x01;
				break;
			case 2:
				this.header[1]=0x02;
				break;
			case 3:
				this.header[1]=0x03;
				break;
			case 4:
				this.header[1]=0x04;
				break;
			case 5:
				this.header[1]=0x05;
				break;
		}
	}
	
	public void setSequenceN(short n){
		byte[] seq = Utils.shortToBytes(n,true);
		this.header[2]=seq[0];
		this.header[3]=seq[1];
	}
	
	public void setLength(short l){
		byte[] a = Utils.shortToBytes(l,true);
		this	.header[4] = a[0];
		this.header[5] = a[1];
	}
	
	public byte getChannel(){
		return this.header[1];
	}
	
	public byte[] getSequenceN(){
		byte[] seq = new byte[2];
		seq[0]=this.header[2];
		seq[1]=this.header[3];
		return seq;
	}
	
	public byte[] getLength(){
		byte[] seq = new byte[2];
		seq[0]=this.header[4];
		seq[1]=this.header[5];
		return seq;
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
	
	public boolean isSNAC(){
		if(this.header[1] == 0x02)
			return true;
		else
			return false;
	}	
}
