package jimmy.jabber;

import jimmy.*;
import java.util.Vector;

public class JabberParseXML {
	public static Vector parseContacts(String in, JabberProtocol protocol) {
		Vector contacts = new Vector();
		
		int x1, x2;
		int item2;
		String cName, cJid, cGroup;	//contact's screen name, jabber id, group name
		while (in.indexOf("<item") != -1) {	//until only query and iq
			in = in.substring(in.indexOf("<item"));	//trim anything before the first <item occurance

			if (in.indexOf("<item", 1) != -1)
				item2 = in.indexOf("<item", 1);	//get the start of the next <item block, if any
			else
				item2 = in.length();
			
			//<item ... name='...'
			x1 = in.indexOf("name='");
			if ((x1 != -1) && (x1 < item2)) {
				x1 += 6;	//the end of name='
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("'");
				cName = in.substring(0, x2);
				in = in.substring(x2 + 1); item2 -= (x2+1);
			} else
				cName = null;
			
			//jid='...'
			x1 = in.indexOf("jid='");
			if ((x1 != -1) && (x1 < item2)) {
				x1 += 5;	//the end of jid='
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("'");
				cJid = in.substring(0,x2);
				in = in.substring(x2 + 1); item2 -= (x2+1);
			} else
				cJid = null;
			
			//<group>...</group>
			x1 = in.indexOf("<group>");
			if ((x1 != -1) && (x1 < item2)) {
				x1 += 7;	//the end of <group>
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("</group>");
				cGroup = in.substring(0,x2);
				in = in.substring(x2+8); item2 -= (x2+8); //cut off </group></item>
			} else
				cGroup = null;
			
			contacts.addElement(new Contact(cJid, protocol, 0, cGroup, cName));
		}

		return contacts;
	}
	
	public static boolean parseUserPass(String in) {
		if (in.indexOf("error")==-1) return true;
		
		return false;
	}
	
	/**
	 * This method parses the given string and forward according to the XML stanza type,
	 * the String to the correct submethod.
	 * 
	 * Different submethods parse the given String then and trim it every time.
	 * Submethods are called as long as the given String isn't trimmed to length==0.
	 * 
	 * @param in Given String input which was received from the Jabber server
	 * @param protocol Reference to the Jabber protocol (needed for contacts list and other details)
	 */
	public static void genericParse(String in, JabberProtocol protocol, ProtocolInteraction jimmy) {
		int x1, x2;
		String type;
		
		if (in==null) return;
		
		System.out.println("genericParse:\n" + in);
		
		while (in.length() != 0) {
			System.out.println(in);
			x1 = in.indexOf("<") + 1;
			x2 = in.indexOf(" ");	

                        type = in.substring(x1, x2); //get the first word in <> stanza
			
			if (type.compareTo("presence") == 0) {
				in = parsePresence(in, protocol, jimmy);
			} else if (type.compareTo("message") == 0) {
				in = parseMessage(in, protocol, jimmy);
			} else if (type.compareTo("iq") == 0) {
				in = parseIq(in, protocol, jimmy);
			} else if (type.compareTo("c") == 0) {
				in = parseC(in, protocol, jimmy);
			} else
				break;
		}
	}
	
	/**
	 * Parse the first message stanza of the given XML data.
	 * Call the appropriate methods for the interaction with the program and UI.
	 * Cut off the processed stanza.
	 * 
	 * @param in An XML stanzas needed to be processed.
	 * @param protocol Pointer to the Jabber Protocol used.
	 * @param jimmy Pointer to the main Jimmy application used.
	 * @return The initial string without the processed stanza.
	 */
	private static String parseMessage(String in, JabberProtocol protocol, ProtocolInteraction jimmy) {
		String from = in.substring(in.indexOf("from='")+6, in.indexOf("/", in.indexOf("from='")+6));
		ChatSession cs = null;
		Contact c = protocol.getContact(from);
		
		if (c == null) {
			c = new Contact(from, protocol);
		} else
			cs = protocol.getChatSession(c);

                if (cs==null)
                    cs = protocol.startChatSession(c);
		
		String msg = getAttributeValue(in, "<body>", "</body>");
		
		jimmy.msgRecieved(cs, c, msg);
		
		return in.substring(in.indexOf("</message>") + 10);
	}
	
	/**
	 * Return a block surrounded by the given left and right String elements in the given String.
	 * Returns null, if the pattern wasn't found.
	 * 
	 * @param in The whole String
	 * @param left Left surrounding value
	 * @param right Right surrounding value
	 * @return The block between left and right surrounding String in String in. Returns null, if the pattern wasn't found.
	 */
	private static String getAttributeValue(String in, String left, String right) {
		if (in==null) {
			return null;
		}
		if (in.indexOf(left) == -1) {
			return null;
		}
		if (in.indexOf(right, in.indexOf(left)) == -1) {
			return null;
		}
		
		return in.substring(in.indexOf(left) + left.length(), in.indexOf(right, in.indexOf(left) + left.length()));
	}

	private static String parsePresence(String in, JabberProtocol protocol, ProtocolInteraction jimmy) {
		int x1, x2, x22;
		String from;

		x1 = in.indexOf("from='") + 6;
		//jid string, which contact is this about ends at / or ', whichever comes first 
		if ( ((x2 = in.indexOf("/", x1))  < (x22 = in.indexOf("'", x1))) &&
		      (x2 != -1) ) {
			from = in.substring(x1, x2);
		} else {
			from = in.substring(x1, x22);
		}
		
		//find the Contact from the contacts list
		Contact c = protocol.getContact(from);

		//if the Contact wasn't found, create it automatically
		if (c==null)
			c = new Contact(from, protocol);
		
		//contact was found, proceed
		//checking, if the contact became off-line
		x2 = in.indexOf("type='unavailable'/>");
		x22 = in.indexOf(">");

		if ((x2 < x22) && (x2 != -1)) {
			c.setStatus(Contact.ST_OFFLINE);
			jimmy.changeContactStatus(c);
			in = in.substring(x2 + 20);
			return in;
		}
		
		//a contact added you to his list
		x2 = in.indexOf("type='subscribe'/>");
		if ((x2 < x22) && (x2 != -1)) {
			allowContact(c, protocol, jimmy);	//authorize contact to add 
			in = in.substring(x2 + 18);
			return in;
		}
		
		//contact is not off-line
		String tmp = getAttributeValue(in, "<presence", "</presence>");
		String show = getAttributeValue(tmp, "<show>", "</show>");
		if (show == null)
			c.setStatus(Contact.ST_ONLINE);
		else if ((show.compareTo("away")==0) || (show.compareTo("xa")==0))
			c.setStatus(Contact.ST_AWAY);
		else if (show.compareTo("dnd")==0)
			c.setStatus(Contact.ST_BUSY);
		
		//does a contact also include a message?
		String statusMsg = getAttributeValue(tmp, "<status>", "</status>");
		c.setStatusMsg(statusMsg);

		jimmy.changeContactStatus(c);
		
		if (in.indexOf("<presence", 1) != -1)
			in = in.substring(in.indexOf("<presence", 1));
		else
			in = "";

		return in;
	}
	
	static void allowContact(Contact c, JabberProtocol protocol, ProtocolInteraction jimmy) {
		String oString = "<presence from='" + protocol.getAccount().getUser() + "' to='" + c.userID() + "' type='subscribed'/>";
		protocol.getServerHandler().sendRequest(oString);
		jimmy.addContact(c);
	}
	
	static String parseIq(String in, JabberProtocol protocol, ProtocolInteraction jimmy) {
		///TODO Currently, this method only removes the <iq> stanza
		return in.substring(in.indexOf("</iq>")+5);
	}

	static String parseC(String in, JabberProtocol protocol, ProtocolInteraction jimmy) {
		///TODO Currently, this method only removes the <c> stanza
		return in.substring(in.indexOf("/>")+2);
	}
}
