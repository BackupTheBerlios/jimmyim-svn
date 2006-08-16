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
 File: jimmy/JabberProtocol.java
 Version: pre-alpha  Date: 2006/05/18
 Author(s): Matevz Jekovec
 */

package jimmy.jabber;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.net.ServerHandler;
import jimmy.ProtocolInteraction;

/**
 * This class implements the Jabber protocol.
 * Non-standard comments are present before any method.
 * For the standard methods description, see jimmy/Protocol.java .
 * 
 * @author Matevž Jekovec
 */
public class JabberProtocol extends Protocol {
	ServerHandler sh_;	//server handler reference - used for any outgoing/incoming connections
	private final String DEFAULT_SERVER = "jabber.org";	//default server name - if none set in user account
	private final int DEFAULT_PORT = 5222;	//default server port - if none set in user account
	private final String DEFAULT_STREAMS = "http://etherx.jabber.org/streams";	//default Jabber streams address
	
	private boolean stop_;	//stop the thread
	
	/**
	 * Constructor method.
	 */
	public JabberProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		protocolType_ = JABBER;
		status_ = DISCONNECTED;
	}
	
	/**
	 * Returns the ServerHandler used to establish the connection.
	 * 
	 * @return ServerHandler used when connecting.
	 */
	public ServerHandler getServerHandler() { return sh_; }
	
	/**
	 * Initializes the connection and logs in using the given account.
	 */
	public boolean login(Account account) {
		account_ = account;
		
		//get the first half of the JID (login name)
		String userName = splitString(account.getUser(), '@')[0]; 

		//get the second half of the JID (server name)
		String userServer = splitString(account.getUser(), '@')[1];

		//get the server name stored in the account - if none set, use the user@server ones, if none @ given, use the default one
		String server;
		if (account.getServer()!=null)
			server = account.getServer();
		else if (userServer.length()!=0)
			server = userServer;
		else
			server = DEFAULT_SERVER;

		//get the server port stored in the account - use default server port if none set
		int port = (account.getPort()!=0) ? account.getPort() : DEFAULT_PORT;
		
		sh_ = new ServerHandler(server, port);
		sh_.connect();
		if (sh_.isConnected() == false)
			return false;
		
		String oString = new String();
		String iString = new String();
		
		//Welcome message
		oString =
			"<?xml version='1.0'?>" +
			"<stream:stream to='" + userServer + "' xmlns='jabber:client' xmlns:stream='" + DEFAULT_STREAMS + "'>";
		
		sh_.sendRequest(oString);
		iString = sh_.getReply();	//ignore the welcome message
		
		//Authentication
		oString = "<iq type='set'>" +
			"<query xmlns='jabber:iq:auth'>" +
			"<username>" + userName + "</username>" +
			"<password>" + account.getPassword() + "</password>" +
			"<resource>globe</resource>" +
			"</query>" +
			"</iq>";
		
		sh_.sendRequest(oString);
		iString = sh_.getReply();

		//check authentication feedback
		if (JabberParseXML.parseUserPass(iString)==false) {
			status_ = WRONG_PASSWORD;
			System.out.println("JabberProtocol: WRONG PASSWORD!");
			return false;
		}
		
		//Get contacts list
		oString = "<iq type='get'><query xmlns='jabber:iq:roster'/></iq>";
		sh_.sendRequest(oString);
		iString = sh_.getReply();
		//parse the contacts feedback
		contacts_ = JabberParseXML.parseContacts(iString, this);
		jimmy_.addContacts(contacts_);
		
		//Set status "online"
		oString = "<presence type='available'/>";
		sh_.sendRequest(oString);

		status_ = CONNECTED;
		thread_.start();
		
		return true;
	}
	
	/**
	 * This method is provided by the convenience. It's the same as the upper one.
	 */
	public boolean login(String userID, String password) {
		return this.login(new Account(userID, password, JABBER));
	}

	/**
	 * Logs out and closes the connection.
	 */
	public void logout() {
		String oString;
		oString = "<presence type='offline'/>";
		sh_.sendRequest(oString);
		stop_ = true;	//stops the thread
		status_ = DISCONNECTED;
	}

	public ChatSession startChatSession(Contact contact) {
		ChatSession cs = new ChatSession(this);
		chatSessionList_.addElement(cs);
		
		cs.addContact(contact);
		
		return cs;
	}
	
	public void sendMsg(String msg, ChatSession session) {
		for (int i=0; i<session.countContacts(); i++) {
			String oString = "<message from='" + account_.getUser() + "' to='"
			 + ((Contact)session.getContactsList().elementAt(i)).userID() + "' type='chat'><body>"
			 + msg + "</body></message>\n";
			
			System.out.println("OUT:\n" + oString);
			sh_.sendRequest(oString);
		}
	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		for (int i=0; i<session.countContacts(); i++) {
			if (!contactsList.contains(session.getContactsList().elementAt(i)))
				continue;
				
			String oString = "<message from='" + account_.getUser() + "' to='"
			 + ((Contact)session.getContactsList().elementAt(i)).userID() + "' type='chat'><body>"
			 + msg + "</body></message>\n";
			
			System.out.println("OUT:\n" + oString);
			sh_.sendRequest(oString);
		}
	}
	
	/**
	 * Utility to split the given String of characters to two parts, seperated by ch.
	 * 
	 * @param in Input String
	 * @param ch Separator character
	 * @return Array of two Strings
	 */
    private static String[] splitString(String in, char ch) {
        String[] result = new String[2];
        int      pos = in.indexOf( ch );

        if( pos != -1 ) {
            result[0] = in.substring( 0, pos ).trim();
            result[1] = in.substring( pos+1 ).trim();
        } else {
            result[0] = in.trim();
        }

        return result;
    }
    
    public void run() {
    	String in;
    	stop_ = false;
    	
    	while (!stop_) {
    		try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    		if (status_!=CONNECTED) continue;
    		
    		in = sh_.getReply();
    		if (in != null)
    			JabberParseXML.genericParse(in, this, jimmy_);
    	}

    	sh_.disconnect();
    }
    
    public void parseNewContacts(Contact[] c) {
    	contacts_.copyInto(c);
    }
    
    public void addContact(Contact c) {
		String oString = "<presence from=\"" + account_.getUser() + "\" to=\"" + c.userID() + "\" type=\"subscribe\"/>\n";
		sh_.sendRequest(oString);
		contacts_.addElement(c);
    }
    
    public boolean removeContact(Contact c) {
    	if (getContact(c.userID())==null)
    		return false;
    	
    	String oString =
    		"<iq from='" + getAccount().getUser() + "' type='set' id='roster_4'>\n" +
    		"<query xmlns='jabber:iq:roster'>\n" +
    		"<item jid='" + c.userID() + "' subscription='remove'/>\n" +
    		"</query>\n" +
    		"</iq>\n";
    	
    	sh_.sendRequest(oString);
    	
    	return true;
    }
    
    public void updateContactProperties(Contact c) {
   		String oString =
   			"<iq type='set' from='" + getAccount().getUser() + "' to='" + c.userID() + "'>" +
   				"<query xmlns='jabber:iq:roster'>\n" +
   					"<item " +
   						"jid='" + c.userID() + "' " +
   						"subscription='from'" +
   						((c.screenName()!=null)?(" name='" + c.screenName() + "'"):"") + ">\n" +
   						((c.groupName()!=null)?("<group>" + c.groupName() + "</group>\n"):"") +
   					"</item>\n" +
   				"</query>\n" +
   			"</iq>\n";
   		
   		sh_.sendRequest(oString);
    }
}    
