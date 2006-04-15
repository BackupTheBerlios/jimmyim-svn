/*
 * MSN.java
 *
 * Created on Torek, 2006, april 4, 17:59
 */
package hello;

import javax.microedition.io.*;
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
import java.io.*;
import java.util.*;

/**
 *
 * @author  Zoran Mesec
 * @version
 */
public class Main extends MIDlet
{
    public void startApp() 
    {
        ServerCommunication sc = new ServerCommunication("messenger.hotmail.com",1863);
        sc.connect();
        String message = "VER 0 MSNP8 CVR0\r\n";
        sc.sendMsg(message);
        System.out.print(message);
        System.out.println(sc.getReply());
        System.out.println("*************************************");
        message = "CVR 2 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS avgustin.ocepek@yahoo.com.au\r\n";
       //sc.disconnect();
        //sc.connect();
        sc.sendMsg(message);
        
        System.out.println(message);
        System.out.println(sc.getReply());
        System.out.println("*************************************");        
        message="USR 3 TWN I avgustin.ocepek@yahoo.com.au\r\n";
        sc.sendMsg(message);
        System.out.println(message);
        System.out.println(sc.getReply());
        System.out.println("*************************************");        
        sc.disconnect();
        
        
        ServerCommunication sc2 = new ServerCommunication("baym-cs118.msgr.hotmail.com",1863);
        sc2.connect();
        message="VER 4 MSNP8 CVR0\r\n";
        sc2.sendMsg(message);
        System.out.println(message);
        System.out.println(sc2.getReply());
        System.out.println("*************************************");   
        
        
        message = "CVR 5 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS avgustin.ocepek@yahoo.com.au\r\n";
        sc2.sendMsg(message);
        System.out.println(message);
        System.out.println(sc2.getReply());      
        System.out.println("*************************************"); 
        message = "USR 6 TWN I avgustin.ocepek@yahoo.com.au\r\n";
        System.out.println(message);
        sc2.sendMsg(message);
        System.out.println(sc2.getReply());  
        sc2.disconnect();
        
        
        /*String url="socket://messenger.hotmail.com:1863";
        String message = "VER 0 MSNP7 MSNP6 MSNP5 MSNP5 CVR0\n";

            try
            {
                SocketConnection ssc = (SocketConnection)Connector.open(url);
                InputStream is = null;    
                ByteArrayInputStream bais = null;
                
                OutputStream os = ssc.openOutputStream();
                InputStreamReader isr=new InputStreamReader(ssc.openInputStream());
                byte[] data = message.getBytes();
                os.write(data);
                os.flush();
                System.out.println("connection opened!");
                //is = ssc.openInputStream();
                StringBuffer sb = new StringBuffer();
                System.out.println("prebrano");                   
                
                
                                
                int ch; 
                ch = isr.read();                  
                while (ch != '\n')
                {  
                        sb.append((char)ch);
                        System.out.println("a: "+sb.toString());
                        ch = isr.read();
                }
                System.out.println("odgovor: "+sb.toString());
                //isr.close();
                sb.delete(0,sb.length());
                message = "INF 1\n";
                data = message.getBytes();
                os.write(data);
                os.flush();
                ch = isr.read();                  
                while (ch != -1)
                {  
                        sb.append((char)ch);
                        System.out.println("b: "+sb.toString());
                        ch = isr.read();
                }
                System.out.println("odgovor2: "+sb.toString());
                isr.close();
                //is.close();
                ssc.close();
                System.out.println("knèano");
            }
            catch (Exception e )
            {
                System.out.println("izjema");
                e.printStackTrace();
            }*/
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
}
