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
 File: jimmy/YahooProtocol.java
 Version: pre-alpha  Date: 2006/07/27
 Author(s): Matevz Jekovec
 */

package jimmy.yahoo;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.ServerHandler;

public class YahooProtocol extends Protocol {
	ServerHandler sh_;	//server handler reference - used for any outgoing/incoming connections
	private final String DEFAULT_SERVER_ = "scs.msg.yahoo.com";	//default server name - if none set in user account
	private final int DEFAULT_PORT_ = 5050;	//default server port - if none set in user account

	final static String NL_ = "" + (char)0xc0 + (char)0x80;	//key-value separator string

	public YahooProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		protocolType_ = YAHOO;
		status_ = DISCONNECTED;
	}
	
	public boolean login(Account account) {
		String oString;
		String iString=null;
		
		sh_ = new ServerHandler(DEFAULT_SERVER_, DEFAULT_PORT_);
		sh_.connect();
		if (sh_.isConnected() == false)
			return false;
		
		//authentication 
		oString = "YMSG" + (char)0x09 + (char)0x00 + (char)0x00 + (char)0x00;	//using Yahoo protocol version 9
		oString = oString + (char)0x00 + (char)(account.getUser().length()+15);	//data length (yahoo username)
		oString = oString + (char)0x00 + (char)0x57;	//YAHOO_SERVICE_AUTH
		oString = oString + (char)0x30 + (char)0x00 + (char)0x00 + (char)0x00;	//YAHOO_STATUS_AVAILABLE
		oString = oString + (char)0x00 + (char)0x00 + (char)0x00 + (char)0x00; //+ (char)0x00 + (char)0x00 + (char)0x00 + (char)0x00 + (char)0x00;	//session ID
		oString = oString + (char)0x31 + NL_ + account.getUser() + NL_;	//1 : userID
		
		System.out.println("OUT:");
		for (int i=0; i<oString.length(); i++)
			System.out.println((int)oString.charAt(i));
		sh_.sendRequest(oString);

		iString = sh_.getReply();
		System.out.println("IN:");
		for (int i=0; i<iString.length(); i++)
			System.out.println((int)iString.charAt(i));
		
		return false;
	}

	public boolean login(String username, String passwd) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout() {
		this.sh_.disconnect();
		this.status_ = DISCONNECTED;
	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addContact(Contact c) {
		// TODO Auto-generated method stub

	}

	public void sendMsg(String msg, ChatSession session) {
		// TODO Auto-generated method stub

	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub

	}

	public void run() {
		// TODO Auto-generated method stub

	}
        
	public boolean removeContact(Contact c){
		return false;
	}
	
	public void updateContactProperties(Contact c) {
		
	}
}
