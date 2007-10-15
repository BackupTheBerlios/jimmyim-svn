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
 File: jimmy/JabberProtocolXML.java
 Version: pre-alpha  Date: 2006/05/18
 Author(s): Matevz Jekovec
 */

package jimmy.jabber;

import java.util.Vector;

import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.ProtocolInteraction;
import jimmy.util.Utils;

public class JabberParseXML
{
  /**
   * 
   * @param in
   * @return
   * @deprecated
   */
  public static boolean parseUserPass(String in)
  {
    if (in.indexOf("error") == -1)
      return true;

    return false;
  }

  public static String parseVarious(String in, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (in == null || in.trim().equals(""))
      return null;
    in = in.trim();
    in = removeXmlHeader(in);
    String res = "";

    while (true)
    {
      in = in.trim();
      if (in.equals(""))
        break;

      int endIndex;
      if (in.startsWith("<iq "))
      {
        System.out.println("[INFO] Inbound <IQ>");
        endIndex = getNextElementEndIndex(in, "iq");
        if (endIndex == -1)
          break;
        res += parseIq(in.substring(0, endIndex), protocol, jimmy);
      }
      else if (in.startsWith("<message "))
      {
        System.out.println("[INFO] Inbound <MESSAGE>");
        endIndex = getNextElementEndIndex(in, "message");
        if (endIndex == -1) break;
        res += parseMessage(in.substring(0, endIndex), protocol, jimmy);
      }
      else if (in.startsWith("<presence "))
      {
        System.out.println("[INFO] Inbound <PRESENCE>");
        endIndex = getNextElementEndIndex(in, "presence");
        if (endIndex == -1)
          break;
        res += parsePresence(in.substring(0, endIndex), protocol, jimmy);
      }
      else if (in.startsWith("<success "))
      {
        System.out.println("[INFO] Inbound <SUCCESS>");
        endIndex = getNextElementEndIndex(in, "success");
        if (endIndex == -1)
          break;
        //        TODO not implemented yet
      }
      else if (in.startsWith("<stream:stream "))
      {
        System.out.println("[INFO] Inbound <STREAM:STREAM>");
        endIndex = in.length();
        return getAttributeValue(in.substring(0, endIndex), "id");
      }
      else
      {
        System.out.println("[INFO] Inbound UNKNOWN");
        System.out.println(in);
        if (in.charAt(in.indexOf(">") - 1) == '/')
          endIndex = in.indexOf(">") + 1;
        else
        {
          String s = in.substring(0, in.indexOf(" "));
          endIndex = in.indexOf("</" + s.substring(1, s.length()) + ">")
              + s.length() + 3;
        }
      }

      in = in.substring(endIndex, in.length());
    }

    return res;
  }

  private static String parseIq(String s, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (s == null || s.equals(""))
      return null;

    if (Utils.stringContains(s, "jabber:iq:roster"))
    {
      Vector items = parseContacts(s, "item");
      for (int j = 0; j < items.size(); j++)
      {
        //TODO Parse group element <group> for group name
        Contact c = new Contact(getAttributeValue((String) items.elementAt(j),
            "jid"), protocol, Contact.ST_OFFLINE, null, getAttributeValue(
            (String) items.elementAt(j), "name"));
        protocol.addContact(c);
        jimmy.addContact(c);
      }
    }
    else if (Utils.stringContains(s, "jabber:iq:version"))
    {
      //TODO not implemented yet
    }
    else if (Utils.stringContains(s, "google:mail:notify"))
    {
      //TODO not implemented yet
    }
    else if (Utils.stringContains(s, "google:shared-status"))
    {
      //TODO not implemented yet
    }
    else if (Utils.stringContains(s, "urn:ietf:params:xml:ns:xmpp-bind"))
    {
      return getElementValue(s, "jid");
    }

    return "";
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
  private static String parseMessage(String in, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    String from = getAttributeValue(in, "from");
    from = from.substring(0, from.indexOf("/"));

    ChatSession cs = null;
    Contact c = protocol.getContact(from);

    if (c == null)
      c = new Contact(from, protocol);
    else
      cs = protocol.getChatSession(c);

    if (cs == null)
      cs = protocol.startChatSession(c);
    
    if (!Utils.stringContains(in, "<body"))
      return "";
      
    jimmy.msgRecieved(cs, c, getElementValue(in, "body"));

    //    return in.substring(in.indexOf("</message>") + 10);
    return "";
  }

  private static String parsePresence(String in, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    String from = getAttributeValue(in, "from");
    if (Utils.stringContains(from, "/"))
      from = from.substring(0, from.indexOf("/"));

    if (from.equals(protocol.getAccount().getUser()))
      return "";

    Contact c = protocol.getContact(from);
    if (c == null)
      c = new Contact(from, protocol, Contact.ST_OFFLINE, null, from);

    String type = getAttributeValue(in, "type");
    if (type.equals("unavailable"))
    {
      c.setStatus(Contact.ST_OFFLINE);
      jimmy.changeContactStatus(c);
    }
    else if (type.equals("unsubscribe"))
    {
      //a contact removed you from his list, keep the contact visible, but set it offline
      c.setStatus(Contact.ST_OFFLINE);
      jimmy.changeContactStatus(c);
    }
    else if (type.equals("unsubscribed"))
    {
      //a contact removed you from his list, keep the contact visible, but set it offline
      c.setStatus(Contact.ST_OFFLINE);
      jimmy.changeContactStatus(c);
    }
    else if (type.equals("subscribe"))
    {
      //a contact added you to his list
      allowContact(c, protocol, jimmy); //authorize contact to add
    }
    else if (type.equals("subscribed"))
    {
      //contact has added you to his list, set the contact's list
      protocol.updateContactProperties(c); //authorize contact to add 

      //Let them know about our status "online"
      protocol.getServerHandler().sendRequest("<presence type=\"available\"/>");
    }
    else
    {
      //contact is not off-line
      String show = getElementValue(in, "show");
      if (show.equals(""))
        c.setStatus(Contact.ST_ONLINE);
      else if ((show.compareTo("away") == 0) || (show.compareTo("xa") == 0))
        c.setStatus(Contact.ST_AWAY);
      else if (show.compareTo("dnd") == 0)
        c.setStatus(Contact.ST_BUSY);

      //does a contact also include a message?
      String statusMsg = getElementValue(in, "status");
      c.setStatusMsg(statusMsg);

      jimmy.changeContactStatus(c);
    }

    return "";
  }

  private static Vector parseContacts(String in, String name)
  {
    in = in.substring(in.indexOf("<" + name), in.length());

    Vector v = new Vector();
    while (true)
    {
      in = in.trim();
      if (in.equals("") || !Utils.stringContains(in, name))
        break;

      int endIndex;

      if (in.charAt(in.indexOf(">") - 1) == '/')
        endIndex = in.indexOf(">") + 1;
      else
        endIndex = in.indexOf("</" + name + ">") + name.length() + 3;

      v.addElement(in.substring(0, endIndex));
      in = in.substring(endIndex, in.length());
    }

    return v;
  }

  private static String parseC(String in, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    ///TODO Currently, this method only removes the <c> stanza
    return "";
  }

  private static String parseX(String in, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    ///TODO Currently, this method only removes the <x> stanza
    return "";
  }

  private static void allowContact(Contact c, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    String oString = "<presence from='" + protocol.getAccount().getUser()
        + "' to='" + c.userID() + "' type='subscribed'/>";
    protocol.getServerHandler().sendRequest(oString);

    if (protocol.getContact(c.userID()) == null)
    {
      jimmy.addContact(c);
      protocol.addContact(c);
    }
  }

  private static String getElementValue(String in, String name)
  {
    String val = "";

    if (Utils.stringContains(in, name + "/>"))
      return "";

    if (Utils.stringContains(in, name))
      val += in.substring(in.indexOf("<" + name + ">") + name.length() + 2, in
          .indexOf("</" + name + ">"));

    return val;
  }

  private static String getAttributeValue(String in, String name)
  {
    String val = "";

    if (Utils.stringContains(in, name))
      val += in.substring(in.indexOf(name) + name.length() + 2, in.indexOf(
          "\"", in.indexOf(name) + name.length() + 2));

    return val;
  }

  private static String removeXmlHeader(String in)
  {
    if (in.startsWith("<?"))
      return in.substring(in.indexOf("?>") + 2, in.length());
    return in;
  }

  private static int getNextElementEndIndex(String in, String name)
  {
    if (in.charAt(in.indexOf(">") - 1) == '/')
      return in.indexOf(">") + 1;
    else if (Utils.stringContains(in, "</" + name + ">"))
      return in.indexOf("</" + name + ">") + name.length() + 3;
    else
      return -1;
  }
  
  protected static String getAuthXml(String token)
  {
    return 
      "<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" " +
      "mechanism=\"X-GOOGLE-TOKEN\">" + token + "</auth>";
  }
  
  protected static String getOpenStreamXml(String server)
  {
    return 
      "<?xml version=\"1.0\"?>" +
      "<stream:stream xmlns:stream=\"http://etherx.jabber.org/streams\" " +
      "xmlns=\"jabber:client\" to=\"" + server + "\" version=\"1.0\">";
  }
  
  protected static String getBindToServerXml(String resourceName)
  {
    return 
      "<iq type=\"set\" id=\"bind\">" + 
      "  <bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
      "    <resource>" + resourceName + "</resource>" +
      "  </bind>" +
      "</iq>";
  }
  
  protected static String getCreateSessionXml(String server)
  {
    return 
      "<iq to=\"" + server + "\" type=\"set\" id=\"sess_1\">" +
      "  <session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/>" +
      "</iq>";
  }
  
  /**
   * Calculates string representation of initial user status
   * Sets important Google settings
   * Informs Google Talk that we want to use GTalk features 
   */
  protected static String getGTalkOptionsXml()
  {
    return 
      "<iq type=\"get\" id=\"6\">" +
      "  <query xmlns=\"google:relay\"/>" +
      "</iq>";
  }
  
  /**
   * Sends mail notification request to GTalk server
   */
  protected static String getGTalkMailNotificationReqXml(String fullJid)
  {
    return 
      "<iq type=\"set\" to=\"" + fullJid + "\" id=\"15\">" +
      "  <usersetting xmlns=\"google:setting\">" +
      "    <autoacceptrequests value=\"false\"/>" +
      "    <mailnotifications value=\"true\"/>" +
      "  </usersetting>" +
      "</iq>";
  }
  
  protected static String getUserStatusXml(String fullJid)
  {
    return 
      "<iq type=\"get\" id=\"23\">" +
      "  <query xmlns=\"google:mail:notify\" " +
      "    q=\"(!label:^s) (!label:^k) ((label:^u) (label:^i) (!label:^vm))\"/>" +
      "</iq>" +
      "<iq type=\"get\" to=\"" + fullJid + "\" id=\"21\">" +
      "  <query xmlns=\"google:shared-status\"/>" +
      "</iq>";
  }
  
  protected static String getRosterXml()
  {
    return 
      "<iq type=\"get\" id=\"roster\">" +
      "  <query xmlns=\"jabber:iq:roster\"/>" +
      "</iq>";
  }
  
  protected static String getGTalkPresence()
  {
    return "<presence><show></show><status></status></presence>";
  }
  
  protected static String getWelcomeMsgXml(String server, String stream)
  {
    return 
      "<?xml version='1.0'?>" +
      "<stream:stream to='" + server + "' xmlns='jabber:client' " +
      "  xmlns:stream='" + stream + "'>";
  }
}
