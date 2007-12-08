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
 File: jimmy/MSNContact.java
 Version: pre-alpha  Date: 2006/05/12
 Author(s): Matevz Jekovec, Zoran Mesec
 */

package jimmy.msn;

import jimmy.Contact;
import jimmy.Protocol;

/**
 * Class MSNContact represents a single user contact.
 * 
 * 
 * @author Matevz Jekovec
 */
public class MSNContact extends Contact {
	/*private Protocol protocol_; //reference to the protocol object of this contact
	private String userID_; //user ID
	private String screenName_; //user's nick/screen name
	private String groupName_; //group which this contact is part of*/
	
        private String userHash;
        private String groupHash;
        private short lists;
    
	//contact possible status follow
	/*public static final byte ST_OFFLINE = 0;
	public static final byte ST_ONLINE = 1;
	public static final byte ST_AWAY = 2;
	public static final byte ST_BUSY = 3;*/

	/**
	 * Create a new contact.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 * @param status User status - online, offline, away, busy etc.
	 * @param groupName Group's name which the user belongs to
	 * @param screenName User's nick or the name shown on the display
	 */
	public MSNContact(String userID, Protocol protocol, int status, String groupName, String screenName) 
        {
                super(userID, protocol, status, groupName, screenName);
	}
	
	/**
	 * Create a new contact. This method is provided by the convenience.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 */
	public MSNContact(String userID, Protocol protocol) {
		this(userID, protocol, 0, null, null);
	}
	
	/**
	 * Sets a new user hash
	 * 
	 * @param hash hash 
	 */
	public void setUserHash(String hash){
		this.userHash = hash;
	}
	
	/**
	 * Sets a new user's group hash
	 * 
	 * @param hash hash 
	 */
	public void setGroupHash(String hash){
		this.groupHash = hash;
	}
        
	/**
	 * Sets a number for lists
	 * 
	 * @param short list
	 */
	public void setLists(short list){
		this.lists = list;
	}

         /**
	 * Get the user's lists number
	 * 
	 * @return
	 */
	public short getLists() {return this.lists;}    
        
 	/**
	 * Get the user hash
	 * 
	 * @return
	 */
        
	public String getUserHash() {return this.userHash;}    
        
 	/**
	 * Get the user's group hash
	 * 
	 * @return
	 */
	public String getGroupHash() {return this.groupHash;}          
}
