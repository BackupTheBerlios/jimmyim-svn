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
 Version: beta  Date: 2007/10/17
 Author(s): Matevz Jekovec, Matej Usaj
 */

package jimmy.jabber;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.ServerHandler;
import jimmy.util.XmlNode;

/**
 * Jabber (XMPP) protocol implementation class
 * 
 * @author Matevz Jekovec
 * @author Matej Usaj
 */
public class JabberProtocol extends Protocol
{
  /** default server name - if none set in user account */
  private final String DEFAULT_SERVER = "jabber.org";

  /** default server port - if none set in user account*/
  private final int DEFAULT_PORT = 5222;

  /** server handler reference - used for any outgoing/incoming connections*/
  protected ServerHandler sh_;

  /** stop the thread */
  private boolean stop_;
  
  /** User account domain name */
  protected String domain_;
  
  /** Is this a GTalk account */
  protected boolean isGTalk_;
  
  /** Full users jid in uname@domain/resource format */
  protected String fullJid_;
  
  /**
   * Default constructor
   * 
   * @param jimmy {@link ProtocolInteraction} class
   */
  public JabberProtocol(ProtocolInteraction jimmy)
  {
    super(jimmy);
    protocolType_ = JABBER;
    status_ = DISCONNECTED;
  }
  
  /**
   * Opens connection to jabber server, authenticates, fills the contact list
   * and does all necessary server interaction
   * 
   * @param account Users jabber {@link Account}
   * @return Authentications success
   */
  public boolean login(Account account)
  {
    /* Set the required variables */
    status_ = CONNECTING;
    account_ = account;
    contacts_ = new Vector();
    isGTalk_ = false;
    
    domain_ = account.getUser().indexOf("@") >= 0 ?
        account.getUser().substring(
            account.getUser().indexOf("@") + 1, account.getUser().length()) : 
        account.getServer() != null ? 
            account.getServer() :
            DEFAULT_SERVER;
            
    account.setServer(
        account.getServer() != null ? 
            account.getServer() :
            domain_);
    
    account.setPort(account.getPort() == 0 ? DEFAULT_PORT : account.getPort());
    
    /* Create and open connection */
    sh_ = new ServerHandler(account.getServer(), account.getPort());
    sh_.connect(account.getUseSSL());
    if (!sh_.isConnected()) return false;
    sh_.sendRequest(JabberParseXML.getOpenStreamXml(domain_));

    /* Authenticate with the server */
    while (status_ == CONNECTING)
    {
      XmlNode x = XmlNode.parse(sh_.getReply());
      if (x != null)
        for (int i = 0; i < x.childs.size(); i++)
        {
          JabberParseXML.parse(
              (XmlNode)x.childs.elementAt(i), 
              this, 
              jimmy_);
        }
    }
    
    /* Check if authentication failed */
    if (status_ == DISCONNECTED ||
        status_ == NO_CONNECTION ||
        status_ == WRONG_PASSWORD)
      return false;
    
    /* Start the listener thread */
    thread_.start();
    
    /* Enable special GTalk features */
    if (isGTalk_)
    {
      sh_.sendRequest(JabberParseXML.getGTalkOptionsXml());
      sh_.sendRequest(JabberParseXML.getGTalkMailNotificationReqXml(fullJid_));
      sh_.sendRequest(JabberParseXML.getGTalkPresence());
    }
    
    /* Request roster and user status */
    sh_.sendRequest(JabberParseXML.getUserStatusXml(fullJid_));
    sh_.sendRequest(JabberParseXML.getRosterXml());
    
    sh_.sendRequest("<presence from=\"" + fullJid_ + "\"/>");
    
    return true;
  }
  
  /**
   * Sends "offline" status and stops the listener thread
   */
  public void logout()
  {
    sh_.sendRequest("<presence type='unavailable'/>");
    stop_ = true;
  }
  
  /**
   * Adds new contact to jabber account
   * 
   * @param c New {@link Contact} to subscribe to
   */
  public void addContact(Contact c)
  {
    sh_.sendRequest(
        "<presence from=\"" + account_.getUser() + "\" to=\"" + 
          c.userID() + "\" type=\"subscribe\"/>");
    contacts_.addElement(c);
  }
  
  /**
   * Removes a contact from jabber account
   * 
   * @param c {@link Contact} to unsubscribe from
   * @return Removal success
   */
  public boolean removeContact(Contact c)
  {
    if (getContact(c.userID()) == null)
      return false;

    sh_.sendRequest(
        "<iq from='" + fullJid_ + "' type='set' id='roster_4'>" + 
        "  <query xmlns='jabber:iq:roster'>" +
        "    <item jid='" + c.userID() + "' subscription='remove'/>" + 
        "  </query>" + 
        "</iq>");
    contacts_.removeElement(c);

    return true;
  }
  
  /**
   * Updates contacts data
   * 
   * @param c {@link Contact} to update
   */
  public void updateContactProperties(Contact c)
  {
    sh_.sendRequest(
        "<iq type='set' from='" + fullJid_ + "'>" + 
        "  <query xmlns='jabber:iq:roster'>" +
        "    <item jid='" + c.userID() + "' subscription='both'" + 
             ((c.screenName() != null) ? (" name='" + c.screenName() + "'") : "") + ">" + 
             ((c.groupName() != null) ? ("<group>" + c.groupName() + "</group>") : "") + 
        "    </item>" + 
        "  </query>" + 
        "</iq>");
  }
  
  /**
   * Sends a message to all contacts in a {@link ChatSession}
   * 
   * @param msg Message to send
   * @param session {@link ChatSession} to send message to
   */
  public void sendMsg(String msg, ChatSession session)
  {
    for (int i = 0; i < session.countContacts(); i++)
      sendMsg(msg, ((Contact) session.getContactsList().elementAt(i)).userID());
  }
  
  /**
   * Sends a message to specific contacts in a {@link ChatSession}
   * 
   * @param msg Message to send
   * @param contactsList {@link Vector} containing contacts to send message to
   * @param session {@link ChatSession} to send message to
   */
  public void sendMsg(String msg, Vector contactsList, ChatSession session)
  {
    for (int i = 0; i < session.countContacts(); i++)
    {
      if (!contactsList.contains(session.getContactsList().elementAt(i)))
        continue;
      
      sendMsg(msg, ((Contact) session.getContactsList().elementAt(i)).userID());
    }
  }
  
  /**
   * Sends a message to a user
   * 
   * @param msg Message to send
   * @param recJid Receivers jid
   */
  private void sendMsg(String msg, String recJid)
  {
    sh_.sendRequest(
        "<message " +
        " to=\"" + recJid + "\"" +
        " from=\"" + fullJid_ + "\" " +
        " type=\"chat\">" +
        (isGTalk_ ? "  <nos:x value=\"disabled\" xmlns:nos=\"google:nosave\"/>" : "") +
        "  <body>" + msg + "</body>" +
        "</message>");
  }
  
  /**
   * Creates a new {@link ChatSession} with a {@link Contact}
   * 
   * @param user {@link Contact} to start the session with
   * @return New {@link ChatSession}
   */
  public ChatSession startChatSession(Contact user)
  {
    ChatSession cs = new ChatSession(this);
    chatSessionList_.addElement(cs);

    cs.addContact(user);

    return cs;
  }
  
  /**
   * Runs the listener {@link Thread}. It periodicaly checks the
   * connections inbound buffer for new messages 
   */
  public void run()
  {
    stop_ = false;
    
    /* Loop while manualy stopped */
    while (!stop_)
    {
      /* Timeout */
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      /* Fetch and parse reply */
      XmlNode x = XmlNode.parse(sh_.getReply());
      if (x != null)
        for (int i = 0; i < x.childs.size(); i++)
        {
          JabberParseXML.parse(
              (XmlNode)x.childs.elementAt(i), 
              this, 
              jimmy_);
        }
    }

    sh_.disconnect();
    status_ = DISCONNECTED;
  }
  
  /**
   * Sets authentications success
   * 
   * @param s Authentications success
   */
  protected void setAuthStatus(boolean s)
  {
    if (s) status_ = CONNECTED;
    else status_ = DISCONNECTED;
  }
}
