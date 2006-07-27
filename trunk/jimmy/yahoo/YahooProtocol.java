package jimmy.yahoo;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;

public class YahooProtocol extends Protocol {

	public YahooProtocol(ProtocolInteraction jimmy) {
		super(jimmy);
		this.protocolType_ = YAHOO;
	}
	
	public boolean login(Account account) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean login(String username, String passwd) {
		// TODO Auto-generated method stub
		return false;
	}

	public void logout() {
		// TODO Auto-generated method stub

	}

	public ChatSession startChatSession(Contact user) {
		// TODO Auto-generated method stub
		return null;
	}

	public void addContact(Contact c) {
		// TODO Auto-generated method stub

	}

	public void sendMsg(String msg, ChatSession session) {
		// TODO Auto-generated method stub

	}

	public void sendMsg(String msg, Vector contactsList, ChatSession session) {
		// TODO Auto-generated method stub

	}

	public void run() {
		// TODO Auto-generated method stub

	}

}
