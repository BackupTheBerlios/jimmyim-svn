/*
 * Created on 25.6.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jimmy.protocol;

public interface ProtocolStatus {
	public static final byte DISCONNECTED = 0; //disconnected, ok
	public static final byte CONNECTED = 1; //connected, ok
	public static final byte CONNECTING = 2; //connecting, busy
	public static final byte WRONG_PASSWORD = -1; //wrong password, error 
	public static final byte NO_CONNECTION = -2; //connection cannot be established, error
	
	public byte status_ = 0;
	public byte getStatus();
}
