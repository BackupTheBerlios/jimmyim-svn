package jimmy.icq;

import jimmy.Contact;
import jimmy.Protocol;

public class ICQContact extends Contact {

	private short IcqID = 0;
	private short IcqGID = 0;
	
	public ICQContact(String userID, Protocol protocol, int status,
			String groupName, String screenName) {
		super(userID, protocol, status, groupName, screenName);
		// TODO Auto-generated constructor stub
	}

	public ICQContact(String userID, Protocol protocol) {
		super(userID, protocol);
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * Sets the ICQ contact ID for later use
	 * 
	 * @param i icq user id
	 */
	public void setIcqID(short i){this.IcqID=i;}
	
	/**
	 * Sets the ICQ contact group ID for later use
	 * 
	 * @param g icq contact GID
	 */
	public void setIcqGID(short g){this.IcqGID=g;}
	
	/**
	 * Get the ICQ ID
	 * 
	 * @return
	 */
	public short getIcqID() {return this.IcqID;}
	
	/**
	 * Get the ICQ GID
	 * 
	 * @return
	 */
	public short getIcqGID() {return this.IcqGID;}

}
