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
 * 
 * @author matevz
 */
public class JabberProtocol extends Protocol {
	ServerHandler sh_;	//server handler reference - used for any outgoing/incoming connections
	private final String DEFAULT_SERVER = "jabber.org";	//default server name - if none set in user account
	private final int DEFAULT_PORT = 5222;	//default server port - if none set in user account
	private final String DEFAULT_STREAMS = "http://etherx.jabber.org/streams";	//default Jabber streams address
	
	/**
	 * Constructor method.
	 */
	public JabberProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		this.protocolType_ = JABBER;
	}
	
	/**
	 * Initializes the connection and logs in using the given account.
	 */
	public boolean login(Account account) {
		//get the first half of the JID (login name)
		String userName = splitString(account.getUser(), '@')[0]; 
		//get the second half of the JID (server name)
		String userServer = splitString(account.getUser(), '@')[1];
		//get the server name stored in the account - use default server if none set
		String server = (account.getServer()!=null) ? account.getServer() : DEFAULT_SERVER; 
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
		
		this.sh_.sendRequest(oString);
		iString = sh_.getReply();
		//check authentication feedback
		if (JabberParseXML.parseUserPass(iString)==false) {
			status_ = WRONG_PASSWORD;
			return false;
		}
		
		//Get contacts list
		oString = "<iq type='get'><query xmlns='jabber:iq:roster'/></iq>";
		this.sh_.sendRequest(oString);
		iString = sh_.getReply();
		//parse the contacts feedback
		contacts_ = JabberParseXML.parseContacts(iString, this);
		jimmy_.addContacts(contacts_);
		
		//Set status "online"
		oString = "<presence type=\"available\"/>";// type=\"online\"/>";
		this.sh_.sendRequest(oString);

		status_ = CONNECTED;
		startThread();
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
		this.sh_.disconnect();
		this.status_ = DISCONNECTED;
	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMsg(String msg, ChatSession session) {
		//Poslji sporocilo
		/*oString = "<message from='jimmyim@jabber.org' to='thepianoguy@jabber.org' type='chat'> <body>I wish to complain about <b>this</b> parrot what I purchased not half an hour ago from this very boutique.</body> </message> ";
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());*/
	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub

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
    	
    	while (true) {
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
    }
    
    public void parserNewContacts(Contact[] c) {
    	contacts_.copyInto(c);
    }
    
    public void addContact(Contact c){
	    
    }
}    
