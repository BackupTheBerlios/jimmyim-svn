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
 File: jimmy/YahooProtocol.java
 Version: pre-alpha  Date: 2006/07/27
 Author(s): Matevz Jekovec
 */

package jimmy.yahoo;

import java.util.Vector;

import jimmy.Account;
import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.net.ServerHandler;

public class YahooProtocol extends Protocol
{
  private final static String MAGIC = "YMSG";
  
  private final String DEFAULT_SERVER_ = "scs.msg.yahoo.com";
  private final int DEFAULT_PORT_ = 5050;
  private final static int[] SEPARATOR = { 0xc0, 0x80 };

  protected ServerHandler sh_;
  protected Account account_;
  
  /* FOR HEADER */
  private int service_;
  private long ystatus_;
  private long sessionId_;

  public YahooProtocol(ProtocolInteraction jimmy)
  {
    super(jimmy);
    protocolType_ = YAHOO;
    status_ = DISCONNECTED;
  }

  public boolean login(Account account)
  {
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

    sh_ = new ServerHandler(account_.getServer(), account_.getPort());
    sh_.connect(account.getUseSSL());
    if (!sh_.isConnected()) return false;
    
    service_ = YahooConstants.SERVICE_AUTH;
    ystatus_ = YahooConstants.STATUS_AVAILABLE;
    sessionId_ = 0L;
    
    byte[] body = addToBody(new byte[0], "0");
    body = addToBody(body, account_.getUser());
    body = addToBody(body, "1");
    body = addToBody(body, account_.getUser());
    
    return false;
  }

  public void logout()
  {
  }

  public void addContact(Contact c)
  {
  }

  public boolean removeContact(Contact c)
  {
    return false;
  }

  public void sendMsg(String msg, ChatSession session)
  {
  }

  public void sendMsg(String msg, Vector contactsList, ChatSession session)
  {
  }

  public ChatSession startChatSession(Contact user)
  {
    return null;
  }

  public void updateContactProperties(Contact c)
  {
  }

  public void run()
  {
  }
  
  private byte[] addToBody(byte[] oldBody, String data)
  {
    byte[] body = new byte[oldBody.length + data.length() + 2];
    for (int i = 0; i < oldBody.length; i++)
      body[i] = oldBody[i];
    for (int i = oldBody.length; i < body.length; i++)
      body[i] = (byte)data.charAt(i - oldBody.length);
    
    body[body.length - 2] = (byte)SEPARATOR[0];
    body[body.length - 1] = (byte)SEPARATOR[1];
    
    return body;
  }
  
  private void send(byte[] body)
  {
    
  }
  
  private byte[] attachHeader(byte[] body)
  {
    byte[] message = new byte[body.length + 20];
    
    for (int i = 0; i < MAGIC.length(); i++)
      message[i] = (byte)MAGIC.charAt(i); 
    body[4] = (byte)SEPARATOR[0];
    body[5] = (byte)SEPARATOR[1];
    body[6] = (byte)0x0a;
    for (int i = 7; i < 10; i++)
      message[i] = (byte)0x00; 
    
    
    return message;
  }
}
