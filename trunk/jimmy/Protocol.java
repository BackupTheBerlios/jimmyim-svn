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

/**
 * Astract class to be extended with implementations of various protocols.
 * 
 * @author Matevz Jekovec
 * @author Zoran Mesec
 * version 1.0
 */
public abstract class Protocol 
{
    protected boolean connected_;
    protected Vector chatSessions_;	//list of active chat sessions
    protected Vector contacts_;	//list of contacts in the protocol
    
    /**
     * This abstract method initializes connection and logs in.
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
     * Returns connection status.
     * 
     * @return Connection status - true if connected, false otherwise
     */
    public boolean isConnected() {return connected_;}
    
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
}
