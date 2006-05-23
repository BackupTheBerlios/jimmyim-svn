/**
 * 
 */
package jimmy;

import java.util.Vector;
import java.io.*;

/**
 * @author dejan
 *
 */
public class ICQProtocol extends Protocol{

	private String user;
	private String pass;
	
	public ICQProtocol(){
		this.connected_ = false;
	}
	
	public boolean login(Account account) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean login(String username, String passwd) {
		// TODO Auto-generated method stub
		this.user = username;
		this.pass = passwd;
		return false;
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

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub
		
	}

}
