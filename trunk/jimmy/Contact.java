/*
 * Created on 14.5.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package jimmy;

/**
 * Class Contact represents a single contact.
 * @author Matevz Jekovec
 */
public class Contact {
	/**
	 * Create a new contact.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 * @param status User status - online, offline, away, busy etc.
	 * @param groupName Group's name which the user belongs to
	 * @param screenName User's nick or the name shown on the display
	 */
	Contact(String userID, Protocol protocol, int status, String groupName, String screenName) {
		userID_ = userID;
		protocol_ = protocol;
		status_ = status;
		groupName_ = groupName;
		screenName_ = screenName;
	}
	
	/**
	 * Create a new contact. This method is provided by the convenience.
	 * 
	 * @param userID User ID on the server (usually the e-mail address)
	 * @param protocol Reference to the corresponding protocol
	 */
	Contact(String userID, Protocol protocol) {
		this(userID, protocol, 0, null, null);
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
	
	public String userID() {return userID_;}
	public String screenName() {return screenName_;}
	public String groupName() {return groupName_;}
	public int status() {return status_;}
	public Protocol protocol() {return protocol_;}
	
	private String userID_; //user ID
	private String screenName_; //user's nick/screen name
	private String groupName_; //group which this contact is part of
	
	/**
	 * int status_: 0 - offline
	 *              1 - online
	 *              2 - away
	 *              3 - busy
	 */
	private int status_; //user status
	
	private Protocol protocol_; //reference to the protocol object of this contact
}
