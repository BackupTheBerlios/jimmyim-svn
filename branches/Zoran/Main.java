/*
 * MSN.java
 *
 * Created on Torek, 2006, april 4, 17:59
 */

import javax.microedition.io.*;
import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;

/**
 *
 * @author  Zoran Mesec
 * @version
 */
public class Main extends MIDlet {
    public void startApp() 
    {
        String url="socket://messenger.hotmail.com:1863";
        String message = "VER 0 MSNP7 MSNP6 MSNP5 MSNP5 CVR0";

            try
            {
                SocketConnection ssc = (SocketConnection)Connector.open(url);
                InputStream is = null;    
                
                OutputStream os = ssc.openOutputStream();
                byte[] data = message.getBytes();
                os.write(data);
                System.out.println("connection opened!");
                is = ssc.openInputStream();
                int ch = 0;
                StringBuffer sb = new StringBuffer();
                while ((ch = is.read()) != -1)
                {
                        sb.append((char)ch);
                        System.out.println("a: "+sb.toString());
                }
                System.out.println("odgovor: "+sb.toString());
                os.close();
                is.close();
                ssc.close();
            }
            catch (Exception e )
            {
                System.out.println("izjema");
                e.printStackTrace();
            }            
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
