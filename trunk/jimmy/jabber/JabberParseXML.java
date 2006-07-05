package jimmy.jabber;

import jimmy.*;
import java.util.Vector;

public class JabberParseXML {
	public static Vector parseContacts(String in, JabberProtocol protocol) {
		Vector contacts = new Vector();
		
		int x1, x2;
		int item1, item2;
		String cName, cJid, cGroup;	//contact's screen name, jabber id, group name
		Contact c;
		while (in.compareTo("</query></iq>")!=0) {	//until only query and iq
			item2 = in.indexOf("</item>");	//get the end of the current <item> block
			
			//<item ... name='...'
			x1 = in.indexOf("name='") + 6;
			if (x1 < item2) {
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("'");
				cName = in.substring(0, x2);
				in = in.substring(x2); item2 -= x2;
			} else
				cName = null;
			
			//jid='...'
			x1 = in.indexOf("jid='") + 5;
			if (x1 < item2) {
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("'");
				cJid = in.substring(0,x2);
				in = in.substring(x2); item2 -= x2;
			} else
				cJid = null;
			
			//<group>...</group>
			x1 = in.indexOf("<group>") + 5;
			if (x1 < item2) {
				in = in.substring(x1); item2 -= x1;
				x2 = in.indexOf("</group>");
				cGroup = in.substring(0,x2);
				in = in.substring(x2 + 15); item2 -= (x2 + 15); //cut off </group></item>
			} else
				cGroup = null;
			
			contacts.addElement(c = new Contact(cJid, protocol, 0, cGroup, cName));
		}
		
		return contacts;
	}
	
	public static boolean parseUserPass(String in) {
		if (in.compareTo("<iq type='result'/>")==0) return true;
		
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
		
		while (in.length() != 0) {
			x1 = in.indexOf("<") + 1;
			x2 = in.indexOf(" ");	
		
			type = in.substring(x1, x2); //get the first word in <> stanza
		
			if (type.compareTo("presence") == 0) {
				in = parsePresence(in, protocol, jimmy);
			}
			
		}
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

		//if the Contact wasn't found, trim the first presence stanza off and return
		if (c==null) {
			x2 = in.indexOf("/>");
			x22 = in.indexOf("</presence>");
			
			if ((x2 < x22) && (x2 != -1))
				in = in.substring(x2 + 2);
			else
				in = in.substring(x22 + 11);
			
			return in;
		}
		
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
		
		//contact is not off-line
		String tmp = getAttributeValue(in, "<presence ", "</presence>");
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
		
		//trim the input String - the first stanza was now processed - remove it
		in = in.substring(in.indexOf("</presence>") + 11);

		jimmy.changeContactStatus(c);
		return in;
	}
}
