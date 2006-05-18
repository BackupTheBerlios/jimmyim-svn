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

public class JabberProtocol extends Protocol {
	ServerHandler sh_;
	
	public JabberProtocol() {
		this.connected_ = false;
	}
	
	public boolean login(String userID, String password) {
		String userName = splitString(userID, '@')[0];
		String userServer = splitString(userID, '@')[1];
		
		this.sh_ = new ServerHandler("gristle.org", 5222);
		this.sh_.setTimeout(20000);
		this.sh_.connect();
		if (sh_.isConnected() == false)
			return false;
		
		this.connected_ = true;
		
		String oString = new String();
		//Welcome message
		oString =
			"<?xml version='1.0'?>" +
			"<stream:stream to='" + userServer + "' xmlns='jabber:client' xmlns:stream='http://etherx.jabber.org/streams' version='1.0'>";
		
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());
		
		//Poslji user/pass
		oString = "<iq type='set'>" +
			"<query xmlns='jabber:iq:auth'>" +
			"<username>" + userName + "</username>" +
			"<password>" + password + "</password>" +
			"<resource>globe</resource>" +
			"</query>" +
			"</iq>";
		
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());
		
		//Dobi seznam kontaktov
		oString = "<presence></presence>";
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());

		//Poslji sporocilo
		oString = "<message from='jimmy@gristle.org' to='thepianoguy@jabber.org' type='chat'> <body>I wish to complain about this parrot what I purchased not half an hour ago from this very boutique.</body> </message> "; //izpise seznam kontaktov
		this.sh_.sendRequest(oString);
		System.out.println("OUT:\n" + oString);
		System.out.println("IN:\n" + sh_.getReply());
		
		return true;
	}

	public void logout() {
		// TODO Auto-generated method stub

	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void sendMsg(String msg, ChatSession session) {
		// TODO Auto-generated method stub

	}

	public void sendMsg(String msg, Contact user, ChatSession session) {
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
