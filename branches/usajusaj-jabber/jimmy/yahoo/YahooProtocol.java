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
 File: jimmy/yahoo/YahooProtocol.java
 Version: pre-alpha  Date: 2007/07/27
 Author(s): Matej Usaj
 */

package jimmy.yahoo;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.ServerHandler;

/**
 * Yahoo protocol implementation class
 * 
 * @author Matej Usaj
 */
public class YahooProtocol extends Protocol
{
  /** default server name - if none set in user account */
  private final String DEFAULT_SERVER_ = "scs.msg.yahoo.com";
  
  /** default server port - if none set in user account*/
  private final int DEFAULT_PORT_ = 5050;
  
  /** Max time for authentication to complete its process (in ms) */
  private final int AUTH_TIMEOUT = 30000;
  
  /** Ping packet interval (in ms) */
  private final int PING_TIMEOUT = 60000;

  /** server handler reference - used for any outgoing/incoming connections*/
  protected ServerHandler sh_;
  
  /** stop the thread */
  private boolean stop_;

  /**
   * Default constructor
   * 
   * @param jimmy {@link ProtocolInteraction} class
   */
  public YahooProtocol(ProtocolInteraction jimmy)
  {
    super(jimmy);
    protocolType_ = YAHOO;
    status_ = DISCONNECTED;
  }

  /**
   * Opens connection to yahoo server, authenticates, fills the contact list
   * and does all necessary server interaction
   * 
   * @param account Users yahoo {@link Account}
   * @return Authentications success
   */
  public boolean login(Account account)
  {
    /* Set the required variables */
    status_ = CONNECTING;
    account_ = account;
    contacts_ = new Vector();

    account_.setUser(account_.getUser().toLowerCase());
    account_.setServer(
        account_.getServer() != null ? 
            account_.getServer() :
            DEFAULT_SERVER_);
    account_.setPort(
        account_.getPort() != 0 ? 
            account_.getPort() : 
            DEFAULT_PORT_);

    /* Create and open connection */
    sh_ = new ServerHandler(account_.getServer(), account_.getPort());
    sh_.connect(account.getUseSSL());
    if (!sh_.isConnected()) return false;
    sh_.sendRequest(
      YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_AUTH)
        .setStatus(YahooConstants.STATUS_AVAILABLE)
        .setSessionId(0L)
        .append("0", account_.getUser())
        .append("1", account_.getUser()).packPacket());
    
    /* Start the listener thread */
    thread_.start();

    /* Authenticate with the server */
    int t = 0;
    while (status_ == CONNECTING)
    {
      /* Timeout */
      try {
        Thread.sleep(10);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if ((t += 10) > AUTH_TIMEOUT)
      {
        status_ = NO_CONNECTION;
        break;
      }
    }
    
    /* Check if authentication failed */
    if (status_ == DISCONNECTED ||
        status_ == NO_CONNECTION ||
        status_ == WRONG_PASSWORD)
    {
      stop_ = true;
      return false;
    }
    
    return true;
  }

  /**
   * Sends "offline" status and stops the listener thread
   */
  public void logout()
  {
    System.out.println("[INFO-YAHOO] LOGOUT");
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_LOGOFF)
        .setStatus(YahooConstants.STATUS_OFFLINE)
        .append("0", account_.getUser()).packPacket());
    stop_ = true;
  }

  /**
   * Adds new contact to Yahoo account
   * 
   * @param c New {@link Contact} to subscribe to
   */
  public void addContact(Contact c)
  {
    if (getContact(c.userID()) != null)
      return;
    
    if (c.groupName() == null || c.groupName().length() == 0)
      c.setGroupName("JimmyIM");
    
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_FRIENDADD)
        .setStatus(YahooConstants.STATUS_AVAILABLE)
        .append("1", account_.getUser())
        .append("7", c.userID())
        .append("65", c.groupName())/* group */
        .packPacket());
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_CONTACTIGNORE)
        .setStatus(YahooConstants.STATUS_OFFLINE)
        .append("1", account_.getUser())
        .append("7", c.userID())
        .append("13", "2")
        .packPacket());
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_ISBACK)
        .setStatus(YahooConstants.STATUS_AVAILABLE)
        .append("10", Long.toString(YahooConstants.STATUS_AVAILABLE)).packPacket());

    
//    jimmy_.addContact(c);
    contacts_.addElement(c);
  }

  /**
   * Removes a contact from Yahoo account
   * 
   * @param c {@link Contact} to unsubscribe from
   * @return Removal success
   */
  public boolean removeContact(Contact c)
  {
    if (getContact(c.userID()) == null)
      return false;
    
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_FRIENDREMOVE)
        .setStatus(YahooConstants.STATUS_OFFLINE)
        .append("1", account_.getUser())
        .append("7", c.userID())
        .append("65", c.groupName() == null ? "JimmyIM" : c.groupName())
        .packPacket());
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_CONTACTREJECT)
        .setStatus(YahooConstants.STATUS_OFFLINE)
        .append("1", account_.getUser())
        .append("7", c.userID())
        .append("14", "")
        .packPacket());
    sh_.sendRequest(YahooPacket.createPacket()
        .setService(YahooConstants.SERVICE_CONTACTIGNORE)
        .setStatus(YahooConstants.STATUS_OFFLINE)
        .append("1", account_.getUser())
        .append("7", c.userID())
        .append("13", "1")
        .packPacket());
    
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
    jimmy_.changeContactStatus(c);
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
   * @param to Receivers jid
   */
  private void sendMsg(String msg, String to)
  {
    sh_.sendRequest(YahooPacket.createPacket()
      .setService(YahooConstants.SERVICE_MESSAGE)
      .setStatus(YahooConstants.STATUS_OFFLINE)
      .append("0", account_.getUser())
      .append("1", account_.getUser())
      .append("5", to)
      .append("14", msg)
      .append("64", "0").packPacket());
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
    
    int pingCount = 0;
    /* Loop while manualy stopped */
    while (!stop_)
    {
      /* Timeout */
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      
      byte[] reply = sh_.getReplyBytes();
      if (reply == null)
      {
        System.out.println("[INFO-YAHOO] NULL PACKET");
        continue;
      }
      
      Vector v = YahooPacket.splitPacket(reply);
      for (int i = 0; i < v.size(); i++)
      {
        YahooPacket packet = YahooPacket.parsePacket((byte[])v.elementAt(i));
        
        /* Fetch and process reply */
        YahooWorker.processPacket(
            packet, 
            this, 
            jimmy_);
      }

      if ((pingCount += 1000) >= PING_TIMEOUT)
      {
        sh_.sendRequest(YahooPacket.createPacket()
            .setService(YahooConstants.SERVICE_PING)
            .setStatus(YahooConstants.STATUS_AVAILABLE).packPacket());
        pingCount = 0;
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
