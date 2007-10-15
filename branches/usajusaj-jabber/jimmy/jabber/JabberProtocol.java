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
 File: jimmy/JabberProtocol.java
 Version: pre-alpha  Date: 2006/05/18
 Author(s): Matevz Jekovec
 */

package jimmy.jabber;

import java.io.DataInputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpsConnection;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.ServerHandler;
import jimmy.util.MD5;
import jimmy.util.Utils;

/**
 * This class implements the Jabber protocol. Non-standard comments are present
 * before any method. For the standard methods description, see
 * jimmy/Protocol.java .
 * 
 * @author Matevž Jekovec
 * @author Matej Ušaj
 */
public class JabberProtocol extends Protocol
{
  /** server handler reference - used for any outgoing/incoming connections*/
  ServerHandler sh_;

  /** default server name - if none set in user account */
  private final String DEFAULT_SERVER = "jabber.org";

  /** default server port - if none set in user account*/
  private final int DEFAULT_PORT = 5222;

  /** default Jabber streams address */
  private final String DEFAULT_STREAMS = "http://etherx.jabber.org/streams";

  /** stop the thread */
  private boolean stop_;
  
//  /** full jid (including resource name) */
//  private String fullJid;
  
  /**
   * Constructor method.
   */
  public JabberProtocol(ProtocolInteraction jimmy)
  {
    super(jimmy);
    protocolType_ = JABBER;
    status_ = DISCONNECTED;
  }

  /**
   * Returns the ServerHandler used to establish the connection.
   * 
   * @return ServerHandler used when connecting.
   */
  public ServerHandler getServerHandler()
  {
    return sh_;
  }

  /**
   * Initializes the connection and logs in using the given account.
   */
  public boolean login(Account account)
  {
    account_ = account;
    contacts_ = new Vector();
    String userName;
    String userServer;
    String server;
    int port;
    
    // get the first half of the JID (login name)
    userName = splitString(account.getUser(), '@')[0];

    // get the second half of the JID (server name)
    userServer = splitString(account.getUser(), '@')[1];

    // get the server name stored in the account - if none set, use the
    // user@server ones, if none @ given, use the default one
    if (account.getServer() != null)
      server = account.getServer();
    else if (userServer.length() != 0)
      server = userServer;
    else
      server = DEFAULT_SERVER;

    // get the server port stored in the account - use default server port if
    // none set
    port = (account.getPort() != 0) ? account.getPort() : DEFAULT_PORT;

    sh_ = new ServerHandler(server, port);
    sh_.connect(account.getUseSSL());
    if (!sh_.isConnected())
      return false;

    sh_.sendRequest(JabberParseXML.getWelcomeMsgXml(userServer, DEFAULT_STREAMS));
    if (isAuthError(sh_.getReply())) return false;

    boolean loggedIn;
    
    // GTalk auth:
    if (account.getServer().toLowerCase().endsWith("google.com"))
      loggedIn = doGtalk(userName, userServer, server, "JimmyIM", account);
    else
      loggedIn = doJabber(userName, userServer, server, "JimmyIM", account);
    
    if (loggedIn)
    {
      status_ = CONNECTED;
      thread_.start();
    }
    
    return loggedIn;
  }
  
  /**
   * Login to GTalk server
   * 
   * @return login success
   */
  private boolean doGtalk(
      String userName, 
      String userServer,
      String server,
      String resource,
      Account account)
  {
    System.out.println("[INFO-GTALK] Get google authorization token");
    String token = getGoogleToken(userName, account.getPassword());

    System.out.println("[INFO-GTALK] Authenticate");
    sendIgnoreResult(JabberParseXML.getAuthXml(token));

    System.out.println("[INFO-GTALK] Open stream");
    sendIgnoreResult(JabberParseXML.getOpenStreamXml(userServer));

    System.out.println("[INFO-GTALK] Bind resource");
    account_.setUser(sendIgnoreResult(JabberParseXML.getBindToServerXml(resource)));

    System.out.println("[INFO-GTALK] Create session");
    sendIgnoreResult(JabberParseXML.getCreateSessionXml(server));

    System.out.println("[INFO-GTALK] Enable GTalk features");
    sendIgnoreResult(JabberParseXML.getGTalkOptionsXml());

    System.out.println("[INFO-GTALK] Send mail notification request");
    sendIgnoreResult(JabberParseXML.getGTalkMailNotificationReqXml(account_.getUser()));
    
    System.out.println("[INFO-GTALK] Get contacts");
    sh_.sendRequest(JabberParseXML.getUserStatusXml(account_.getUser()));

    System.out.println("[INFO-GTALK] Get roster");
    sh_.sendRequest(JabberParseXML.getRosterXml());
    
    JabberParseXML.parseVarious(sh_.getReply(), this, jimmy_);

    System.out.println("[INFO-GTALK] Send presence");
    sendIgnoreResult(JabberParseXML.getGTalkPresence());
    
    System.out.println("[INFO-GTALK] Full ID: " + account_.getUser());
    
    return true;
  }
  
  /**
   * Login to other jabber server
   * 
   * @return login success
   */
  private boolean doJabber(
      String userName, 
      String userServer,
      String server,
      String resource,
      Account account)
  {
    // Authentication
    String query = "<iq type='set'>" + "<query xmlns='jabber:iq:auth'>"
        + "<username>" + userName + "</username>" + "<password>"
        + account.getPassword() + "</password>" + "<resource>globe</resource>"
        + "</query>" + "</iq>";

    sh_.sendRequest(query);
    String reply = sh_.getReply();
    System.out.println(reply);
    // check authentication feedback
    if (JabberParseXML.parseUserPass(reply) == false)
    {
      status_ = WRONG_PASSWORD;
      System.out.println("JabberProtocol: WRONG PASSWORD!");
      return false;
    }

    // Get contacts list
    query = "<iq type='get'><query xmlns='jabber:iq:roster'/></iq>";
    sh_.sendRequest(query);
    reply = sh_.getReply();
    // parse the contacts feedback
//    contacts_ = JabberParseXML.parseContacts(reply, this);
//    jimmy_.addContacts(contacts_);

    // Set status "online"
    query = "<presence type='available'/>";
    sh_.sendRequest(query);

    return true;
  }
  
  /**
   * Logs out and closes the connection.
   */
  public void logout()
  {
    String oString;
    oString = "<presence type='offline'/>";
    sh_.sendRequest(oString);
    stop_ = true; // stops the thread
  }

  public ChatSession startChatSession(Contact contact)
  {
    ChatSession cs = new ChatSession(this);
    chatSessionList_.addElement(cs);

    cs.addContact(contact);

    return cs;
  }

  public void sendMsg(String msg, ChatSession session)
  {
    System.out.println(msg);
    for (int i = 0; i < session.countContacts(); i++)
    {
      String oString = "<message to=\""
          + ((Contact) session.getContactsList().elementAt(i)).userID()
          + "\" from=\"" + account_.getUser() + "\" type=\"chat\">"
          +"<nos:x value=\"disabled\" xmlns:nos=\"google:nosave\"/>"+"<body>"+msg+"</body></message>";
      System.out.println("OUT:\n" + oString);
      sh_.sendRequest(oString);
    }
  }

  public void sendMsg(String msg, Vector contactsList, ChatSession session)
  {
    System.out.println(msg);
    for (int i = 0; i < session.countContacts(); i++)
    {
      if (!contactsList.contains(session.getContactsList().elementAt(i)))
        continue;

      String oString = "<message from='" + account_.getUser() + "' to='"
          + ((Contact) session.getContactsList().elementAt(i)).userID()
          + "' type='chat'><body>" + msg + "</body></message>\n";

      System.out.println("OUT:\n" + oString);
      sh_.sendRequest(oString);
    }
  }

  /**
   * Utility to split the given String of characters to two parts, seperated by
   * ch.
   * 
   * @param in
   *          Input String
   * @param ch
   *          Separator character
   * @return Array of two Strings
   */
  private static String[] splitString(String in, char ch)
  {
    String[] result = new String[2];
    int pos = in.indexOf(ch);

    if (pos != -1)
    {
      result[0] = in.substring(0, pos).trim();
      result[1] = in.substring(pos + 1).trim();
    }
    else
    {
      result[0] = in.trim();
    }

    return result;
  }

  public void run()
  {
    String in;
    stop_ = false;

    while (!stop_)
    {
      try
      {
        Thread.sleep(1000);
      }
      catch (InterruptedException e)
      {
        e.printStackTrace();
      }
      if (status_ != CONNECTED)
        continue;

      in = sh_.getReply();
      if (in != null)
        JabberParseXML.parseVarious(in, this, jimmy_);
    }

    sh_.disconnect();
    status_ = DISCONNECTED;
  }

  public void parseNewContacts(Contact[] c)
  {
    contacts_.copyInto(c);
  }

  public void addContact(Contact c)
  {
    String oString = "<presence from=\"" + account_.getUser() + "\" to=\""
        + c.userID() + "\" type=\"subscribe\"/>\n";
    sh_.sendRequest(oString);
    contacts_.addElement(c);
  }

  public boolean removeContact(Contact c)
  {
    if (getContact(c.userID()) == null)
      return false;

    String oString = "<iq from='" + getAccount().getUser()
        + "' type='set' id='roster_4'>\n"
        + "<query xmlns='jabber:iq:roster'>\n" + "<item jid='" + c.userID()
        + "' subscription='remove'/>\n" + "</query>\n" + "</iq>\n";

    sh_.sendRequest(oString);
    contacts_.removeElement(c);

    return true;
  }

  public void updateContactProperties(Contact c)
  {
    String oString = "<iq type='set' from='"
        + account_.getUser()
        + "'>\n"
        + "<query xmlns='jabber:iq:roster'>\n"
        + "<item "
        + "jid='"
        + c.userID()
        + "' "
        + "subscription='both'"
        + ((c.screenName() != null) ? (" name='" + c.screenName() + "'") : "")
        + ">\n"
        + ((c.groupName() != null) ? ("<group>" + c.groupName() + "</group>\n")
            : "") + "</item>\n" + "</query>\n" + "</iq>\n";

    sh_.sendRequest(oString);
  }
  
  private String sendIgnoreResult(String query)
  {
    sh_.sendRequest(query);
    return JabberParseXML.parseVarious(sh_.getReply(), this, jimmy_);
  }
  
  private boolean isAuthError(String reply)
  {
    if (Utils.stringContains(reply.toLowerCase(), "stream:error"))
      return true;
    return false;
  }

  /* FROM MGTALK ************************************ */

  /**
   * From mGTalk
   * 
   * Generates X-GOOGLE-TOKEN response by communication with
   * http://www.google.com
   * 
   * @param userName
   * @param passwd
   * @return
   */
  private String getGoogleToken(String userName, String passwd)
  {
    String first = "Email=" + userName + "&Passwd=" + passwd
        + "&PersistentCookie=false&source=googletalk";
    try
    {
      HttpsConnection c = (HttpsConnection) Connector
          .open("https://www.google.com:443/accounts/ClientAuth?" + first);
      System.out.println("[INFO] Connecting to www.google.com");
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
        System.out.println("[INFO] Next www.google.com connection");
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
   * Service routine
   * 
   * @param dis
   * @return
   */
  private String readLine(DataInputStream dis)
  {
    String s = "";
    byte ch = 0;
    try
    {
      while ((ch = dis.readByte()) != -1)
      {
        if (ch == '\n')
          return s;
        s += (char) ch;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }

    return s;
  }
}
