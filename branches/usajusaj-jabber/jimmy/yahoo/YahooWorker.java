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
 File: jimmy/yahoo/YahooWorker.java
 Version: pre-alpha  Date: 2007/11/14
 Author(s): Matej Usaj
 */

package jimmy.yahoo;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.Vector;

import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ProtocolInteraction;
import jimmy.util.Utils;

/**
 * @author Matej Usaj
 */
public class YahooWorker extends YahooConstants
{
  private static YahooPacket list;
  
  protected static void processPacket(
      YahooPacket packet, 
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    switch (packet.service_)
    {
      case SERVICE_AUTH: System.out.println("[INFO-YAHOO] AUTH"); doAuth(packet, protocol); break;
      case SERVICE_ADDIGNORE : System.out.println("[INFO-YAHOO] ADDIGNORE"); break;
      case SERVICE_AUTHRESP : System.out.println("[INFO-YAHOO] AUTHRESP"); protocol.setAuthStatus(true); break;
      case SERVICE_CHATCONNECT : System.out.println("[INFO-YAHOO] CHATCONNECT"); break;
      case SERVICE_CHATDISCONNECT: System.out.println("[INFO-YAHOO] CHATDISCONNECT"); break;
      case SERVICE_CHATLOGOFF : System.out.println("[INFO-YAHOO] CHATLOGOFF"); break;
      case SERVICE_CHATLOGON : System.out.println("[INFO-YAHOO] CHATLOGON"); break;
      case SERVICE_CHATMSG : System.out.println("[INFO-YAHOO] CHATMSG"); break;
      case SERVICE_CHATPM : System.out.println("[INFO-YAHOO] CHATPM"); break;
      case SERVICE_CONFADDINVITE: System.out.println("[INFO-YAHOO] CONFADDINVITE"); break;
      case SERVICE_CONFDECLINE : System.out.println("[INFO-YAHOO] CONFDECLINE"); break;
      case SERVICE_CONFINVITE : System.out.println("[INFO-YAHOO] CONFINVITE"); break;
      case SERVICE_CONFLOGOFF : System.out.println("[INFO-YAHOO] CONFLOGOFF"); break;
      case SERVICE_CONFLOGON : System.out.println("[INFO-YAHOO] CONFLOGON"); break;
      case SERVICE_CONFMSG : System.out.println("[INFO-YAHOO] CONFMSG"); break;
      case SERVICE_CONTACTIGNORE: System.out.println("[INFO-YAHOO] CONTACTIGNORE"); break;
      case SERVICE_CONTACTNEW : System.out.println("[INFO-YAHOO] CONTACTNEW"); break;
      case SERVICE_FRIENDADD : System.out.println("[INFO-YAHOO] FRIENDADD"); break;
      case SERVICE_FRIENDREMOVE : System.out.println("[INFO-YAHOO] FRIENDREMOVE"); break;
      case SERVICE_IDACT : System.out.println("[INFO-YAHOO] IDACT"); break;
      case SERVICE_IDDEACT : System.out.println("[INFO-YAHOO] IDDEACT"); break;
      case SERVICE_ISAWAY : System.out.println("[INFO-YAHOO] ISAWAY"); break;
      case SERVICE_ISBACK : System.out.println("[INFO-YAHOO] ISBACK"); break;
      case SERVICE_LIST : System.out.println("[INFO-YAHOO] LIST"); parseList(packet, protocol, jimmy);  break;
      case SERVICE_LOGOFF : System.out.println("[INFO-YAHOO] LOGOFF"); break;
      case SERVICE_LOGON : System.out.println("[INFO-YAHOO] LOGON"); doLogon(packet, protocol, jimmy);  break;
      case SERVICE_MESSAGE : System.out.println("[INFO-YAHOO] MESSAGE"); parseMessage(packet, protocol, jimmy); break;
      case SERVICE_NOTIFY : System.out.println("[INFO-YAHOO] NOTIFY"); break;
      case SERVICE_PING : System.out.println("[INFO-YAHOO] PING"); break;
      case SERVICE_USERSTAT : System.out.println("[INFO-YAHOO] USERSTAT"); break;
      default:
        break;
    }
  }
  
  private static void doAuth(
      YahooPacket packet, 
      YahooProtocol protocol)
  {
    String challenge = packet.getValue("94");
    try
    {
      String[] s = YahooChallengeResponse.getStrings(
          protocol.getAccount().getPassword(), 
          challenge);
      byte[] msg = YahooPacket.createPacket()
        .append("0", protocol.getAccount().getUser())
        .append("6", s[0])
        .append("96", s[1])
        .append("2", "1")
        .append("1", protocol.getAccount().getUser())
        .setService(SERVICE_AUTHRESP)
        .setStatus(STATUS_AVAILABLE).packPacket();
      protocol.sh_.sendRequest(msg);
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
      return;
    }
  }
  
  private static void parseList(
      YahooPacket packet,
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (list == null)
      list = packet.clone();
    else
      list.merge(packet, new String[]{"87", "88", "89"});
    
    if (!packet.hasKey("59")) return;
    
    String k87 = list.getValue("87");
    if (k87 != null)
    {
      String[] friends = Utils.explode('\n', k87);
      for (int i = 0; i < friends.length; i++)
      {
        String[] groupUsers = Utils.explode(':', friends[i]);
        String[] users = Utils.explode(',', groupUsers[1]);
        for (int j = 0; j < users.length; j++)
        {
          processContact(users[j], protocol, Contact.ST_OFFLINE, groupUsers[0], null, jimmy);
        }
      }
    }
    String k88 = list.getValue("88");
    if (k88 != null)
    {
      String[] users = Utils.explode(',', k88);
      for (int i = 0; i < users.length; i++)
      {
        processContact(users[i], protocol, Contact.ST_OFFLINE, null, null, jimmy);
      }
    }
    String k89 = list.getValue("89");
    if (k89 != null)
    {
      String[] users = Utils.explode(',', k89);
      for (int i = 0; i < users.length; i++)
      {
        System.out.println(users[i]);
//        Contact c = protocol.getContact(users[i]);
//        if (c == null)
//        {
//          c = new Contact(
//              users[i], 
//              protocol);
//          c.setStatus(Contact.ST_OFFLINE);
//          protocol.addContact(c);
//          jimmy.addContact(c);
//        }
//        else
//          c.setStatus(Contact.ST_OFFLINE);
      }
    }
  }
  
  private static void parseMessage(
      YahooPacket packet,
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (packet.hasKey("14"))
    {
      Vector tos = packet.getValues("5");
      Vector froms = packet.getValues("4");
      Vector messages = packet.getValues("14");
      Vector timestamps = packet.getValues("15");
      if (packet.ystatus_ == STATUS_NOTINOFFICE)
        for (int i = 0; i < messages.size(); i++)
        {
          if (tos.elementAt(i).equals(protocol.getAccount().getUser()))
          {
            createMessage(
                (String)froms.elementAt(i), 
                Utils.formatDate(new Date(Long.parseLong((String)timestamps.elementAt(i))*1000)) + "> " + (String)messages.elementAt(i), 
                protocol, 
                jimmy);
          }
        }
      else
        if (tos.elementAt(0).equals(protocol.getAccount().getUser()))
          createMessage(
              (String)froms.elementAt(0), 
              (String)messages.elementAt(0), 
              protocol, 
              jimmy);
    }
  }
  
  private static void createMessage(
      String from,
      String message,
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    Contact c = protocol.getContact(from);
    ChatSession cs = null;
    if (c == null)
      c = new Contact(from, protocol);
    else
      cs = protocol.getChatSession(c);

    if (cs == null)
      cs = protocol.startChatSession(c);
    
    jimmy.msgRecieved(cs, c, message);
  }
  
  private static void processContact(
      String id,
      Protocol protocol,
      byte status,
      String group,
      String screenName,
      ProtocolInteraction jimmy)
  {
    Contact c = protocol.getContact(id);
    if (c == null)
    {
      c = new Contact(
          id, 
          protocol,
          status == (byte)-1 ? Contact.ST_ONLINE : status,
          group,
          screenName);
      protocol.addContact(c);
      jimmy.addContact(c);
    }
    
    if (status != (byte)-1) c.setStatus(status);
    if (group != null) c.setGroupName(group);
    if (screenName != null) c.setScreenName(screenName);
    jimmy.changeContactStatus(c);
  }
  
  private static void doLogon(
      YahooPacket packet,
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    if (packet.hasKey("7"))
    {
      Vector contacts = packet.getValues("7");
      Vector statuses = packet.getValues("10");
      for (int i = 0; i < contacts.size(); i++)
      {
        String id = (String)contacts.elementAt(i);
        long status = Long.parseLong((String)statuses.elementAt(i));
        if (status == STATUS_OFFLINE)
          processContact(id, protocol, Contact.ST_OFFLINE, null, null, jimmy);
        else
          processContact(id, protocol, Contact.ST_ONLINE, null, null, jimmy);
      }
    }
    
    protocol.setAuthStatus(true);
    byte[] p = YahooPacket.createPacket()
      .setService(SERVICE_ISBACK)
      .setStatus(STATUS_AVAILABLE)
      .append("10", Long.toString(STATUS_AVAILABLE)).packPacket();
    protocol.sh_.sendRequest(p);
  }
}
