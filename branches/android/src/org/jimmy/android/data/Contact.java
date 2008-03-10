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
 File: jimmy/Contact.java
 Version: pre-alpha  Date: 2006/05/12
 Author(s): Matevz Jekovec, Zoran Mesec
 */

package org.jimmy.android.data;

/**
 * Class Contact represents a single user contact.
 * 
 * @author Matevz Jekovec
 */
public class Contact {
	private Protocol protocol_; //reference to the protocol object of this contact
	private String userID_; //user ID
	private String screenName_; //user's nick/screen name
	private String groupName_; //group which this contact is part of
	
	//contact possible status follow
	public static final byte ST_OFFLINE = 0;
	public static final byte ST_ONLINE = 1;
	public static final byte ST_AWAY = 2;
	public static final byte ST_BUSY = 3;
	
	private int status_; //see ST_*
	private String statusMsg_;	//custom status description eg. I'm out for a Jazz concert at Ljubljana

	/**
	 * Create a new contact.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 * @param status User status - online, offline, away, busy etc.
	 * @param groupName Group's name which the user belongs to
	 * @param screenName User's nick or the name shown on the display
	 */
	public Contact(String userID, Protocol protocol, int status, String groupName, String screenName) {
		userID_ = userID;
		protocol_ = protocol;
		status_ = status;
		statusMsg_ = null;
		
		groupName_ = groupName;
		screenName_ = screenName;
	}
	
	/**
	 * Create a new contact. This method is provided by the convenience.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 */
	public Contact(String userID, Protocol protocol) {
		this(userID, protocol, 0, null, null);
	}
	
	/**
	 * Sets a new user ID
	 * 
	 * @param s ID
	 */
	public void setUserID(String s){
		this.userID_ = s;
	}
	
	/**
	 * Change user's status
	 * 
	 * @param status User's new status
	 */
	public void setStatus(int status) {
		status_ = status;
	}
	
	/**
	 * Set user's custom status message
	 * 
	 * @param msg User's new status message
	 */
	public void setStatusMsg(String msg) {
		statusMsg_ = msg;
	}
	
	/**
	 * Change user's group.
	 * 
	 * @param groupName User's new group name
	 */
	public void setGroupName(String groupName) {
		groupName_ = groupName;
	}
	
	/**
	 * Change user's screen name.
	 * 
	 * @param screenName User's new screen name
	 */
	public void setScreenName(String screenName) {
		screenName_ = screenName;
	}
	
	/**
	 * Set the current protocol
	 * 
	 * @param p protocol
	 */
	public void setProtocol(Protocol p){
		this.protocol_ = p;
	}
	
	
	public String userID() {return userID_;}
	public String screenName() {return screenName_;}
	public String groupName() {return groupName_;}
	public int status() {return status_;}
	public String statusMsg() {return statusMsg_;}
	public Protocol protocol() {return protocol_;}	
	
    
    public static String insertSpaces(String s){
	StringBuffer sb = new StringBuffer(s);
	for(int i=0; i<sb.length(); i++){
	    if(sb.charAt(i) == '%' && sb.charAt(i+1) == '2' && sb.charAt(i+2) == '0'){
		sb.delete(i,i+3);
		sb.insert(i,' ');
	    }
	}
	
	return sb.toString();
    }
    
    public static String removeSpaces(String s){
	StringBuffer sb = new StringBuffer(s);
	for(int i=0; i<sb.length(); i++){
	    if(sb.charAt(i) == ' '){
		sb.delete(i,i+1);
		sb.insert(i,"%20");
	    }
	}
	
	return sb.toString();	
    }
	
}
