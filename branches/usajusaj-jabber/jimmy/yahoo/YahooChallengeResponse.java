/**
 * 
 */
package jimmy.yahoo;

import java.io.DataInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jimmy.util.Utils;

public class YahooChallengeResponse
{ // -----These lookup tables are used in decoding the challenge string
  private final static String ALPHANUM_LOOKUP = "qzec2tb3um1olpar8whx4dfgijknsvy5"; // 32
                                                                                    // chars
  private final static String OPERATORS_LOOKUP = "+|&%/*^-"; // 8 chars
  // -----These lookup tables are used in encoding the response strings
  private final static String ENCODE1_LOOKUP = "FBZDWAGHrJTLMNOPpRSKUVEXYChImkwQ"; // 32
                                                                                    // chars
  private final static String ENCODE2_LOOKUP = "F0E1D2C3B4A59687abcdefghijklmnop"; // Ditto
  private final static String ENCODE3_LOOKUP = ",;";
  
  private final static boolean DB = false; // Debug
  private final static boolean DB2 = false; // Debug deep
  
  // -----Buffer for binary data, and resource filename
  private static byte[] data;
  private final static String BIN_FILE = "/jimmy/media/yahoo_challenge.bin";
  
  public static final char[] TABLE_OFFSETS =
    {0, 256, 512, 544, 800, 832, 1088, 1120, 1376, 1408, // Tables 0 - 9 (1408)
    1664, 1696, 1952, 1984, 2240, 2272, 2528, 2784, 3040, 3296, // Tables 10 -
                                                                // 19 (1888)
    3552, 3584, 3840, 3872, 4128, 4384, 4416, 4448, 4704, 4736, // Tables 20 -
                                                                // 29 (1440)
    4992, 5024, 5280, 5536, 5792, 6048, 6304, 6560, 6816, 7072, // Tables 30 -
                                                                // 39 (2336)
    7104, 7136, 7168, 7200, 7456, 7488, 7744, 8000, 8032, 8064, // Tables 40 -
                                                                // 49 (992)
    8096, 8128, 8160, 8416, 8672, 8704, 8736, 8992, 9248, 9504, // Tables 50 -
                                                                // 59 (1440)
    9760, 9792, 10048, 10304, 10560, 10592, 10624, 10656, 10912, 11168, // Tables
                                                                        // 60 -
                                                                        // 69
                                                                        // (1664)
    11424, 11680, 11712, 11968, 12224, 12480, 12736, 12992, 13024, 13056, // Tables
                                                                          // 70 -
                                                                          // 79
                                                                          // (1888)
    13088, 13344, 13376, 13632, 13664, 13696, 13952, 13984, 14016, 14048, // Tables
                                                                          // 80 -
                                                                          // 89
                                                                          // (992)
    14080, 14336, 14368, 14400, 14656, 14688, 14720, 14976, 15232, 15488, // Tables
                                                                          // 90 -
                                                                          // 99
                                                                          // (1440)
    15520, 15776, 15808, 15840, 15872, 16128, 16384, 16416, 16448, 16704, // Tables
                                                                          // 100
                                                                          // -
                                                                          // 109
                                                                          // (1216)
    16960, 17216, 17248, 17504, 17760, 17792, 18048, 18080, 18336, 18592, // Tables
                                                                          // 110
                                                                          // -
                                                                          // 119
                                                                          // (1888)
    18624, 18880, 18912, 19168, 19424, 19680, 19712, 19744, 20000, 20032, // Tables
                                                                          // 120
                                                                          // -
                                                                          // 129
                                                                          // (1440)
    20288, 20320, 20352, 20608, 20864, 20896, 21152, 21408, 21440, 21472, // Tables
                                                                          // 130
                                                                          // -
                                                                          // 139
                                                                          // (1440)
    21504, 21536, 21792, 22048, 22080, 22112, 22368, 22624, 22656, 22912, // Tables
                                                                          // 140
                                                                          // -
                                                                          // 149
                                                                          // (1440)
    22944, 23200, 23456, 23488, 23520, 23776, 24032, 24288, 24320, 24576, // Tables
                                                                          // 150
                                                                          // -
                                                                          // 159
                                                                          // (1664)
    24832, 25088, 25344, 25600, 25632, 25664, 25920, 26176, 26432, 26688, // Tables
                                                                          // 160
                                                                          // -
                                                                          // 169
                                                                          // (2112)
    26944, 27200, 27232, 27264, 27296, 27328, 27360, 27392, 27648, 27904, // Tables
                                                                          // 170
                                                                          // -
                                                                          // 179
                                                                          // (1216)
    28160, 28416, 28672, 28928, 28960, 28992, 29248, 29504, // Tables 180 - 187
                                                            // (1632)
    29536 // Length of file
    };
  
  // -----------------------------------------------------------------
  // Load binary data tables as a resource from /ysmg/network/BIN_FILE
  // -----------------------------------------------------------------
  static
  {
    try
    { // -----Open stream to resource located next to this class
    // Class v10 = Class.forName("ymsg.network.ChallengeResponseV10");
    // DataInputStream dis = new
    // DataInputStream(v10.getResourceAsStream(BIN_FILE));
      Class challenge = Class.forName("jimmy.yahoo.YahooChallengeResponse");
      DataInputStream dis = new DataInputStream(challenge.getResourceAsStream(BIN_FILE));
      // DataInputStream dis = new DataInputStream(new
      // FileInputStream(BIN_FILE));
      data = new byte[dis.available()];
      // -----Extra entry at end of offset table has entire file size
      if (data.length < TABLE_OFFSETS[TABLE_OFFSETS.length - 1])
        throw new Exception("Data too short?");
      // -----Read binary data into array
      dis.readFully(data);
      dis.close();
    }
    catch (Exception e)
    {
      System.err.println("Error loading resource file: " + BIN_FILE);
      e.printStackTrace();
    }
  }
  
  // -----------------------------------------------------------------
  // Given a username, password and challenge string, this code returns
  // the two valid response strings needed to login to Yahoo
  // -----------------------------------------------------------------
  static String[] getStrings(String username, String password, String challenge) throws NoSuchAlgorithmException
  {
    int operand = 0, i;
    
    // -----Count the number of operator characters, as this determines the
    // -----size of our magic bytes array
    int cnt = 0;
    for (i = 0; i < challenge.length(); i++)
      if (isOperator(challenge.charAt(i)))
        cnt++;
    
    int[] magic = new int[cnt];
    
    // -----PART ONE : Store operands, and OR them with operators.
    // -----(Note: ignore brackets, they are just there to confuse - making
    // -----the challenge string look like an expression of some sort!)
    cnt = 0;
    for (i = 0; i < challenge.length(); i++)
    {
      char c = challenge.charAt(i);
      if (Utils.isLetter(c) || Character.isDigit(c))
      {
        operand = ALPHANUM_LOOKUP.indexOf(c) << 3; // 0-31, shifted to high 5
                                                    // bits
      }
      else if (isOperator(c))
      {
        int a = OPERATORS_LOOKUP.indexOf(c); // 0-7
        magic[cnt] = (operand | a) & 0xff; // Mask with operand
        cnt++;
      }
    }
    if (DB)
      dump("P1", magic);
    
    // -----PART TWO : Monkey around with the data
    for (i = magic.length - 2; i >= 0; i--)
    {
      int a = magic[i], b = magic[i + 1];
      a = ((a * 0xcd) ^ b) & 0xff;
      magic[i + 1] = a;
    }
    if (DB)
      dump("P2", magic);
    
    // -----PART THREE : Create 20 byte buffer, copy first 4 bytes into arrays
    byte[] comparison = _part3Munge(magic); // 20 bytes
    long seed = 0; // First 4 bytes reversed
    byte[] binLookup = new byte[7]; // First 4 plus 3 empty
    for (i = 0; i < 4; i++)
    {
      seed = seed << 8;
      seed += comparison[3 - i] & 0xff;
      binLookup[i] = (byte)(comparison[i] & 0xff);
    }
    if (DB)
      dump("P3.1", comparison);
    
    // -----PART THREE AND A BIT : Binary table lookup params
    int table = 0, depth = 0;
    synchronized (md5Obj)
    {
      for (i = 0; i < 0xffff; i++)
      {
        for (int j = 0; j < 5; j++)
        {
          binLookup[4] = (byte)(i & 0xff);
          binLookup[5] = (byte)((i >> 8) & 0xff);
          binLookup[6] = (byte)j;
          byte[] result = md5Singleton(binLookup);
          if (_part3Compare(result, comparison) == true)
          {
            depth = i;
            table = j;
            i = 0xffff;
            j = 5; // Exit loops
          }
        }
      }
    }
    if (DB)
      System.out.println("P3.2: " + depth + " " + table + ": ");
    
    // -----PART THREE AND A BIT MORE : Do binary table lookup
    byte[] magicValue = new byte[4];
    if (DB)
      System.out.println("P3.3.a: " + seed);
    seed = _part3Lookup(table, depth, seed); // Check PART THREE for
    if (DB)
      System.out.println("P3.3.b: " + seed);
    seed = _part3Lookup(table, depth, seed); // seed generation
    if (DB)
      System.out.println("P3.3.c: " + seed);
    for (i = 0; i < magicValue.length; i++)
    {
      magicValue[i] = (byte)(seed & 0xff);
      seed = seed >> 8;
    }
    
    // -----PART FOUR : This bit is copied from the start of V9
    String regular = yahoo64(md5(password));
    String crypted = yahoo64(md5(md5Crypt(password, "$1$_2S43d5f")));
    if (DB)
      System.out.println("P4.1 " + regular + " " + crypted);
    // -----And now for some more hashing, this time with SHA-1
    boolean hackSha1 = (table >= 3); // June 2004
    String[] s = new String[2];
    s[0] = _part4Encode(_part4Hash(regular, magicValue, hackSha1));
    s[1] = _part4Encode(_part4Hash(crypted, magicValue, hackSha1));
    if (DB)
      System.out.println("FINAL " + s[0] + " " + s[1]);
    
    return s;
  }
  
  // -----------------------------------------------------------------
  private static byte[] _part3Munge(int[] magic)
  {
    int res, i = 1;
    byte[] comparison = new byte[20];
    // -----Add two bytes at a time to fill up array
    try
    {
      for (int c = 0; c < comparison.length; c += 2)
      {
        int a, b;
        a = magic[i++];
        if (a <= 0x7f) // Bit 8 set?
        {
          res = a;
        }
        else
        // Bit 8 unset?
        { // -----Munge data: offset and offset+1
          if (a >= 0xe0) // >=224? (%11100000)
          {
            b = magic[i++]; // Next byte from 'magic'
            a = (a & 0x0f) << 6; // Bits 10-7 from low 4 bits of 'a'
            b = b & 0x3f; // Bits 6-1 from low 6 bits of 'b'
            res = (a + b) << 6; // Combine/shift: aaaabbbbbb000000
          }
          else
          {
            res = (a & 0x1f) << 6; // Shift: 0000aaaaaa000000
          }
          // -----Munge data: result and next 'magic' byte
          res += (magic[i++] & 0x3f);
        }
        // -----Bits 16-1 are places into next two array slots
        comparison[c] = (byte)((res & 0xff00) >> 8); // 16-9 bits to [a+0]
        comparison[c + 1] = (byte)(res & 0xff); // 8-1 bits to [a+1];
      }
    }
    catch (ArrayIndexOutOfBoundsException e)
    {
      e.printStackTrace();
    }
    return comparison;
  }
  
  private static int _part3Lookup(int table, int depth, long seed)
  {
    int offset = 0; // Offset into data table
    long a, b, c; // Temp variables
    
    long idx = seed; // Choose table entry (unsigned int)
    int iseed = (int)seed; // 32 bit *signed*
    for (int i = 0; i < depth; i++)
    { // -----Table 0 (full of IDENT no-ops) deleted to save space
      if (table == 0)
        return iseed;
      // -----Get op from table (idx is an 'unsigned int', so adjust if
      // necessary!)
      if (idx < 0)
        idx += 0x100000000L; // More sign bit to bit #32
      int[] opArr = OPS[table][(int)(idx % 96)];
      if (DB2)
        System.out.println("LOOK1:" + table + " " + depth + " " + iseed + ":" + idx + " " + opArr[OP]);
      switch (opArr[OP])
      { // case IDENT : // Removed: see the 'if'
        // return iseed; // condition at top of loop
        case XOR:
          // Bitwise XOR
          iseed ^= opArr[ARG1];
          break;
        case MULADD:
          // Multiply and add
          iseed = iseed * opArr[ARG1] + opArr[ARG2];
          break;
        case LOOKUP:
          // Arg 1 determines which table in the binary data
          offset = TABLE_OFFSETS[opArr[ARG1]];
          // -----Replace each of the four seed bytes with the byte
          // -----at that offset in the 256 byte table of arg1
          b = _data(offset, (iseed & 0xff)) | _data(offset, (iseed >> 8) & 0xff) << 8 | _data(offset, (iseed >> 16) & 0xff) << 16 | _data(offset, (iseed >> 24) & 0xff) << 24;
          iseed = (int)b;
          break;
        case BITFLD:
          // Arg 1 determines which table in the binary data
          offset = TABLE_OFFSETS[opArr[ARG1]];
          c = 0;
          // -----All 32 bytes in table
          for (int j = 0; j < 32; j++)
          { // -----Move j'th bit to position data[j];
            a = ((iseed >> j) & 1) << _data(offset, j);
            // -----Mask out data[j]'th bit
            b = ~(1 << _data(offset, j)) & c;
            // -----Combine
            c = a | b;
          }
          iseed = (int)c;
          break;
      }
      // -----Last run of the loop? Don't do final part!
      if (depth - i <= 1)
        return iseed;
      if (DB2)
        System.out.println("LOOK2:" + iseed + ":" + idx);
      // -----Bit more mesing about with the seed and table index before
      // -----we loop again. Mess about with each byte in copy of seed
      // -----to get new idx, then scale up the seed.
      a = 0;
      c = iseed;
      for (int j = 0; j < 4; j++)
      {
        a = (a ^ c & 0xff) * 0x9e3779b1;
        c = c >> 8;
      }
      idx = (int)((((a ^ (a >> 8)) >> 16) ^ a) ^ (a >> 8)) & 0xff;
      iseed = iseed * 0x00010dcd;
      if (DB2)
        System.out.println("LOOK3:" + iseed + ":" + idx);
    }
    // -----Should return inside loop before we reach this point
    return iseed;
  }
  
  private final static int _data(int offset, int idx) // Final for speed
  {
    return data[offset + idx] & 0xff;
  }
  
  private final static boolean _part3Compare(byte[] a, byte[] b) // Final for
                                                                  // speed
  {
    for (int i = 0; i < 16; i++)
      if (a[i] != b[i + 4])
        return false;
    return true;
  }
  
  // Each 16 bit value (2 bytes) is encoded into three chars, 5 bits per
  // char, with the least sig bit either ',' or ';' - a equals is inserted
  // in the middle to make it look like an assignment.
  private static String _part4Encode(byte[] buffer)
  {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < buffer.length; i += 2)
    { // -----16 bit value from bytes i and i+1
      int a = ((buffer[i] & 0xff) << 8) + (buffer[i + 1] & 0xff);
      // -----Bits 16 to 12 from first table, followed by a '='
      sb.append(ENCODE1_LOOKUP.charAt((a >> 11) & 0x1f));
      sb.append('=');
      // -----Bits 11 to 7, then 6 to 2, from second table
      sb.append(ENCODE2_LOOKUP.charAt((a >> 6) & 0x1f));
      sb.append(ENCODE2_LOOKUP.charAt((a >> 1) & 0x1f));
      // -----Bit 1 is taken from third table
      sb.append(ENCODE3_LOOKUP.charAt(a & 0x01));
    }
    return sb.toString();
  }
  
  private static byte[] _part4Hash(String target, byte[] magicValue, boolean hackSha1) throws NoSuchAlgorithmException
  { // -----Convert string to 64 byte arrays with padding
    byte[] xor1 = _part4Xor(target, 0x36);
    byte[] xor2 = _part4Xor(target, 0x5c);
    if (DB)
    {
      dump("P4.2", xor1);
      dump("P4.2", xor2);
      dump("P4.2", magicValue);
    }
    // -----Hash with SHA-1, first hash feeds into second
    
    YahooSHA1 sha1 = new YahooSHA1();
    sha1.update(xor1);
    if (hackSha1)
      sha1.setBitCount(0x1ff); // 24th June 2004 'mod'
    sha1.update(magicValue);
    byte[] digest1 = sha1.digest();
    sha1.reset();
    sha1.update(xor2);
    sha1.update(digest1);
    byte[] digest2 = sha1.digest();
    if (DB)
    {
      dump("P4.3", digest1);
      dump("P4.3", digest2);
    }
    return digest2;
  }
  
  private static byte[] _part4Xor(String s, int op)
  {
    byte[] arr = new byte[64];
    // -----XOR against op
    for (int i = 0; i < s.length(); i++)
      arr[i] = (byte)(s.charAt(i) ^ op);
    // -----Pad remainder of 64 bytes with op
    for (int i = s.length(); i < arr.length; i++)
      arr[i] = (byte)op;
    return arr;
  }
  
  // -----------------------------------------------------------------
  // Is c one of the eight operators chars?
  // -----------------------------------------------------------------
  private static boolean isOperator(char c)
  {
    return (OPERATORS_LOOKUP.indexOf(c) >= 0);
  }
  
  // -----------------------------------------------------------------
  // DEBUG code
  // -----------------------------------------------------------------
  static void dump(String title, int[] data)
  {
    int idx = 0;
    System.out.println(title);
    while (idx < data.length)
    {
      String s = Integer.toHexString(data[idx]);
      if (s.length() < 2)
        s = "0" + s;
      System.out.print(s + " ");
      idx++;
      if ((idx % 20) == 0)
        System.out.print("\n");
    }
    if ((idx % 20) != 0)
      System.out.print("\n");
  }
  
  static void dump(String title, byte[] data)
  {
    int idx = 0;
    System.out.println(title);
    while (idx < data.length)
    {
      String s = Integer.toHexString(data[idx] & 0xff);
      if (s.length() < 2)
        s = "0" + s;
      System.out.print(s + " ");
      idx++;
      if ((idx % 20) == 0)
        System.out.print("\n");
    }
    if ((idx % 20) != 0)
      System.out.print("\n");
  }
  
  /*
   * public static void main(String[] args) { try { System.out.println(args[0]+"
   * "+args[1]+" "+args[2]); String[] s =
   * ChallengeResponseV10.getStrings(args[0],args[1],args[2]);
   * System.out.println("["+s[0]+"]\n ["+s[1]+"]"); }catch(Exception e) {
   * e.printStackTrace(); } }
   */

  private final static String Y64 = "ABCDEFGHIJKLMNOPQRSTUVWXYZ" + "abcdefghijklmnopqrstuvwxyz" + "0123456789._";
  
  protected static MessageDigest md5Obj;
  
  // -----------------------------------------------------------------
  // Create a singleton md5 object for all our hashing needs (well, most!)
  // -----------------------------------------------------------------
  static
  {
    try
    {
      md5Obj = MessageDigest.getInstance("MD5");
    }
    catch (NoSuchAlgorithmException e)
    {
      e.printStackTrace();
    }
  }
  
  // -----------------------------------------------------------------
  // Yahoo uses its own custom variation on Base64 encoding (although a
  // little birdy tells me this routine actually comes from the Apple Mac?)
  //
  // For those not familiar with Base64 etc, all this does is treat an
  // array of bytes as a bit stream, sectioning the stream up into six
  // bit slices, which can be represented by the 64 characters in the
  // 'table' Y64 above. In this fashion raw binary data can be expressed
  // as valid 7 bit printable ASCII - although the size of the data will
  // expand by 25% - three bytes (24 bits) taking up four ASCII characters.
  // Now obviously the bit stream will terminate mid way throught an ASCII
  // character if the input array size isn't evenly divisible by 3. To
  // flag this, either one or two dashes are appended to the output. A
  // single dash if we're two over, and two dashes if we're only one over.
  // (No dashes are appended if the input size evenly divides by 3.)
  // -----------------------------------------------------------------
  static String yahoo64(byte[] buffer)
  {
    int limit = buffer.length - (buffer.length % 3);
    String out = "";
    int[] buff = new int[buffer.length];
    
    for (int i = 0; i < buffer.length; i++)
      buff[i] = buffer[i] & 0xff;
    
    for (int i = 0; i < limit; i += 3)
    { // -----Top 6 bits of first byte
      out = out + Y64.charAt(buff[i] >> 2);
      // -----Bottom 2 bits of first byte append to top 4 bits of second
      out = out + Y64.charAt(((buff[i] << 4) & 0x30) | (buff[i + 1] >> 4));
      // -----Bottom 4 bits of second byte appended to top 2 bits of third
      out = out + Y64.charAt(((buff[i + 1] << 2) & 0x3c) | (buff[i + 2] >> 6));
      // -----Bottom six bits of third byte
      out = out + Y64.charAt(buff[i + 2] & 0x3f);
    }
    
    // -----Do we still have a remaining 1 or 2 bytes left?
    int i = limit;
    switch (buff.length - i)
    {
      case 1:
        // -----Top 6 bits of first byte
        out = out + Y64.charAt(buff[i] >> 2);
        // -----Bottom 2 bits of first byte
        out = out + Y64.charAt(((buff[i] << 4) & 0x30));
        out = out + "--";
        break;
      case 2:
        // -----Top 6 bits of first byte
        out = out + Y64.charAt(buff[i] >> 2);
        // -----Bottom 2 bits of first byte append to top 4 bits of second
        out = out + Y64.charAt(((buff[i] << 4) & 0x30) | (buff[i + 1] >> 4));
        // -----Bottom 4 bits of second byte
        out = out + Y64.charAt(((buff[i + 1] << 2) & 0x3c));
        out = out + "-";
        break;
    }
    
    return out;
  }
  
  // -----------------------------------------------------------------
  // Return the MD5 or a string and byte array (note: md5Singleton()
  // is easier on the object heap, but is NOT thread safe. It's ideal
  // for doing lots of hashing inside a tight loop - but remember to
  // mutex lock 'md5Obj' before using it!)
  // -----------------------------------------------------------------
  static byte[] md5(String s) throws NoSuchAlgorithmException
  {
    return md5(s.getBytes());
  }
  
  static byte[] md5(byte[] buff) throws NoSuchAlgorithmException
  {
    MessageDigest md = MessageDigest.getInstance("MD5");
    md.update(buff, 0, buff.length);
    return Utils.digest(md);
  }
  
  static byte[] md5Singleton(byte[] buff) throws NoSuchAlgorithmException
  {
    md5Obj.reset();
    md5Obj.update(buff, 0, buff.length);
    return Utils.digest(md5Obj);
  }
  
  // -----------------------------------------------------------------
  // Return the MD5Crypt of a string and salt
  // -----------------------------------------------------------------
  static byte[] md5Crypt(String k, String s)
  {
    return YahooUnixMD5Crypt.crypt(k, s).getBytes();
  }
  
  // -----Defines for constants in table below
  public final static int IDENT = 1;
  public final static int XOR = 2;
  public final static int MULADD = 3;
  public final static int LOOKUP = 4;
  public final static int BITFLD = 5;
  // -----Const names for three entries in inner array of table below
  public final static int OP = 0;
  public final static int ARG1 = 1;
  public final static int ARG2 = 2;
  
  // -----Five tables, each with 96 entries, each entry of three ints
  // -----[5][96][3] ... However, because Java supports non-rectangular
  // -----arrays we don't have to include all three entries for ops
  // -----which use only one argument - reducing class size.
  public final static int[][][] OPS =
    {
      {{} // Table 0
      // All 96 entries in this table are { IDENT,0,0 } ... effect-
      // ively NO-OPs. These have been removed to reduce class size
      // and replaced by a line of code trapping table 0 lookups.
      },
      {
        {MULADD, 0x36056CD7, 0x4387 }, // Table 1
        {LOOKUP, 0 },
        {LOOKUP, 1 },
        {BITFLD, 2 },
        {LOOKUP, 3 },
        {BITFLD, 4 },
        {MULADD, 0x4ABB534D, 0x3769 },
        {XOR, 0x1D242DA5 },
        {MULADD, 0x3C23132D, 0x339B },
        {XOR, 0x0191265C },
        {XOR, 0x3DB979DB },
        {LOOKUP, 5 },
        {XOR, 0x1A550E1E },
        {XOR, 0x2F140A2D },
        {MULADD, 0x7C466A4B, 0x29BF },
        {XOR, 0x2D3F30D3 },
        {MULADD, 0x7E823B21, 0x6BB3 },
        {BITFLD, 6 },
        {LOOKUP, 7 },
        {BITFLD, 8 },
        {LOOKUP, 9 },
        {BITFLD, 10 },
        {LOOKUP, 11 },
        {BITFLD, 12 },
        {LOOKUP, 13 },
        {BITFLD, 14 },
        {MULADD, 0x5B756AB9, 0x7E9B },
        {LOOKUP, 15 },
        {XOR, 0x1D1C4911 },
        {LOOKUP, 16 },
        {LOOKUP, 17 },
        {XOR, 0x46BD7771 },
        {XOR, 0x51AE2B42 },
        {MULADD, 0x2417591B, 0x177B },
        {MULADD, 0x57F27C5F, 0x2433 },
        {LOOKUP, 18 },
        {LOOKUP, 19 },
        {XOR, 0x71422261 },
        {BITFLD, 20 },
        {MULADD, 0x58E937F9, 0x1075 },
        {LOOKUP, 21 },
        {BITFLD, 22 },
        {LOOKUP, 23 },
        {LOOKUP, 24 },
        {MULADD, 0x0B4C3D13, 0x1597 },
        {BITFLD, 25 },
        {XOR, 0x0FE07D38 },
        {MULADD, 0x689B4017, 0x3CFB },
        {BITFLD, 26 },
        {LOOKUP, 27 },
        {XOR, 0x35413DF3 },
        {MULADD, 0x05B611AB, 0x570B },
        {MULADD, 0x0DA5334F, 0x3AC7 },
        {XOR, 0x47706008 },
        {BITFLD, 28 },
        {LOOKUP, 29 },
        {BITFLD, 30 },
        {XOR, 0x57611B36 },
        {MULADD, 0x314C2CD1, 0x2B5B },
        {XOR, 0x1EF33946 },
        {MULADD, 0x28EA041F, 0x638F },
        {LOOKUP, 31 },
        {LOOKUP, 32 },
        {LOOKUP, 33 },
        {MULADD, 0x511537CB, 0x7135 },
        {MULADD, 0x1CF71007, 0x5E17 },
        {XOR, 0x583D4BCF },
        {LOOKUP, 34 },
        {XOR, 0x373E6856 },
        {MULADD, 0x4D595519, 0x1A7D },
        {LOOKUP, 35 },
        {LOOKUP, 36 },
        {XOR, 0x0E2A36A7 },
        {LOOKUP, 37 },
        {LOOKUP, 38 },
        {BITFLD, 39 },
        {BITFLD, 40 },
        {XOR, 0x53F3604F },
        {BITFLD, 41 },
        {BITFLD, 42 },
        {MULADD, 0x1EDC0BA3, 0x7531 },
        {LOOKUP, 43 },
        {XOR, 0x10DF1038 },
        {BITFLD, 44 },
        {LOOKUP, 45 },
        {XOR, 0x4EDE0CAC },
        {MULADD, 0x2F076EEB, 0x5BCF },
        {XOR, 0x6D86030F },
        {XOR, 0x3F331713 },
        {LOOKUP, 46 },
        {MULADD, 0x41CD726F, 0x3F79 },
        {BITFLD, 47 },
        {XOR, 0x0ECE0054 },
        {MULADD, 0x19B32B03, 0x4AD1 },
        {BITFLD, 48 },
        {BITFLD, 49, 0 } },
      {
        {MULADD, 0x39731111, 0x419B }, // Table 2
        {XOR, 0x54F7757A },
        {BITFLD, 50 },
        {BITFLD, 51 },
        {LOOKUP, 52 },
        {LOOKUP, 53 },
        {MULADD, 0x3CC0256B, 0x7CE7 },
        {XOR, 0x79991847 },
        {MULADD, 0x228F7FB5, 0x472D },
        {MULADD, 0x32DA290B, 0x7745 },
        {XOR, 0x7A28180D },
        {BITFLD, 54 },
        {BITFLD, 55 },
        {MULADD, 0x5C814F8B, 0x227F },
        {LOOKUP, 56 },
        {MULADD, 0x0B496F6D, 0x412D },
        {XOR, 0x6F4B62DA },
        {LOOKUP, 57 },
        {XOR, 0x64973977 },
        {LOOKUP, 58 },
        {LOOKUP, 59 },
        {BITFLD, 60 },
        {LOOKUP, 61 },
        {LOOKUP, 62 },
        {XOR, 0x6DD14C92 },
        {LOOKUP, 63 },
        {BITFLD, 64 },
        {BITFLD, 65 },
        {BITFLD, 66 },
        {LOOKUP, 67 },
        {XOR, 0x5E6324D8 },
        {LOOKUP, 68 },
        {LOOKUP, 69 },
        {LOOKUP, 70 },
        {BITFLD, 71 },
        {XOR, 0x62745ED0 },
        {MULADD, 0x102C215B, 0x0581 },
        {LOOKUP, 72 },
        {LOOKUP, 73 },
        {LOOKUP, 74 },
        {MULADD, 0x19511111, 0x12C1 },
        {LOOKUP, 75 },
        {MULADD, 0x2A6E2953, 0x6977 },
        {LOOKUP, 76 },
        {XOR, 0x55CD5445 },
        {BITFLD, 77 },
        {BITFLD, 78 },
        {MULADD, 0x646C21EB, 0x43E5 },
        {XOR, 0x71DC4898 },
        {XOR, 0x167519CB },
        {XOR, 0x6D3158F8 },
        {XOR, 0x7EA95BEA },
        {BITFLD, 79 },
        {XOR, 0x47377587 },
        {XOR, 0x2D8B6E8F },
        {MULADD, 0x5E6105DB, 0x1605 },
        {XOR, 0x65B543C8 },
        {LOOKUP, 80 },
        {BITFLD, 81 },
        {MULADD, 0x48AF73CB, 0x0A67 },
        {XOR, 0x4FB96154 },
        {LOOKUP, 82 },
        {BITFLD, 83 },
        {XOR, 0x622C4954 },
        {BITFLD, 84 },
        {XOR, 0x20D220F3 },
        {XOR, 0x361D4F0D },
        {XOR, 0x2B2000D1 },
        {XOR, 0x6FB8593E },
        {LOOKUP, 85 },
        {BITFLD, 86 },
        {XOR, 0x2B7F7DFC },
        {MULADD, 0x5FC41A57, 0x0693 },
        {MULADD, 0x17154387, 0x2489 },
        {BITFLD, 87 },
        {BITFLD, 88 },
        {BITFLD, 89 },
        {LOOKUP, 90 },
        {XOR, 0x7E221470 },
        {XOR, 0x7A600061 },
        {BITFLD, 91 },
        {BITFLD, 92 },
        {LOOKUP, 93 },
        {BITFLD, 94 },
        {MULADD, 0x00E813A5, 0x2CE5 },
        {MULADD, 0x3D707E25, 0x3827 },
        {MULADD, 0x77A53E07, 0x6A5F },
        {BITFLD, 95 },
        {LOOKUP, 96 },
        {LOOKUP, 97 },
        {XOR, 0x43A73788 },
        {LOOKUP, 98 },
        {BITFLD, 99 },
        {LOOKUP, 100 },
        {XOR, 0x55F4606B },
        {BITFLD, 101, 0 } },
      {
        {BITFLD, 102 }, // Table 3
        {MULADD, 0x32CA58E3, 0x04F9 },
        {XOR, 0x11756B30 },
        {MULADD, 0x218B2569, 0x5DB1 },
        {XOR, 0x77D64B90 },
        {BITFLD, 103 },
        {LOOKUP, 104 },
        {MULADD, 0x7D1428CB, 0x3D },
        {XOR, 0x6F872C49 },
        {XOR, 0x2E484655 },
        {MULADD, 0x1E3349F7, 0x41F5 },
        {LOOKUP, 105 },
        {BITFLD, 106 },
        {XOR, 0x61640311 },
        {BITFLD, 107 },
        {LOOKUP, 108 },
        {LOOKUP, 109 },
        {LOOKUP, 110 },
        {XOR, 0x007044D3 },
        {BITFLD, 111 },
        {MULADD, 0x5C221625, 0x576F },
        {LOOKUP, 112 },
        {LOOKUP, 113 },
        {XOR, 0x2D406BB1 },
        {MULADD, 0x680B1F17, 0x12CD },
        {BITFLD, 114 },
        {MULADD, 0x12564D55, 0x32B9 },
        {MULADD, 0x21A67897, 0x6BAB },
        {LOOKUP, 115 },
        {MULADD, 0x06405119, 0x7143 },
        {XOR, 0x351D01ED },
        {MULADD, 0x46356F6B, 0x0A49 },
        {MULADD, 0x32C77969, 0x72F3 },
        {BITFLD, 116 },
        {LOOKUP, 117 },
        {LOOKUP, 118 },
        {BITFLD, 119 },
        {LOOKUP, 120 },
        {BITFLD, 121 },
        {MULADD, 0x74D52C55, 0x5F43 },
        {XOR, 0x26201CA8 },
        {XOR, 0x7AEB3255 },
        {LOOKUP, 122 },
        {MULADD, 0x578F1047, 0x640B },
        {LOOKUP, 123 },
        {LOOKUP, 124 },
        {BITFLD, 125 },
        {BITFLD, 126 },
        {XOR, 0x4A1352CF },
        {MULADD, 0x4BFB6EF3, 0x704F },
        {MULADD, 0x1B4C7FE7, 0x5637 },
        {MULADD, 0x04091A3B, 0x4917 },
        {XOR, 0x270C2F52 },
        {LOOKUP, 127 },
        {BITFLD, 128 },
        {LOOKUP, 129 },
        {BITFLD, 130 },
        {MULADD, 0x127549D5, 0x579B },
        {MULADD, 0x0AB54121, 0x7A47 },
        {BITFLD, 131 },
        {XOR, 0x751E6E49 },
        {LOOKUP, 132 },
        {LOOKUP, 133 },
        {XOR, 0x670C3F74 },
        {MULADD, 0x6B080851, 0x7E8B },
        {XOR, 0x71CD789E },
        {XOR, 0x3EB20B7B },
        {BITFLD, 134 },
        {LOOKUP, 135 },
        {MULADD, 0x58A67753, 0x272B },
        {MULADD, 0x1AB54AD7, 0x4D33 },
        {MULADD, 0x07D30A45, 0x0569 },
        {MULADD, 0x737616BF, 0x70C7 },
        {LOOKUP, 136 },
        {MULADD, 0x45C4485D, 0x2063 },
        {BITFLD, 137 },
        {XOR, 0x2598043D },
        {MULADD, 0x223A4FE3, 0x49A7 },
        {XOR, 0x1EED619F },
        {BITFLD, 138 },
        {XOR, 0x6F477561 },
        {BITFLD, 139 },
        {BITFLD, 140 },
        {LOOKUP, 141 },
        {MULADD, 0x4BC13C4F, 0x45C1 },
        {XOR, 0x3B547BFB },
        {LOOKUP, 142 },
        {MULADD, 0x71406AB3, 0x7A5F },
        {XOR, 0x2F1467E9 },
        {MULADD, 0x009366D1, 0x22D1 },
        {MULADD, 0x587D1B75, 0x2CA5 },
        {MULADD, 0x213A4BE7, 0x4499 },
        {MULADD, 0x62653E89, 0x2D5D },
        {BITFLD, 143 },
        {MULADD, 0x4F5F3257, 0x444F },
        {MULADD, 0x4C0E2B2B, 0x19D3 } },
      {
        {MULADD, 0x3F867B35, 0x7B3B }, // Table 4
        {MULADD, 0x32D25CB1, 0x3D6D },
        {BITFLD, 144 },
        {MULADD, 0x50FA1C51, 0x5F4F },
        {LOOKUP, 145 },
        {XOR, 0x05FE7AF1 },
        {MULADD, 0x14067C29, 0x10C5 },
        {LOOKUP, 146 },
        {MULADD, 0x4A5558C5, 0x271F },
        {XOR, 0x3C0861B1 },
        {BITFLD, 147 },
        {LOOKUP, 148 },
        {MULADD, 0x18837C9D, 0x6335 },
        {BITFLD, 149 },
        {XOR, 0x7DAB5033 },
        {LOOKUP, 150 },
        {MULADD, 0x03B87321, 0x7225 },
        {XOR, 0x7F906745 },
        {LOOKUP, 151 },
        {BITFLD, 152 },
        {XOR, 0x21C46C2C },
        {MULADD, 0x2B36757D, 0x028D },
        {BITFLD, 153 },
        {LOOKUP, 154 },
        {XOR, 0x106B4A85 },
        {XOR, 0x17640F11 },
        {LOOKUP, 155 },
        {XOR, 0x69E60486 },
        {LOOKUP, 156 },
        {MULADD, 0x3782017D, 0x05BF },
        {BITFLD, 157 },
        {LOOKUP, 158 },
        {XOR, 0x6BCA53B0 },
        {LOOKUP, 159 },
        {LOOKUP, 160 },
        {LOOKUP, 161 },
        {LOOKUP, 162 },
        {XOR, 0x0B8236E3 },
        {BITFLD, 163 },
        {MULADD, 0x5EE51C43, 0x4553 },
        {BITFLD, 164 },
        {LOOKUP, 165 },
        {LOOKUP, 166 },
        {LOOKUP, 167 },
        {MULADD, 0x42B14C6F, 0x5531 },
        {XOR, 0x4A2548E8 },
        {MULADD, 0x5C071D85, 0x2437 },
        {LOOKUP, 168 },
        {MULADD, 0x29195861, 0x108B },
        {XOR, 0x24012258 },
        {LOOKUP, 169 },
        {XOR, 0x63CC2377 },
        {XOR, 0x08D04B59 },
        {MULADD, 0x3FD30CF5, 0x7027 },
        {XOR, 0x7C3E0478 },
        {MULADD, 0x457776B7, 0x24B3 },
        {XOR, 0x086652BC },
        {MULADD, 0x302F5B13, 0x371D },
        {LOOKUP, 170 },
        {MULADD, 0x58692D47, 0x0671 },
        {XOR, 0x6601178E },
        {MULADD, 0x0F195B9B, 0x1369 },
        {XOR, 0x07BA21D8 },
        {BITFLD, 171 },
        {BITFLD, 172 },
        {XOR, 0x13AC3D21 },
        {MULADD, 0x5BCF3275, 0x6E1B },
        {MULADD, 0x62725C5B, 0x16B9 },
        {MULADD, 0x5B950FDF, 0x2D35 },
        {BITFLD, 173 },
        {BITFLD, 174 },
        {MULADD, 0x73BA5335, 0x1C13 },
        {BITFLD, 175 },
        {BITFLD, 176 },
        {XOR, 0x3E144154 },
        {MULADD, 0x4EED7B27, 0x38AB },
        {LOOKUP, 177 },
        {MULADD, 0x627C7E0F, 0x7F01 },
        {MULADD, 0x5D7E1F73, 0x2C0F },
        {LOOKUP, 178 },
        {MULADD, 0x55C9525F, 0x4659 },
        {XOR, 0x3765334C },
        {MULADD, 0x5DF66DDF, 0x7C25 },
        {LOOKUP, 179 },
        {LOOKUP, 180 },
        {XOR, 0x16AE5776 },
        {LOOKUP, 181 },
        {LOOKUP, 182 },
        {BITFLD, 183 },
        {BITFLD, 184 },
        {LOOKUP, 185 },
        {MULADD, 0x4392327B, 0x7E0D },
        {LOOKUP, 186 },
        {MULADD, 0x3D8B0CB5, 0x640D },
        {MULADD, 0x32865601, 0x4D43 },
        {BITFLD, 187, 0 } } };
  // Btw: total saving by removing table 1 and unused args from
  // tables 2-5... 12,707 bytes down to 9,778.
}
