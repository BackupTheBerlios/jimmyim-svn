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
   * Parse inbound xml for authentication 
   * 
   * @param x Received xml
   * @param protocol {@link JabberProtocol} instance
   * @param jimmy {@link ProtocolInteraction} instance
   */
  protected static void parseAuth(
      XmlNode x, 
      JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (x.contains("stream:stream") && 
        !x.contains("stream:features"))
    {
      protocol.isSasl_ = false;
      protocol.fullJid_ = protocol.getAccount().getUser() + "/JimmyIM";
      protocol.sh_.sendRequest(
          "<iq type='set'>" +
          "  <query xmlns='jabber:iq:auth'>" +
          "    <username>" + 
          protocol.getAccount().getUser().substring(0, protocol.getAccount().getUser().indexOf("@")) + 
          "</username>" +
          "    <password>" + protocol.getAccount().getPassword() + "</password>" +
          "    <resource>" + "JimmyIM" + "</resource>" +
          "  </query>" +
          "</iq>");
    }
    else
    {
      for (int i = 0; i < x.childs.size(); i++)
      {
        parse(
            (XmlNode)x.childs.elementAt(i), 
            protocol, 
            jimmy);
      }
    }
  }  
  
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
    if (x.name.equals("stream:stream") && 
        x.getFirstNode("stream:error") != null)
    {
      XmlNode err = (XmlNode)x.getFirstNode("stream:error").childs.elementAt(0);
      System.out.println(
          "[INFO-JABBER] Stream error!: " +
          err.name + "," + err.value);
      protocol.setAuthStatus(false);
    }
    else if (x.name.equals("stream:features"))
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
  
  /**
   * Parse the <<lit>iq</lit>> node and launch apropriate action
   * 
   * @param x {@link XmlNode} to parse
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
  private static void parseIq(
      XmlNode x, 
      JabberProtocol p, 
      ProtocolInteraction j)
  {
    String id = (String)x.attribs.get("id");
    
    if (id != null && (id.equals("sess") || 
        (x.attribs.get("type").equals("result") && 
            Utils.stringContains(id, "auth"))))
    {
      p.setAuthStatus(true);
      return;
    }
    
    XmlNode xNode = 
      x.childs.size() == 0 ? null : (XmlNode)x.childs.elementAt(0);
    if (xNode == null) return;
    
    if (x.contains("error"))
    {
      xNode = x.getFirstNode("error");
      System.out.println(
          "[INFO-JABBER] <IQ> error received: " +
          "Code=" + xNode.attribs.get("code") + " " +
          "Value=" + xNode.value);
    }
    
    if (xNode.name.equals("query"))
    {
      if (xNode.attribs.get("xmlns").equals("jabber:iq:roster"))
      {
        Vector v = xNode.getNodesByName("item");
        for (int i = 0; i < v.size(); i++)
        {
          xNode = (XmlNode)v.elementAt(i);
          
          if (xNode.attribs.get("subscription").equals("both") ||
              xNode.attribs.get("subscription").equals("to"))
          {
            updateContact(
                ((String)xNode.attribs.get("jid")).toLowerCase(), 
                (String)xNode.attribs.get("name"), 
                xNode.getFirstNode("group") == null ? null : xNode.getFirstNode("group").value,
                Byte.MAX_VALUE,
                null,
                p,
                j);
          }
        }
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
  
  /**
   * Parse the <<lit>presence</lit>> node and launch apropriate action
   * 
   * @param x {@link XmlNode} to parse
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
  private static void parsePresence(
      XmlNode x, 
      JabberProtocol p, 
      ProtocolInteraction j)
  {
    String fromFull = (String)x.attribs.get("from");
    String from = "";
    if (Utils.stringContains(fromFull, "/"))
      fromFull = fromFull.substring(0, fromFull.indexOf("/"));

    if (fromFull.equals(p.getAccount().getUser()))
      return;
    
    String type = (String)x.attribs.get("type");
    /* AUTOMATICALLY ACCEPTS INVITATION!!!!! */
    if (type != null && type.equals("subscribe"))
      p.sh_.sendRequest(
          "<presence to=\"" + fromFull + "\" type=\"subscribed\"/>" +
          "<presence from=\"" + p.fullJid_ + "\" to=\"" + fromFull + "\" type=\"subscribe\"/>");
    
    byte status = Byte.MAX_VALUE;
    type = 
        type != null ? 
          type : 
          (x.getFirstNode("show") != null ? 
              ((XmlNode)x.getFirstNode("show")).value :
              "");
    
    if(type.equals(""))
      status = Contact.ST_ONLINE;
    else if(type.equals("unavailable"))
      status = Contact.ST_OFFLINE;
    else if(type.equals("online"))
      status = Contact.ST_ONLINE;
    else if(type.equals("away"))
      status = Contact.ST_AWAY;
    else if(type.equals("xa"))
      status = Contact.ST_AWAY;
    else if(type.equals("dnd"))
      status = Contact.ST_BUSY;
    
    updateContact(
        from.equals("") ? fromFull : from, 
        null, 
        null, 
        status, 
        x.getFirstNode("status") == null ? null : ((XmlNode)x.getFirstNode("status")).value,
        p, 
        j);
  }
  
  /**
   * Parse the <<lit>message</lit>> node and launch apropriate action
   * 
   * @param x {@link XmlNode} to parse
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
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
  
  /**
   * Parse the <<lit>stream:features</lit>> node and launch apropriate action
   * 
   * @param x {@link XmlNode} to parse
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
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
  
  /**
   * Parse the <<lit>challenge</lit>> node and launch apropriate action
   * 
   * @param x {@link XmlNode} to parse
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
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
   * Updates contacts information. If this contact does not exist, it makes
   * a new instance of {@link Contact}
   * 
   * @param jid Contacts jid
   * @param name Contacts screen name
   * @param group Contacts group name
   * @param status Contacts status
   * @param statusText Contacts personal message
   * @param p {@link JabberProtocol} instance
   * @param j {@link ProtocolInteraction} instance
   */
  private static void updateContact(
      String jid, 
      String name, 
      String group,
      byte status,
      String statusText,
      JabberProtocol p,
      ProtocolInteraction j)
  {
    Contact c = p.getContact(jid);
    if (c == null)
    {
      c = new Contact(
          jid, 
          p, 
          status == Byte.MAX_VALUE ? Contact.ST_OFFLINE : status, 
          group, 
          name);
      c.setStatusMsg(statusText);
      
      p.addContact(c);
      j.addContact(c);
    }
    else
    {
      if (name != null)
        c.setScreenName(name);
      if (group != null) 
        c.setGroupName(group);
      c.setStatus(status == Byte.MAX_VALUE ? c.status() : status);
      c.setStatusMsg(statusText);
      j.changeContactStatus(c);
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
  protected static String getGTalkUserStatusXml(String fullJid)
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
