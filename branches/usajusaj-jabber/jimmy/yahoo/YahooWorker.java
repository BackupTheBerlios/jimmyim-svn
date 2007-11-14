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

import jimmy.ProtocolInteraction;

/**
 * @author Matej Usaj
 */
public class YahooWorker extends YahooConstants
{
  protected static void processPacket(
      YahooPacket packet, 
      YahooProtocol protocol,
      ProtocolInteraction jimmy)
  {
    switch (packet.service_)
    {
      case SERVICE_AUTH: doAuth(packet, protocol); break;
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
          protocol.getAccount().getUser(), 
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
  
  public static void main(String[] args)
  {
  }
}
