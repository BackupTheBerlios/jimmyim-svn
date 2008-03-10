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

package org.jimmy.android.jabber;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.Vector;

import javax.net.ssl.HttpsURLConnection;

import org.jimmy.android.data.ChatSession;
import org.jimmy.android.data.Contact;
import org.jimmy.android.data.ProtocolInteraction;
import org.jimmy.android.util.MD5;
import org.jimmy.android.util.Utils;
import org.jimmy.android.util.XmlNode;

import android.net.SSLSocket;
import android.net.SSLSocketFactory;

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
  protected static final void parseAuth(
      XmlNode x, 
      JabberProtocol protocol,
      ProtocolInteraction jimmy)
  {
    System.out.println("[JABBER IN XML]:\n" + x.toString());

    if (x.contains("stream:stream") && 
        !x.contains("stream:features"))
    {
      protocol.isSasl_ = false;
      protocol.fullJid_ = protocol.getAccount().getUser() + "/JimmyIM";
      protocol.sendRequest(new StringBuffer()
        .append("<iq type='set'>")
        .append(  "<query xmlns='jabber:iq:auth'>")
        .append(    "<username>") 
        .append(protocol.getAccount().getUser().substring(0, protocol.getAccount().getUser().indexOf("@"))) 
        .append(    "</username>")
        .append(    "<password>").append(protocol.getAccount().getPassword()).append("</password>")
        .append(    "<resource>").append("JimmyIM").append("</resource>")
        .append(  "</query>")
        .append("</iq>").toString());
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
      //TODO Activate zlib connection
//      stream.setZlibCompression();
//      try {
//        stream.initiateStream(account.getServer(), true, SR.MS_XMLLANG);
//      } catch (IOException ex) {
//      }
    }
    /* Reply to DIGEST-MD5 challenges */
    else if (x.name.equals("challenge"))
    {
      parseChallenge(x, protocol);
    }
    else if (x.name.equals("failure"))
    {
      System.out.println("[INFO-JABBER] Failed");
      protocol.setAuthStatus(false);
    }
    else if (x.name.equals("success"))
    {
      System.out.println("[INFO-JABBER] Auth success");
      protocol.sendRequest(getOpenStreamXml(protocol.domain_));
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
          "Value=" + xNode.getFirstNode("text").value);
      if (x.contains("query") && x.getFirstNode("query").attribs.get("xmlns").equals("jabber:iq:auth"))
        p.setAuthStatus(false);
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
      p.sendRequest(new StringBuffer()
        .append("<iq type=\"set\" id=\"sess\">") 
        .append(  "<session xmlns=\"urn:ietf:params:xml:ns:xmpp-session\"/>")
        .append("</iq>").toString());
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
    String type = (String)x.attribs.get("type");
    if (type != null && type.equals("error"))
    {
      XmlNode xNode = x.getFirstNode("error");
      System.out.println(
          "[INFO-JABBER] <IQ> error received: " +
          "Code=" + xNode.attribs.get("code") + " " +
          "Value=" + xNode.getFirstNode("text").value);
      return;
    }
    
    String fromFull = (String)x.attribs.get("from");
    String from = "";
    if (Utils.stringContains(fromFull, "/"))
      fromFull = fromFull.substring(0, fromFull.indexOf("/"));

    if (fromFull.equals(p.getAccount().getUser()))
      return;
    
    /* AUTOMATICALLY ACCEPTS INVITATION!!!!! */
    if (type != null && type.equals("subscribe"))
      p.sendRequest(new StringBuffer()
        .append("<presence to=\"").append(fromFull).append("\" type=\"subscribed\"/>")
        .append("<presence from=\"").append(p.fullJid_).append("\" to=\"").append(fromFull).append("\" type=\"subscribe\"/>").toString());
    
    byte status = Byte.MAX_VALUE;
    type = 
        type != null ? 
          type : 
          (x.getFirstNode("show") != null ? 
              x.getFirstNode("show").value :
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
        x.getFirstNode("status") == null ? null : x.getFirstNode("status").value,
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
//TODO ZLIB compression algorytm implementation
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
        int endI = p.getAccount().getUser().indexOf("@");
        if (endI == -1) endI = p.getAccount().getUser().length();
        auth += "mechanism=\"X-GOOGLE-TOKEN\">";
        auth += getGoogleToken(
            p.getAccount().getUser().substring(
                0, 
                endI),
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
      
      p.sendRequest(auth);
    }
    
    /* Check for resource bind */
    x2 = x.getFirstNode("bind");
    if (x2 != null)
    {
      System.out.println("[INFO-JABBER] Send bind request");
      p.sendRequest(new StringBuffer()
        .append("<iq type=\"set\" id=\"bind\">") 
        .append(  "<bind xmlns=\"urn:ietf:params:xml:ns:xmpp-bind\">")
        .append(    "<resource>").append("JimmyIM").append("</resource>")
        .append(  "</bind>")
        .append("</iq>").toString());
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
      JabberProtocol protocol)
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
      
      int endI = protocol.getAccount().getUser().indexOf("@");
      if (endI == -1) endI = protocol.getAccount().getUser().length();
      String t = responseMd5Digest(
          protocol.getAccount().getUser().substring(
              0, 
              endI),
          protocol.getAccount().getPassword(), 
          protocol.getAccount().getServer(), 
          "xmpp/" + protocol.getAccount().getServer(), 
          nonce, 
          cnonce);
      resp += t;
    }
    
    resp += "</response>";
    protocol.sendRequest(resp);
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

    String resp = MD5.toBase64(new StringBuffer()
      .append("username=\"").append(user)
      .append("\",realm=\"").append(realm)
      .append("\",nonce=\"").append(nonce)
      .append("\",nc=00000001,cnonce=\"").append(cnonce)
      .append("\",qop=auth,digest-uri=\"").append(digestUri)
      .append("\",response=\"").append(hResp.getDigestHex())
      .append("\",charset=utf-8").toString().getBytes());

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
    String first = new StringBuffer()
      .append("Email=").append(userName)
      .append("&Passwd=").append(passwd)
      .append("&PersistentCookie=false&source=googletalk").toString();
    try
    {
//      SSLSocket so = SSLSocketFactory.createSocket(socket)
      
      Socket s = HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(
          "https://www.google.com/accounts/ClientAuth?" + first, 
          443);
      
      System.out.println("[INFO-JABBER] Connecting to www.google.com");
      DataInputStream dis = new DataInputStream(s.getInputStream());
      String str = readLine(dis);
      String SID = "";
      String LSID = "";
      if (str.startsWith("SID="))
      {
        SID = str.substring(4, str.length());
        str = readLine(dis);
        LSID = str.substring(5, str.length());
        first = new StringBuffer()
          .append("SID=").append(SID)
          .append("&LSID=").append(LSID)
          .append("&service=mail&Session=true").toString();
        dis.close();
        s.close();

        s = HttpsURLConnection.getDefaultSSLSocketFactory().createSocket(
            "https://www.google.com/accounts/IssueAuthToken?" + first, 
            443);
        System.out.println("[INFO-JABBER] Next www.google.com connection");
        dis = new DataInputStream(s.getInputStream());
        str = readLine(dis);
        String token = MD5.toBase64(new StringBuffer()
          .append("\0").append(userName)
          .append("\0").append(str).toString().getBytes());
        dis.close();
        s.close();
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
      
      p.getContacts().addElement(c);
      j.addContact(c);
    }
    else
    {
      boolean changed = false;
      if (name != null && !name.equals(c.screenName())) {
        c.setScreenName(name); changed = true;
      }
      if (group != null && !group.equals(c.groupName())) {
        c.setGroupName(group); changed = true;
      }
      if (status != Byte.MAX_VALUE && status != c.status()) {
        c.setStatus(status); changed = true;
      }
      if (statusText != null && !statusText.equals(c.statusMsg())) {
        c.setStatusMsg(statusText); changed = true;
      }
      
      if (changed)
        j.changeContactStatus(c);
    }
  }
  
  protected static void sendStatus(
      byte status,
      JabberProtocol protocol)
  {
    StringBuffer presence = new StringBuffer()
      .append("<presence")
      .append(" jimmy='send status'>")
      .append(  "<priority>1</priority>");
    
    if (status != Contact.ST_OFFLINE && status != Contact.ST_ONLINE)
      presence
        .append(  "<show>")
        .append(    status == Contact.ST_AWAY ? "away" : "dnd")
        .append(  "</show>");
    
    presence.append("</presence>");
    
    protocol.sendRequest(presence.toString());
  }
  
//  TODO Uncomment when activating gtalk special features
//  /**
//   * Calculates string representation of initial user status
//   * Sets important Google settings
//   * Informs Google Talk that we want to use GTalk features 
//   */
//  protected static final String getGTalkOptionsXml()
//  {
//    return new StringBuffer()
//      .append("<iq type=\"get\" id=\"6\">")
//      .append(  "<query xmlns=\"google:relay\"/>")
//      .append("</iq>").toString();
//  }
//  
//  /**
//   * Sends mail notification request to GTalk server
//   */
//  protected static final String getGTalkMailNotificationReqXml(String fullJid)
//  {
//    return new StringBuffer()
//      .append("<iq type=\"set\" to=\"").append(fullJid).append("\" id=\"15\">")
//      .append(  "<usersetting xmlns=\"google:setting\">")
//      .append(    "<autoacceptrequests value=\"false\"/>")
//      .append(    "<mailnotifications value=\"true\"/>")
//      .append(  "</usersetting>")
//      .append("</iq>").toString();
//  }
//  
//  /**
//   * Get presence request for GTalk
//   */
//  protected static final String getGTalkPresence()
//  {
//    return "<presence><show></show><status></status></presence>";
//  }
//  
//  /**
//   * Get user status request
//   */
//  protected static final String getGTalkUserStatusXml(String fullJid)
//  {
//    return new StringBuffer()
//      .append("<iq type=\"get\" id=\"23\">")
//      .append(  "<query xmlns=\"google:mail:notify\" ")
//      .append(    "q=\"(!label:^s) (!label:^k) ((label:^u) (label:^i) (!label:^vm))\"/>")
//      .append("</iq>")
//      .append("<iq type=\"get\" to=\"").append(fullJid).append("\" id=\"21\">")
//      .append(  "<query xmlns=\"google:shared-status\"/>")
//      .append("</iq>").toString();
//  }
  
  /**
   * Get roster request
   */
  protected static final String getRosterXml()
  {
    return new StringBuffer()
      .append("<iq type=\"get\" id=\"roster\">")
      .append(  "<query xmlns=\"jabber:iq:roster\"/>")
      .append("</iq>").toString();
  }
  
  /**
   * Get open stream request
   */
  protected static final String getOpenStreamXml(String server)
  {
    return new StringBuffer()
      .append("<?xml version=\"1.0\"?>")
      .append("<stream:stream to=\"").append(server).append("\" xmlns=\"jabber:client\" ")
      .append(  "xmlns:stream=\"http://etherx.jabber.org/streams\" version=\"1.0\">").toString();
  }
}
