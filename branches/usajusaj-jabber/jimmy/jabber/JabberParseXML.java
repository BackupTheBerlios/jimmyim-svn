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
 Version: beta  Date: 2006/05/18
 Author(s): Matevz Jekovec, Matej Usaj
 */

package jimmy.jabber;

import java.io.DataInputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpsConnection;

import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.ProtocolInteraction;
import jimmy.util.MD5;
import jimmy.util.Utils;
import jimmy.util.XmlNode;

/**
 * Jabbers xml manipulation class
 * 
 * @author Matevz Jekovec
 * @author Matej Usaj
 */
public class JabberParseXML
{
  /**
   * Parse inbound xml and execute apropriate action
   * 
   * @param x Received xml
   * @param protocol {@link JabberProtocol} instance
   * @param jimmy {@link ProtocolInteraction} instance
   */
  protected static void parse(
      XmlNode x, 
      JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (x.name.equals("stream:features"))
    {
      parseStreamFeatures(x, protocol, jimmy);
    }
    else if (x.name.equals("compressed"))
    {
      //TODO Implement zlib connection
//      stream.setZlibCompression();
//      try {
//        stream.initiateStream(account.getServer(), true, SR.MS_XMLLANG);
//      } catch (IOException ex) {
//      }
    }
    /* Reply to DIGEST-MD5 challenges */
    else if (x.name.equals("challenge"))
    {
      parseChallenge(x, protocol, jimmy);
    }
    else if (x.name.equals("failure"))
    {
      System.out.println("[INFO-JABBER] Failed");
      protocol.setAuthStatus(false);
    }
    else if (x.name.equals("success"))
    {
      System.out.println("[INFO-JABBER] Auth success");
      protocol.sh_.sendRequest(getOpenStreamXml(protocol.domain_));
    }
    else if (x.name.equals("iq"))
    {
      parseIq(x, protocol, jimmy);
    }
    else if (x.name.equals("presence"))
    {
      parsePresence(x, protocol, jimmy);
    }
    else if (x.name.equals("message"))
    {
      parseMessage(x, protocol, jimmy);
    }
  }
  
  private static void parseIq(
      XmlNode x, 
      JabberProtocol p, 
      ProtocolInteraction j)
  {
    String from = (String)x.attribs.get("from");
    String to = (String)x.attribs.get("to");
    String type = (String)x.attribs.get("type");
    String id = (String)x.attribs.get("id");
    
    if (id != null && id.equals("sess"))
    {
      p.setAuthStatus(true);
      return;
    }
    
    XmlNode xNode = (XmlNode)x.childs.elementAt(0);
    if (xNode == null) return;
    
    if (xNode.name.equals("error"))
    {
      System.out.println(
          "[INFO-JABBER] <IQ> error received: " +
          "Code=" + xNode.attribs.get("code") + " " +
          "Value=" + xNode.value);
    }
    else if (xNode.name.equals("query"))
    {
      if (xNode.attribs.get("xmlns").equals("jabber:iq:roster"))
      {
        Vector v = xNode.getNodesByName("item");
        for (int i = 0; i < v.size(); i++)
        {
          xNode = (XmlNode)v.elementAt(i);
          Contact c = new Contact(
              ((String)xNode.attribs.get("jid")).toLowerCase(), 
              p, 
              Contact.ST_OFFLINE, 
              null, 
              (String)xNode.attribs.get("name"));
          if (xNode.contains("group"))
            c.setGroupName(xNode.getFirstNode("group").value);
          
          p.addContact(c);
          j.addContact(c);
        }
      }
      else if (xNode.attribs.get("xmlns").equals("jabber:iq:register"))
      {
        
      }
    }
    else if (xNode.name.equals("bind"))
    {
      System.out.println("[INFO-JABBER] Send open session request");
      p.fullJid_ = xNode.getFirstNode("jid").value;
      p.sh_.sendRequest(
          "<iq type=\"set\" id=\"sess\">" + 
          "  <session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/>" +
          "</iq>");
    }
  }
  
  private static void parsePresence(
      XmlNode x, 
      JabberProtocol p, 
      ProtocolInteraction j)
  {
    String from = (String)x.attribs.get("from");
    if (Utils.stringContains(from, "/"))
      from = from.substring(0, from.indexOf("/"));

    if (from.equals(p.getAccount().getUser()))
      return;

    Contact c = p.getContact(from.toLowerCase());
    if (c == null)
    {
      c = new Contact(from, p, Contact.ST_OFFLINE, null, from);
      p.addContact(c);
      j.addContact(c);
    }

    String type = (String)x.attribs.get("type");
    if (type == null)
    {
      //contact is not off-line
      XmlNode xNode = x.getFirstNode("show");
      String show = "";
      if (xNode != null)
        show = xNode.value;
        
      if (show.equals(""))
        c.setStatus(Contact.ST_ONLINE);
      else if ((show.compareTo("away") == 0) || (show.compareTo("xa") == 0))
        c.setStatus(Contact.ST_AWAY);
      else if (show.compareTo("dnd") == 0)
        c.setStatus(Contact.ST_BUSY);

      //does a contact also include a message?
      xNode = x.getFirstNode("status");
      if (xNode != null)
        c.setStatusMsg(xNode.value);

      j.changeContactStatus(c);
    }
    else if (type.equals("unavailable"))
    {
      c.setStatus(Contact.ST_OFFLINE);
      j.changeContactStatus(c);
    }
    else if (type.equals("unsubscribe"))
    {
      //a contact removed you from his list, keep the contact visible, but set it offline
      c.setStatus(Contact.ST_OFFLINE);
      j.changeContactStatus(c);
    }
    else if (type.equals("unsubscribed"))
    {
      //a contact removed you from his list, keep the contact visible, but set it offline
      c.setStatus(Contact.ST_OFFLINE);
      j.changeContactStatus(c);
    }
    else if (type.equals("subscribe"))
    {
      //a contact added you to his list
      allowContact(c, p, j); //authorize contact to add
    }
    else if (type.equals("subscribed"))
    {
      //contact has added you to his list, set the contact's list
      p.updateContactProperties(c); //authorize contact to add 

      //Let them know about our status "online"
      p.sh_.sendRequest("<presence type=\"available\"/>");
    }
  }
  
  private static void parseMessage(
      XmlNode x,
      JabberProtocol p,
      ProtocolInteraction j)
  {
    String from = (String)x.attribs.get("from");
    if (Utils.stringContains(from, "/"))
      from = from.substring(0, from.indexOf("/"));

    if (from.equals(p.getAccount().getUser()))
      return;

    ChatSession cs = null;
    Contact c = p.getContact(from);

    if (c == null)
      c = new Contact(from, p);
    else
      cs = p.getChatSession(c);

    if (cs == null)
      cs = p.startChatSession(c);
    
    XmlNode xNode = x.getFirstNode("body");
    if (xNode == null)
      return;
      
    j.msgRecieved(cs, c, xNode.value);
  }
  
  private static void parseStreamFeatures(
      XmlNode x,
      JabberProtocol p,
      ProtocolInteraction j)
  {
    /* Check for stream compression method */
    XmlNode x2 = x.getFirstNode("compression");
    if (x2 != null &&
        x2.getFirstNode("method") != null &&
        x2.getFirstNode("method").value.equals("zlib"))
    {
      System.out.println("[INFO-JABBER] Using zlib(NOT IMPLEMENTED YET)");
//TODO ZLIB compression
//      protocol.sh_.sendRequest("<compress xmlns=\"http://jabber.org/protocol/compress\"><method>zlib</method></compress>");
    }
    
    /* Check for authentication mechanisms */
    x2 = x.getFirstNode("mechanisms");
    if (x2 != null &&
        x2.getFirstNode("mechanism") != null)
    {
      String mechanism = x2.getFirstNode("mechanism").value;

      String auth = 
        "<auth xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\" ";
      
      /* DIGEST-MD5 authentication */
      if (mechanism.equals("DIGEST-MD5"))
      {
        System.out.println("[INFO-JABBER] Using DIGEST-MD5");
        auth += "mechanism=\"DIGEST-MD5\"/>";
      }
      /* X-GOOGLE-TOKEN authentication */
      else if (mechanism.equals("X-GOOGLE-TOKEN"))
      {
        System.out.println("[INFO-JABBER] Using X-GOOGLE-TOKEN");
        p.isGTalk_ = true;
        auth += "mechanism=\"X-GOOGLE-TOKEN\">";
        auth += getGoogleToken(
            p.getAccount().getUser().substring(
                0, 
                p.getAccount().getUser().indexOf("@")),
            p.getAccount().getPassword());
        auth += "</auth>";
      }
      /* PLAIN authentication */
      else if (mechanism.equals("PLAIN"))
      {
        System.out.println("[INFO-JABBER] Using PLAIN");
        auth += "mechanism=\"PLAIN\">";
        auth += MD5.toBase64((p.getAccount().getUser()
            + "\0" + p.getAccount().getUser().substring(
                0, 
                p.getAccount().getUser().indexOf("@"))
            + "\0" + p.getAccount().getPassword()).getBytes());
        auth += "</auth>";
      }
      /* Unknown authentication method */
      else
      {
        p.setAuthStatus(false);
      }
      
      p.sh_.sendRequest(auth);
    }
    
    /* Check for resource bind */
    x2 = x.getFirstNode("bind");
    if (x2 != null)
    {
      System.out.println("[INFO-JABBER] Send bind request");
      p.sh_.sendRequest(
          "<iq type=\"set\" id=\"bind\">" + 
          "  <bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">" +
          "    <resource>" + "JimmyIM" + "</resource>" +
          "  </bind>" +
          "</iq>");
    }
  }
  
  private static void parseChallenge(
      XmlNode x,
      JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    System.out.println("[INFO-JABBER] Received challenge");
    String challenge = JabberMD5.decodeBase64(x.value);
    String resp = "<response xmlns=\"urn:ietf:params:xml:ns:xmpp-sasl\">";
    
    int nonceIndex = challenge.indexOf("nonce=");
    if (nonceIndex>=0) 
    {
      nonceIndex += 7;
      String nonce = challenge.substring(nonceIndex, challenge.indexOf('\"', nonceIndex));
      String cnonce = "123456789abcd";
      
      String t = responseMd5Digest(
          protocol.getAccount().getUser().substring(
              0, 
              protocol.getAccount().getUser().indexOf("@")),
          protocol.getAccount().getPassword(), 
          protocol.getAccount().getServer(), 
          "xmpp/" + protocol.getAccount().getServer(), 
          nonce, 
          cnonce);
      resp += t;
    }
    
    resp += "</response>";
    protocol.sh_.sendRequest(resp);
  }

  /**
   * This routine generates MD5-DIGEST response via SASL specification
   * (From BOMBUS project)
   * 
   * @param user
   * @param pass
   * @param realm
   * @param digest_uri
   * @param nonce
   * @param cnonce
   * @return
   */
  private static String responseMd5Digest(String user, String pass,
      String realm, String digestUri, String nonce, String cnonce)
  {
    JabberMD5 hUserRealmPass = new JabberMD5();
    hUserRealmPass.init();
    hUserRealmPass.updateASCII(user);
    hUserRealmPass.update((byte) ':');
    hUserRealmPass.updateASCII(realm);
    hUserRealmPass.update((byte) ':');
    hUserRealmPass.updateASCII(pass);
    hUserRealmPass.finish();

    JabberMD5 hA1 = new JabberMD5();
    hA1.init();
    hA1.update(hUserRealmPass.getDigestBits());
    hA1.update((byte) ':');
    hA1.updateASCII(nonce);
    hA1.update((byte) ':');
    hA1.updateASCII(cnonce);
    hA1.finish();

    JabberMD5 hA2 = new JabberMD5();
    hA2.init();
    hA2.updateASCII("AUTHENTICATE:");
    hA2.updateASCII(digestUri);
    hA2.finish();

    JabberMD5 hResp = new JabberMD5();
    hResp.init();
    hResp.updateASCII(hA1.getDigestHex());
    hResp.update((byte) ':');
    hResp.updateASCII(nonce);
    hResp.updateASCII(":00000001:");
    hResp.updateASCII(cnonce);
    hResp.updateASCII(":auth:");
    hResp.updateASCII(hA2.getDigestHex());
    hResp.finish();

    String out = "username=\"" + user + "\",realm=\"" + realm + "\","
        + "nonce=\"" + nonce + "\",nc=00000001,cnonce=\"" + cnonce + "\","
        + "qop=auth,digest-uri=\"" + digestUri + "\"," + "response=\""
        + hResp.getDigestHex() + "\",charset=utf-8";
    String resp = MD5.toBase64(out.getBytes());

    return resp;
  }
  
  /**
   * Generates X-GOOGLE-TOKEN response by communication with
   * http://www.google.com
   * (From mGTalk project)
   * 
   * @param userName
   * @param passwd
   * @return
   */
  private static String getGoogleToken(String userName, String passwd)
  {
    String first = "Email=" + userName + "&Passwd=" + passwd
        + "&PersistentCookie=false&source=googletalk";
    try
    {
      HttpsConnection c = (HttpsConnection) Connector
          .open("https://www.google.com:443/accounts/ClientAuth?" + first);
      System.out.println("[INFO-JABBER] Connecting to www.google.com");
      DataInputStream dis = c.openDataInputStream();
      String str = readLine(dis);
      String SID = "";
      String LSID = "";
      if (str.startsWith("SID="))
      {
        SID = str.substring(4, str.length());
        str = readLine(dis);
        LSID = str.substring(5, str.length());
        first = "SID=" + SID + "&LSID=" + LSID + "&service=mail&Session=true";
        dis.close();
        c.close();
        c = (HttpsConnection) Connector
            .open("https://www.google.com:443/accounts/IssueAuthToken?" + first);
        System.out.println("[INFO-JABBER] Next www.google.com connection");
        dis = c.openDataInputStream();
        str = readLine(dis);
        String token = MD5.toBase64(new String("\0" + userName + "\0" + str)
            .getBytes());
        dis.close();
        c.close();
        return token;
      }

      throw new Exception("Invalid response");
    }
    catch (Exception ex)
    {
      ex.printStackTrace();
      System.out.println("EX: " + ex.toString());
    }
    return "";
  }
  
  /**
   * Service routine for google token
   * (From mGTalk project)
   * 
   * @param dis
   * @return
   */
  private static String readLine(DataInputStream dis)
  {
    String s = "";
    byte ch = 0;
    try {
      while ((ch = dis.readByte()) != -1)
      {
        if (ch == '\n')
          return s;
        s += (char) ch;
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return s;
  }
  
  /**
   * Subscribe a contact
   * 
   * @param c {@link Contact} to subscribe
   * @param protocol {@link JabberProtocol} instance
   * @param jimmy {@link ProtocolInteraction} instance
   */
  private static void allowContact(Contact c, JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    protocol.sh_.sendRequest(
        "<presence " +
        " from='" + protocol.fullJid_ + "'" +
        " to='" + c.userID() + "'" +
        " type='subscribed'/>");

    if (protocol.getContact(c.userID()) == null)
    {
      jimmy.addContact(c);
      protocol.addContact(c);
    }
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
  
  /**
   * Get presence request for GTalk
   */
  protected static String getGTalkPresence()
  {
    return "<presence><show></show><status></status></presence>";
  }
  
  /**
   * Get user status request
   */
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
  
  /**
   * Get roster request
   */
  protected static String getRosterXml()
  {
    return 
      "<iq type=\"get\" id=\"roster\">" +
      "  <query xmlns=\"jabber:iq:roster\"/>" +
      "</iq>";
  }
  
  /**
   * Get open stream request
   */
  protected static String getOpenStreamXml(String server)
  {
    return 
      "<?xml version=\"1.0\"?>" +
      "<stream:stream to=\"" + server + "\" xmlns=\"jabber:client\" " +
      "  xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\">";
  }
}
