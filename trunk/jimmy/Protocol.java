/* JIMMY - Instant Mobile Messenger
   Copyright (C) 2006  JIMMY Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **********************************************************************
 File: jimmy/Protocol.java
 Version: pre-alpha  Date: 2006/05/12
 Author(s): Matevz Jekovec, Zoran Mesec
 */

package jimmy;

import java.util.Vector;
import jimmy.*;

/**
 * Astract class to be extended with implementations of various protocols.
 * Every protocol runs in its own thread. The thread is initialized in constructor and can be started by calling Protocol.startThread().
 *  
 * @author Matevz Jekovec
 * @author Zoran Mesec
 * version 1.0
 */
public abstract class Protocol implements Runnable
{
	public static final byte DISCONNECTED = 0; //disconnected, ok
	public static final byte CONNECTED = 1; //connected, ok
	public static final byte CONNECTING = 2; //connecting, busy
	public static final byte WRONG_PASSWORD = -1; //wrong password, error 
	public static final byte NO_CONNECTION = -2; //connection cannot be established, error

	public static final byte JABBER=0;
	public static final byte ICQ=1;
	public static final byte MSN=2;
	public static final byte YAHOO=3;

    protected byte status_;	//protocol status - connected, disconnected etc. - see the constants above
    protected Vector chatSessions_;	//list of active chat sessions
    protected Vector contacts_;	//list of contacts in the protocol
    protected ProtocolInteraction jimmy_;
    protected byte protocolType_; //protocol type (Jabber, ICQ, MSN - see the constants above)

    private Thread thread_;
    
    /**
     * Class constructor.
     * 
     * @param jimmy Reference to the main Jimmy application
     */
    public Protocol(ProtocolInteraction jimmy) {
    	this.jimmy_ = jimmy;
    	this.thread_ = new Thread(this);
    }
    
    /**
     * Start the thread. This calls Protocol.run() in a separate process.
     */
    public void startThread() {
    	this.thread_.start();
    }
    
    /**
     * Initializes the connection and logs in using the given account.
     * 
     * @param account User's account
     * @return true if connected and logged in successfully, otherwise false
     */
    public abstract boolean login(Account account);
    
    /**
     * Initializes the connection and logs in.
     * It uses the default server name and port for the protocol.
     * This method is provided for the convenience.
     * 
     * @param username User ID for login
     * @param passwd User password for login
     * @return true if connected and logged in successfully, otherwise false
     */
    public abstract boolean login(String username, String passwd);
    
    /**
     * Log out and close connection. 
     */
    public abstract void logout();
    
    /**
     * Returns the protocl status (connected, disconnected etc. see the constants above).
     * 
     * @return Protocol status - defined above.
     */
    public byte getStatus() {
    	return status_;
    }
    
    /**
     * Returns active chat sessions list.
     * 
     * @return Active chat sessions stored in Vector.
     */
    public Vector getChatSessions() {return chatSessions_;}
    
    /**
     * Starts a new chat session and returns a reference to it. The new chat session is added to the active protocol's chat sessions list.
     * 
     * @param user User ID which you want to start the chat with
     * @return A reference to newly created chat session
     */
    public abstract ChatSession startChatSession(Contact user);
    
    /**
     * Returns contacts list.
     * 
     * @return Contacts list stored in Vector.
     */
    public Vector getContacts() {return contacts_;}
    
    /**
     * Send a message.
     * 
     * @param msg Message in String
     * @param session Active Chat Session to send the message to
     */
    public abstract void sendMsg(String msg, ChatSession session);
    
    /**
     * Send a message to a list of contacts.
     * 
     * @param msg Message in String
     * @param contactsList A list of desired contacts the message should be sent to
     * @param session Active Chat Session to send the message to
     */
    public abstract void sendMsg(String msg, Vector contactsList, ChatSession session);

    /**
     * Returns the protocol type (Jabber, MSN etc.)
     * @return Protocol type defined in interface ProtocolType
     */
    public byte getType() {return protocolType_;}
}
