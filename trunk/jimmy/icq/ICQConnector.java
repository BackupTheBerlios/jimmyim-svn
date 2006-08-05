/**
 * 
 */
package jimmy.icq;

import jimmy.net.ServerHandler;
import java.util.Vector;
import jimmy.util.Utils;
/**
 * @author dejan
 *
 */
public class ICQConnector extends ServerHandler {
	private Vector pkgs = null;
	private final int TIMES_TO_TRY = 3;
	/**
	 * 
	 */
	public ICQConnector(String url, int port) {
		super(url, port);
		this.pkgs = new Vector();
	}
	
	public byte[] getNextPackage(){
		
		if(this.pkgs.size() > 0){
			Vector pk = (Vector)this.pkgs.firstElement();
			byte[] p = new byte[pk.size()];
			for(int i = 0; i < pk.size(); i++){
				p[i] = ((Byte)pk.elementAt(i)).byteValue();
			}
			this.pkgs.removeElementAt(0);
			System.out.println(Utils.byteArrayToHexString(p));
			return p;
		}else{
			byte[] b = super.getReplyBytes();
			int h = 0;
			if(b == null){
				while(b==null || h>this.TIMES_TO_TRY){
					b = super.getReplyBytes();
					h++;
				}
			}
			for(int i = 0; i < b.length;){
				byte[] bplen = new byte[2];
				bplen[0] = b[i+4];
				bplen[1] = b[i+5];
				int plen = Utils.bytesToInt(bplen,true)+6;
				Vector p = new Vector();
				int ind = i;
				for(int j = i; j < ind+plen; j++){
					p.addElement(new Byte(b[j]));
					i++;
					
				}
				this	.pkgs.addElement(p);
			}
			return this.getNextPackage();
		}
	}
	
	public void sendPackage(byte[] p){
		super.sendRequest(p);
	}
	
	public int getNumPackages(){
		return this.pkgs.size();
	}
}
