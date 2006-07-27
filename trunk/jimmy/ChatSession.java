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
	private Vector contactList_;	//list of the users present in the chat room
	private Vector ignoreList_;		//list of the users which the message shouldn't be sent to
	
	/**
	 * Default constructor.
	 * 
	 * @param p Pointer to the associated protocol.
	 * @param contacts Array of Contacts which should be added to the ChatSession.
	 */
	public ChatSession(Protocol p, Contact[] contacts) {
		protocol_ = p;
		contactList_ = new Vector();
		ignoreList_ = new Vector();		

		addContacts(contacts);
	}
	
	/**
	 * This is an overloaded member function provided for convenience.
	 * 
	 * Default constructor.
	 * 
	 * @param p Pointer to the associated protocol.
	 * @param contact Contact which should be added to the ChatSession.
	 */
	public ChatSession(Protocol p, Contact contact) {
		protocol_ = p;
		contactList_ = new Vector();
		ignoreList_ = new Vector();		
		
		addContact(contact);
	}
	
	/**
	 * This is an overloaded member function provided for convenience.
	 * 
	 * Default constructor.
	 * 
	 * @param p Pointer to the associated protocol.
	 */
	public ChatSession(Protocol p) {
		protocol_ = p;
		contactList_ = new Vector();
		ignoreList_ = new Vector();		
	}
	
	/**
	 * Add a user to the list of present users.
	 * 
	 * @param c Pointer to the Contact to be added to the chat session.
	 */
    public void addContact(Contact c) {
    	contactList_.addElement(c);
    }
    
	/**
	 * Add users to the list of present users.
	 * 
	 * @param c Pointer to the array of Contacts to be added to the chat session.
	 */
    public void addContacts(Contact[] c) {
    	contactList_.copyInto(c);
    }
    
    /**
     * Add a user to the ignore list. Users on the ignore list don't receive your messages.
     * Use unignoreContact() to remove the contact from the list.
     * 
     * @param c Pointer to the Contact which should be ignored.
     */
    public void ignoreContact(Contact c) {
    	ignoreList_.addElement(c);
    }
    
    
    /**
     * Remove a user from the ignore list. Users on the ignore list don't receive your messages.
     *
     * @param c Pointer to the Contact which should be unignored.
     */
    public void unignoreContact(Contact c) {
    	ignoreList_.removeElement(c);
    }
    
    /**
	 * Add users to the list of present users.
	 * 
	 * @param c Pointer to the vector of Contacts to be added to the chat session.
	 */
    public void addContacts(Vector c) {
    	for (int i=0; i<c.size(); i++)
    		contactList_.addElement(c.elementAt(i));
    }

    /**
     * Remove a user from the chat session.
     * 
     * @param c User's contact
     * @return true if removal was successful, false if contact wasn't found
     */
    public boolean removeContact(Contact c) {
    	return this.contactList_.removeElement(c);
    }
    
    /**
     * Return the number of present users in the chat session.
     * 
     * @return A number of present users in integer
     */
    public int countContacts() { return contactList_.size(); }

    /**
     * Return the present users list.
     * 
     * @return Users list stored in a vector
     */
    public Vector getContactsList() { return contactList_; }
    
    /**
     * Return whether the chat session contains the given Contact.
     * 
     * @param c Pointer to the Contact, which the query is done for.
     * @return True, if this chat session contains the given Contact, false otherwise.
     */
    public boolean contains(Contact c) { return contactList_.contains(c); }
    
    /**
     * Returns a reference to the associated protocol object.
     * 
     * @return A reference to the associated protocol
     */
    public Protocol getProtocol() { return protocol_; }
    
    /**
     * Send a given message to all users not on the ignore list.
     * 
     * @param msg Message to be sent in String.
     */
    public void sendMsg(String msg) {
		protocol_.sendMsg(msg, this);
    }
}
