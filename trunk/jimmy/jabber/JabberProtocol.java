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
import jimmy.util.Utils;
import jimmy.util.XmlNode;

/**
 * Jabber (XMPP) protocol implementation class
 * 
 * @author Matevz Jekovec
 * @author Matej Usaj
 */
public class JabberProtocol extends Protocol
{
  protected final String RESOURCE = "JimmyIM";
  
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
  
  /** Is Sasl */
  protected boolean isSasl_ = true;
  
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
    sendRequest(JabberParseXML.getOpenStreamXml(domain_));
    
    /* Authenticate with the server */
    while (status_ == CONNECTING)
    {
      XmlNode x = XmlNode.parse(sh_.getReply());
      if (x != null)
        JabberParseXML.parseAuth(x, this, jimmy_);
    }
    
    /* Check if authentication failed */
    if (status_ == DISCONNECTED ||
        status_ == NO_CONNECTION ||
        status_ == WRONG_PASSWORD)
      return false;
    
    /* Start the listener thread */
    thread_.start();
    
    /* Enable special GTalk features */
//    TODO Activate google talk features 
//    FIXME If activated, user status is constantly "dnd" and status message is "Away"
//    if (isGTalk_)
//    {
//      sendRequest(JabberParseXML.getGTalkUserStatusXml(fullJid_));
//      sendRequest(JabberParseXML.getGTalkOptionsXml());
//      sendRequest(JabberParseXML.getGTalkMailNotificationReqXml(fullJid_));
//      sendRequest(JabberParseXML.getGTalkPresence());
//    }
    
    /* Request roster */
    sendRequest(JabberParseXML.getRosterXml());
    
    JabberParseXML.sendStatus(Contact.ST_ONLINE, this);
//    sendRequest(new StringBuffer()
//      .append("<presence><priority>1</priority><c xmlns='http://jabber.org/protocol/caps'")
//      .append("node='http://pidgin.im/caps' ver='2.2.1' ext='moodn nickn tunen avatar'/></presence>").toString());
//    JabberParseXML.sendStatus(Contact.ST_ONLINE, this);
//    sh_.sendRequest(new StringBuffer()
//      .append("<presence from=\"").append(fullJid_).append("\" jimmy=\"login\"/>").toString());
    
    return true;
  }
  
  /**
   * Sends "offline" status and stops the listener thread
   */
  public void logout()
  {
    sendRequest("</stream:stream>");
//    for (int i = 0; i < contacts_.size(); i++)
//      JabberParseXML.sendStatus(
//          Contact.ST_OFFLINE, 
//          ((Contact)contacts_.elementAt(i)).userID(),
//          this);

    stop_ = true;
  }
  
  /**
   * Adds new contact to jabber account
   * 
   * @param c New {@link Contact} to subscribe to
   */
  public void addContact(Contact c)
  {
    if (getContact(c.userID()) != null)
      return;

    sendRequest(new StringBuffer()
      .append("<presence")
      .append(" from=\"").append(fullJid_).append("\"")
      .append(" to=\"").append(c.userID()).append("\"")
      .append(" type=\"subscribe\"")
      .append(" jimmy=\"addContact\"/>").toString());
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
    
    sendRequest(new StringBuffer()
      .append("<presence")
      .append(" from=\"").append(fullJid_).append("\"")
      .append(" to=\"").append(c.userID()).append("\"")
      .append(" type='unavailable'")
      .append(" jimmy=\"removeContact\"/>")
      .append("<presence")
      .append(" from=\"").append(fullJid_).append("\"")
      .append(" to=\"").append(c.userID()).append("\"")
      .append(" type=\"unsubscribe\"")
      .append(" jimmy=\"removeContact\"/>")
      .append("<iq from='").append(fullJid_).append("' type='set' id='roster_4'>") 
      .append(  "<query xmlns='jabber:iq:roster'>")
      .append(    "<item jid='").append(c.userID()).append("' subscription='remove'/>") 
      .append(  "</query>") 
      .append("</iq>").toString());
    
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
    sendRequest(new StringBuffer()
      .append("<iq type='set' from='").append(fullJid_).append("'>") 
      .append(  "<query xmlns='jabber:iq:roster'>")
      .append(    "<item jid='").append(c.userID()).append("' subscription='both'") 
      .append(((c.screenName() != null) ? (" name='" + Utils.urlDecode(c.screenName()) + "'") : "")).append(">") 
      .append(((c.groupName() != null) ? ("<group>" + Utils.urlDecode(c.groupName()) + "</group>") : "")) 
      .append(    "</item>") 
      .append(  "</query>") 
      .append("</iq>").toString());
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
    sendRequest(new StringBuffer()
      .append("<message ")
      .append(" to=\"").append(recJid).append("\"")
      .append(" from=\"").append(fullJid_).append("\"")
      .append(" type=\"chat\">")
      .append((isGTalk_ ? "<nos:x value=\"disabled\" xmlns:nos=\"google:nosave\"/>" : ""))
      .append("<body>").append(msg).append("</body>")
      .append("</message>").toString());
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
          System.out.println("[JABBER IN XML]:\n" + x.childs.elementAt(i).toString());
          JabberParseXML.parse(
              (XmlNode)x.childs.elementAt(i), 
              this, 
              jimmy_);
        }
    }

    sh_.disconnect();
    status_ = DISCONNECTED;
//    account_.setConnected(false);
//    jimmy_.stopProtocol(this);
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
  
  /**
   * Sends request to output stream (debugging purposes).
   */
  protected final void sendRequest(String msg)
  {
    System.out.println("[JABBER OUT XML]:\n  " + msg);
    sh_.sendRequest(msg);
  }
}
