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
	
	public static void generalParse(String in, JabberProtocol protocol) {
		
	}
}
