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
			byte[] b = super.getReplyBytes();
			if(b == null){
				while(b==null){
					b = super.getReplyBytes();
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
}
