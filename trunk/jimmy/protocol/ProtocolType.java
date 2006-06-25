/*
 * Created on 25.6.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jimmy.protocol;

public interface ProtocolType {
	//constants for various protocol types used in Account
	public static final byte JABBER=0;
	public static final byte ICQ=1;
	public static final byte MSN=2;
	public static final byte YAHOO=3;
	
	public byte protocolType_ = -1;
	public byte getType();
}
