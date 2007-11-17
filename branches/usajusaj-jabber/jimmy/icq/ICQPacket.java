package jimmy.icq;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

import jimmy.util.Utils;
import jimmy.yahoo.YahooPacket;

/**
 * @author matej
 *
 */
public class ICQPacket
{
  private static final int HEAD_LEN = 6;

  private ByteArrayOutputStream hBuff = new ByteArrayOutputStream(HEAD_LEN);
  private ByteArrayOutputStream bBuff = new ByteArrayOutputStream(1024);
  
  private DataOutputStream headerWrapper;
  private DataOutputStream bodyWrapper;
  
  protected int channel;
  private static short sequenceNumber;
  private int dataLen;
  
  private Vector tlvs;
  
  private static ICQPacket instance;

  private ICQPacket()
  {
    headerWrapper = new DataOutputStream(hBuff);
    bodyWrapper = new DataOutputStream(bBuff);
  }
  
  protected static ICQPacket createPacket()
  {
    if (instance == null)
      instance = new ICQPacket();
    instance.hBuff.reset();
    instance.bBuff.reset();
    instance.dataLen = 0;
    instance.tlvs = new Vector();
    
    return instance;
  }
  
  protected static ICQPacket createFLAP(
      int channel,
      int dataLen)
  {
    ICQPacket packet = createPacket();
    try
    {
      packet.headerWrapper.writeByte(0x2a);
      packet.headerWrapper.writeByte(channel);
      short seqn = getNextSequenceNumber();
      System.out.println(seqn);
      packet.headerWrapper.writeShort(seqn);
      packet.headerWrapper.writeShort(dataLen);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return packet;
  }
  
  protected static ICQPacket createSNAC(
      int family, 
      int subtype, 
      int flag1, 
      int flag2, 
      int requestId)
  {
    ICQPacket packet = createPacket();
    try
    {
      packet.headerWrapper.writeShort(family);
      packet.headerWrapper.writeShort(subtype);
      packet.headerWrapper.writeByte(flag1);
      packet.headerWrapper.writeByte(flag2);
      packet.headerWrapper.writeInt(requestId);
      packet.headerWrapper.flush();
      
      return packet;
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
  }

  protected static ICQPacket parsePacket(byte[] packet)
  {
    System.out.println("[INFO-ICQ] Packet received " + packet);
    if (packet == null || 
        packet.length < 10 || 
        packet[0] != 0x2a)
    {
      if (packet != null)
        YahooPacket.dump(packet, "IN:");
      System.err.println("Header length check fail");
      return null;
    }
    YahooPacket.dump(packet, "IN:");
    ICQPacket icqPacket = createPacket();
    
    icqPacket.channel = packet[1] & 0xFFFF;
    short tmpSeq = (short)Utils.bytesToUShort(Utils.subArray(packet, 2, 2), true);
    if (tmpSeq >= 0)
      sequenceNumber = tmpSeq;
    icqPacket.dataLen = Utils.bytesToUShort(Utils.subArray(packet, 4, 2), true);
    
    if (packet.length != icqPacket.dataLen + 6)
    {
      System.err.println("Data length check fail");
      return null;
    }
    
    try {
      icqPacket.headerWrapper.write(Utils.subArray(packet, 0, 6));
      icqPacket.headerWrapper.flush();
      icqPacket.bodyWrapper.write(Utils.subArray(packet, 6, icqPacket.dataLen));
      icqPacket.bodyWrapper.flush();
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }
    
    return icqPacket;
  }
  
  protected void parseTLVs()
  {
    DataInputStream dis = new DataInputStream(
        new ByteArrayInputStream(bBuff.toByteArray()));
    
    try
    {
      while (dis.available() > 0)
      {
        short type = dis.readShort();
        if (type == 0x0013)
        {
          dis.skipBytes(2);
          continue;
        }
        short len = dis.readShort();
        if (len >= 0)
        {
          byte[] value = new byte[len];
          if (len != 0) dis.read(value);
          TLV tlv = new TLV(type, len, value);
          tlvs.addElement(tlv);
          tlv.toString();
        }
      }
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return;
    }
  }
  
  protected TLV getTLV(int type, int count)
  {
    for (int i = 0; i < tlvs.size(); i++)
    {
      TLV t = (TLV)tlvs.elementAt(i);
      if (t.type == type &&
          --count == 0)
        return t;
    }
    return null;
  }
  
  protected static ICQPacket createAuth1Packet(
      ICQProtocol protocol)
  {
    byte[] pass = protocol.getAccount().getPassword().getBytes();
    byte[] uname = protocol.getAccount().getUser().getBytes();
    final byte[] xor_table = {
        (byte) 0xf3, (byte) 0x26, (byte) 0x81, (byte) 0xc4,
        (byte) 0x39, (byte) 0x86, (byte) 0xdb, (byte) 0x92
      };
    final byte[] trail = {
        (byte) 0x00, (byte) 0x16, (byte) 0x00, (byte) 0x02, (byte) 0x01, 
        (byte) 0x0A, (byte) 0x00, (byte) 0x17, (byte) 0x00, (byte) 0x02, 
        (byte) 0x00, (byte) 0x04, (byte) 0x00, (byte) 0x18, (byte) 0x00, 
        (byte) 0x02, (byte) 0x00, (byte) 0x3F, (byte) 0x00, (byte) 0x19, 
        (byte) 0x00, (byte) 0x02, (byte) 0x00, (byte) 0x01, (byte) 0x00, 
        (byte) 0x1A, (byte) 0x00, (byte) 0x02, (byte) 0x0C, (byte) 0xCF, 
        (byte) 0x00, (byte) 0x14, (byte) 0x00, (byte) 0x04, (byte) 0x00, 
        (byte) 0x00, (byte) 0x00, (byte) 0x55, (byte) 0x00, (byte) 0x0F, 
        (byte) 0x00, (byte) 0x02, (byte) 0x65, (byte) 0x6E, (byte) 0x00, 
        (byte) 0x0E, (byte) 0x00, (byte) 0x02, (byte) 0x75, (byte) 0x73};

    int xorindex = 0;
    for (int i = 0; i < pass.length; i++)
    {
      pass[i] ^= xor_table[xorindex++];
      if (xorindex >= xor_table.length)
        xorindex = 0;
    }
    
    ICQPacket packet = createPacket();
    try
    {
      packet.headerWrapper.writeByte(0x2a);
      packet.headerWrapper.writeByte(1);
      packet.headerWrapper.writeShort(getNextSequenceNumber());
      packet.headerWrapper.writeShort(117 + pass.length + uname.length);
      packet.headerWrapper.flush();
      
      packet.bodyWrapper.writeInt(0x00000001);
      
      packet.bodyWrapper.writeShort(1);
      packet.bodyWrapper.writeShort(uname.length);
      packet.bodyWrapper.write(uname);
      
      packet.bodyWrapper.writeShort(2);
      packet.bodyWrapper.writeShort(pass.length);
      packet.bodyWrapper.write(pass);
      
      packet.bodyWrapper.writeShort(3);
      packet.bodyWrapper.writeShort(protocol.cli_id.length());
      packet.bodyWrapper.write(protocol.cli_id.getBytes());
      
      packet.bodyWrapper.write(trail);
      packet.bodyWrapper.flush();
    }
    catch (IOException e)
    {
      e.printStackTrace();
      return null;
    }
    
    return packet;
  }
  
  protected static ICQPacket createLoginPacket(byte[] cookie)
  {
    ICQPacket packet = createPacket();
    try
    {
      packet.headerWrapper.writeByte(0x2a);
      packet.headerWrapper.writeByte(1);
      packet.headerWrapper.writeShort(getNextSequenceNumber());
      packet.headerWrapper.writeShort(8 + cookie.length);
      packet.headerWrapper.flush();
      
      packet.bodyWrapper.writeShort(0x0000);
      packet.bodyWrapper.writeShort(0x0001);
      packet.bodyWrapper.writeShort(0x0006);
      packet.bodyWrapper.writeShort(cookie.length);
      packet.bodyWrapper.write(cookie);
      packet.bodyWrapper.flush();
      
      return packet;
    }
    catch (IOException e)
    {
      e.printStackTrace();
    }
    
    return null;
  }
  
  protected final ICQPacket append(byte[] body)
  {
    try
    {
      bodyWrapper.write(body);
      bodyWrapper.flush();
    } catch (IOException e){}
    return instance;
  }
  
  protected void sendSNAC(ICQConnector conn)
  {
    byte[] data = packPacket();
    conn.sendRequest(ICQPacket.createFLAP(2, data.length).append(data).packPacket());
  }
  
  protected byte[] packPacket()
  {
    byte[] message = new byte[hBuff.size() + bBuff.size()];
    System.arraycopy(hBuff.toByteArray(), 0, message, 0, hBuff.size());
    System.arraycopy(bBuff.toByteArray(), 0, message, hBuff.size(), bBuff.size());
    YahooPacket.dump(message, "OUT:");
    return message;
  }

  protected static short getNextSequenceNumber()
  {
    return ++sequenceNumber;
  }
  
  protected short getFamily()
  {
    return Utils.bytesToShort(Utils.subArray(bBuff.toByteArray(), 0, 2), true);
  }
  
  protected short getSubType()
  {
    return Utils.bytesToShort(Utils.subArray(bBuff.toByteArray(), 2, 2), true);
  }
  
  class TLV
  {
    short type;
    short len;
    byte[] value;
    
    public TLV(short t, short l, byte[] v)
    {
      type = t;
      len = l;
      value = v;
    }
    
    public String toString()
    {
      System.out.println(new StringBuffer().append("type=").append(type).append(";len=").append(len).append(";val=").toString());
      YahooPacket.dump(value, "");
      return null;
    }
  }
}
