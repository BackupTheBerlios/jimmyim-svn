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
 File: jimmy/yahoo/YahooPacket.java
 Version: pre-alpha  Date: 2007/11/14
 Author(s): Matej Usaj
 */

package jimmy.yahoo;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import jimmy.util.Utils;

/**
 * @author matej
 *
 */
public class YahooPacket
{
  private static final byte[] RESOURCE = "YMSG".getBytes();
  private static final byte[] VERSION = { 10,0x00,0x00,0x00 };
  private static final byte[] SEPARATOR = { (byte)0xc0, (byte)0x80 };
  private static final int HEAD_LEN = 20;
  
  protected int service_;
  protected long ystatus_;
  protected long sessionId_;
  
  protected Vector message;

  private ByteArrayOutputStream hBuff = new ByteArrayOutputStream(HEAD_LEN);
  private ByteArrayOutputStream bBuff = new ByteArrayOutputStream(1024);
  
  private DataOutputStream headerWrapper;
  private DataOutputStream bodyWrapper;
  
  private static YahooPacket instance;
  
  private YahooPacket()
  {
    headerWrapper = new DataOutputStream(hBuff);
    bodyWrapper = new DataOutputStream(bBuff);
    message = new Vector();
  }
  
  protected static YahooPacket createPacket()
  {
    if (instance == null)
      instance = new YahooPacket();
    instance.hBuff.reset();
    instance.bBuff.reset();
    instance.message.removeAllElements();
    
    return instance;
  }
  
  protected final YahooPacket append(String key, String value)
  {
    try
    {
      bodyWrapper.write(key.getBytes());
      bodyWrapper.write(SEPARATOR);
      bodyWrapper.write(value.getBytes());
      bodyWrapper.write(SEPARATOR);
      bodyWrapper.flush();
    } catch (IOException e){}
    return instance;
  }
  
  private final void generateHeader() throws IOException
  {
    headerWrapper.write(RESOURCE, 0, 4);
    headerWrapper.write(VERSION, 0, 4);
    headerWrapper.writeShort(bBuff.size() & 0xFFFF);
    headerWrapper.writeShort(service_ & 0xFFFF);
    headerWrapper.writeInt((int)(ystatus_ & 0xFFFFFFFF));
    headerWrapper.writeInt((int)(sessionId_ & 0xFFFFFFFF));
    headerWrapper.flush();
  }
  
  protected byte[] packPacket()
  {
    try
    {
      generateHeader();
    } catch (IOException e){}

    byte[] message = new byte[hBuff.size() + bBuff.size()];
    System.arraycopy(hBuff.toByteArray(), 0, message, 0, hBuff.size());
    System.arraycopy(bBuff.toByteArray(), 0, message, hBuff.size(), bBuff.size());
    dump(message, "OUT:");
    return message;
  }
  
  protected final YahooPacket setService(int service)
  {
    service_ = service;
    return instance;
  }
  
  protected final YahooPacket setStatus(long status)
  {
    ystatus_ = status;
    return instance;
  }
  
  protected final YahooPacket setSessionId(long sessionId)
  {
    sessionId_ = sessionId;
    return instance;
  }
  
  protected static YahooPacket parsePacket(byte[] packet)
  {
    System.out.println("[INFO-YAHOO] Packet received " + packet);
    if (packet == null)
      return null;
    dump(packet, "IN:");
    YahooPacket yahooPacket = createPacket();
    
    /* Check for resource name match and version match*/
    if (!Utils.compareByteArray(Utils.subArray(packet, 0, 4), RESOURCE))
      return null;

    if (yahooPacket.sessionId_ == 0L)
      yahooPacket.sessionId_ = Utils.bytesToInt(Utils.subArray(packet, 16, 4), true);
    /* Check for session ID match */
//    else if (yahooPacket.sessionId_ != Utils.bytesToInt(Utils.subArray(packet, 16, 4), true))
//      return null;
    
    /* Check body length */
    if (Utils.bytesToInt(Utils.subArray(packet, 8, 2), true) != packet.length - HEAD_LEN)
      return null;
    
    yahooPacket.service_ = Utils.bytesToInt(Utils.subArray(packet, 10, 2), true);
    yahooPacket.ystatus_ = Utils.bytesToInt(Utils.subArray(packet, 12, 4), true);
    
    /* Get strings in body */
    for (int i = HEAD_LEN, j = HEAD_LEN; i < packet.length; i++)
    {
      if (packet[i] == SEPARATOR[0] && packet[i+1] == SEPARATOR[1])
      {
        yahooPacket.message.addElement(new String(packet, j, i - j));
        i++; j = i + 1;
      }
    }
    
    return yahooPacket;
  }
  
  protected final String getValue(String key)
  {
    for (int i = 0; i < message.size(); i += 2)
      if (message.elementAt(i).equals(key))
        return (String)message.elementAt(i+1);
    return null;
  }
  
  protected final Vector getValues(String key)
  {
    Vector v = new Vector();
    for (int i = 0; i < message.size(); i += 2)
      if (message.elementAt(i).equals(key))
        v.addElement(message.elementAt(i+1));
    return v;
  }
  
  protected final boolean hasKey(String key)
  {
    for (int i = 0; i < message.size(); i += 2)
      if (message.elementAt(i).equals(key))
        return true;
    return false;
  }
  
  protected static Vector splitPacket(byte[] packets)
  {
    Vector v = new Vector();
    int pos = 0;
    while (pos < packets.length)
    {
      int len = Utils.bytesToInt(Utils.subArray(packets, pos + 8, 2), true);
      v.addElement(Utils.subArray(packets, pos, len + HEAD_LEN));
      pos += len + HEAD_LEN;
    }
    
    return v;
  }
  
  // -----------------------------------------------------------------
  // Mutex lock used simply to prevent different outputs getting entangled
  // -----------------------------------------------------------------
  private synchronized static void dump(byte[] array)
  { String s,c="";
    for(int i=0;i<array.length;i++)
    { s="0"+Integer.toHexString(array[i]);
      System.out.print(s.substring(s.length()-2)+" ");
      if(array[i]>=' ' && array[i]<='~')  c=c+(char)array[i];
        else  c=c+".";
      if((i+1)==array.length)
      { while((i%20)!=19) { System.out.print("   ");  i++; }
      }
      if( (((i+1)%20)==0) || ((i+1)>=array.length) )
      { System.out.print(" "+c+"\n");  c="";
      }
    }
  }

  public static void dump(byte[] array,String s)
  { System.out.println(s+"\n01-02-03-04-05-06-07-08-09-10-11-12-13-14-15-16-17-18-19-20");
    dump(array);
  }
  
  protected YahooPacket clone()
  {
    YahooPacket packet = new YahooPacket();
    try
    {
      packet.headerWrapper.write(hBuff.toByteArray());
      packet.bodyWrapper.write(bBuff.toByteArray());
      packet.setService(service_);
      packet.setSessionId(sessionId_);
      packet.setStatus(ystatus_);
      packet.message = new Vector();
      for (int i = 0; i < message.size(); i++)
        packet.message.addElement(message.elementAt(i));
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return packet;
  }
  
  protected void merge(YahooPacket p, String[] keys)
  {
    for (int i = 0; i < keys.length; i++)
    {
      String oldVal = getValue(keys[i]);
       oldVal += p.getValue(keys[i]);
    }
  }
  
  public static void main(String[] args)
  {
    byte[] message = YahooPacket.createPacket()
    .setService(YahooConstants.SERVICE_AUTH)
    .setStatus(YahooConstants.STATUS_BAD)
    .setSessionId(5L)
    .append("0", "aaa")
    .append("1", "bbb").packPacket();
    
//    System.out.println(new String(message));
    YahooPacket p = parsePacket(message);
    for (int i = 0; i < p.message.size(); i++)
    {
      System.out.print(i + ": ");
      System.out.println(p.message.elementAt(i));
    }
  }
}
