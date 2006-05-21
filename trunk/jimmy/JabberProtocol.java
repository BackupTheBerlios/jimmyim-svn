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

package jimmy;

import java.util.Vector;

public class JabberProtocol extends Protocol {
	ServerHandler sh_;
	private final String JABBERSERVER="jabber.org";
	private final int PORT=5222;
	
	public JabberProtocol() {
		this.connected_ = false;
	}
	
	public boolean login(Account account) {
		String userName = splitString(account.getUser(), '@')[0];
		String userServer = splitString(account.getUser(), '@')[1];
		String server = (account.getServer()!=null) ? account.getServer() : JABBERSERVER; 
		int port = (account.getPort()!=0) ? account.getPort() : PORT;
		
		this.sh_ = new ServerHandler(server, port);
		this.sh_.connect();
		if (sh_.isConnected() == false)
			return false;
		
		this.connected_ = true;
		
		String oString = new String();
		//Welcome message
		oString =
			"<?xml version='1.0'?>" +
			"<stream:stream to='" + userServer + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams'>";
		
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());
		
		//Poslji user/pass
		oString = "<iq type='set'>" +
			"<query xmlns='jabber:iq:auth'>" +
			"<username>" + userName + "</username>" +
			"<password>" + account.getPassword() + "</password>" +
			"<resource>globe</resource>" +
			"</query>" +
			"</iq>";
		
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());
		
		//Dobi seznam kontaktov
		oString = "<iq type='get'><query xmlns='jabber:iq:roster'/></iq>";
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());

		//Nastavi se "online"
		oString = "<presence type=\"available\"/>";// type=\"online\"/>";
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		
		//Poslji sporocilo
		//oString = "<message from='jimmy@gristle.org' to='thepianoguy@jabber.org' type='chat'> <body>I wish to complain about <b>this</b> parrot what I purchased not half an hour ago from this very boutique.</body> </message> ";
		//this.sh_.sendRequest(oString);
		//System.out.println("OUT:\n" + oString);
//		System.out.println("IN:\n" + sh_.getReply());
		
		int i=3;
		while (i!=6)
			System.out.println("IN:\n" + sh_.getReply());
			
		return true;
	}
	
	public boolean login(String userID, String password) {
		return this.login(new Account(userID, password, Protocol.PR_JABBER));
	}

	public void logout() {
		this.sh_.disconnect();
		this.connected_ = false;
	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMsg(String msg, ChatSession session) {
		// TODO Auto-generated method stub

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

        if( pos != -1 ){
            result[0] = in.substring( 0, pos ).trim();
            result[1] = in.substring( pos+1 ).trim();
        } else {
            result[0] = in.trim();
        }

        return result;
    }
}
