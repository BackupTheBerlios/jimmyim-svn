/*
 * Created on 14.5.2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package jimmy;

public class Contact {
	Contact(String userID, Protocol protocol) {
		this(userID, protocol, 0, null, null);
	}
	
	Contact(String userID, Protocol protocol, int status, String groupName, String screenName) {
		userID_ = userID;
		protocol_ = protocol;
		status_ = status;
		groupName_ = groupName;
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
	private int status_; //user status - online, away, busy etc.
	private Protocol protocol_; //reference to the protocol object of this contact
}
