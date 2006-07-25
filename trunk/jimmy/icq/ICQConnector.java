/**
 * 
 */
package jimmy.icq;

import jimmy.net.ServerHandler;
import java.util.Vector;
/**
 * @author dejan
 *
 */
public class ICQConnector extends ServerHandler {
	private Vector bytes = null;
	/**
	 * 
	 */
	public ICQConnector(String url, int port) {
		super(url, port);
	}
	
	public byte[] getNextPackage(){
		
		//TODO have to fix the reply to return the next package in from the stream
		
		return super.getReplyBytes();
	}
}
