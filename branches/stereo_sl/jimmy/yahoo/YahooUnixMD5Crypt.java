package jimmy.yahoo;

import jimmy.util.MD5;
import jimmy.util.Utils;

// *********************************************************************
// Oringally converted by the folks at www.orangefood.com
//
// This class implements the popular MD5Crypt function as used by BSD and
// most modern Un*x systems. It was basically converted from the C code
// write by Poul-Henning Kamp. Here is his comment:
//
// "THE BEER-WARE LICENSE" (Revision 42):
// <phk@login.dknet.dk> wrote this file. As long as you retain this notice you
// can do whatever you want with this stuff. If we meet some day, and you think
// this stuff is worth it, you can buy me a beer in return. Poul-Henning Kamp
// *********************************************************************
public class YahooUnixMD5Crypt
{
  public static final String MAGIC = "$1$";
  
  public static String crypt(String strPassword, String strSalt)
  {
    try
    {
      String[] st = Utils.tokenize(strSalt,'$');
      byte[] abyPassword = strPassword.getBytes();
      byte[] abySalt = st[2].getBytes();
      
      MD5 _md = new MD5();
      
      _md.update(abyPassword);
      _md.update(MAGIC.getBytes());
      _md.update(abySalt);
      
      MD5 md2 = new MD5();
      md2.update(abyPassword);
      md2.update(abySalt);
      md2.update(abyPassword);
      byte[] abyFinal = md2.doFinal();
      
      for (int n = abyPassword.length; n > 0; n -= 16)
      {
        _md.update(abyFinal);
      }
      abyFinal = new byte[]
        {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
      
      for (int i = abyPassword.length; i != 0; i >>>= 1)
      {
        if ((i & 1) == 1)
          _md.update(abyFinal);
        else
          _md.update(abyPassword);
      }
      
      // Build the output string
      StringBuffer sbPasswd = new StringBuffer();
      sbPasswd.append(MAGIC);
      sbPasswd.append(new String(abySalt));
      sbPasswd.append('$');
      
      abyFinal = _md.doFinal();
      
      // And now, just to make sure things don't run too fast
      // in C . . . "On a 60 Mhz Pentium this takes 34 msec, so you would
      // need 30 seconds to build a 1000 entry dictionary..."
      for (int n = 0; n < 1000; n++)
      {
        MD5 md3 = new MD5();
        // MD5Init(&ctx1);
        if ((n & 1) != 0)
          md3.update(abyPassword);
        else
          md3.update(abyFinal);
        
        if ((n % 3) != 0)
          md3.update(abySalt);
        
        if ((n % 7) != 0)
          md3.update(abyPassword);
        
        if ((n & 1) != 0)
          md3.update(abyFinal);
        else
          md3.update(abyPassword);
        
        abyFinal = md3.doFinal();
      }
      
      return sbPasswd.append(MD5.toBase64(abyFinal)).toString();
    }
    catch (Exception e)
    {
      return null;
    }
  }
}
