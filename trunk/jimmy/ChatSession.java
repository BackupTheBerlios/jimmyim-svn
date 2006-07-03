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
 File: jimmy/ChatSession.java

 Author(s): Matevz Jekovec
 */

package jimmy;

import java.util.Vector;

/**
 * Chat session represents a room (channel) for a chat.
 * Multiple users can join the room (similar to IRC or a conference meeting).
 * 
 * @author Matevz Jekovec
 */
public class ChatSession {
	private Protocol protocol_;	//reference to the associated protocol
	private Vector contacts_;	//list of the users present in the chat room
	
	/**
	 * Create a new ChatSession.
	 * 
	 * @param protocol Reference to the associated protocol
	 */
	public ChatSession(Protocol protocol) {
		this.contacts_ = new Vector();
		this.protocol_ = protocol;
	}
	
	/**
	 * Add a user to the list of present users.
	 * 
	 * @param c
	 */
    public void addContact(Contact c) {
    	this.contacts_.addElement(c);
    }
    
    /**
     * Removes a user from the chat session.
     * 
     * @param c User's contact
     * @return true if removal was successful, false if contact wasn't found
     */
    public boolean removeContact(Contact c) {
    	return this.contacts_.removeElement(c);
    }
    
    /**
     * Return the number of present users in the chat session.
     * 
     * @return A number of present users in integer
     */
    public int countContacts() {return this.contacts_.size();}

    /**
     * Return the present users list.
     * 
     * @return Users list stored in a vector
     */
    public Vector getContactsList() {return this.contacts_;}
    
    /**
     * Returns a reference to the associated protocol object.
     * 
     * @return A reference to the associated protocol
     */
    public Protocol getProtocol() {return this.protocol_;}
}
