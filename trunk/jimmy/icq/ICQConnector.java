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
			return p;
		}else{
			int ind = 0;
			byte[] b = super.getReplyBytes();
			if(b == null){
				while(b==null){
					b = super.getReplyBytes();
				}
			}
			for(int i = 0; i < b.length; i++){
				byte[] bplen = new byte[2];
				bplen[0] = b[ind+4];
				bplen[1] = b[ind+5];
				int plen = Utils.bytesToInt(bplen,true)+6;
				Vector p = new Vector();
				for(int j = ind; j < plen; j++){
					p.addElement(new Byte(b[j]));
					ind++;
				}
				this	.pkgs.addElement(p);
				i=ind;
			}	
			return this.getNextPackage();
		}
	}
}
