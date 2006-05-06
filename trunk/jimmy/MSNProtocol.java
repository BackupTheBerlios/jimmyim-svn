/*
 * MSNProtocol.java
 *
 * Created on Torek, 2006, april 11, 23:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import hello.MSNTransaction;
import java.io.*;
import javax.microedition.io.*;
/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class MSNProtocol extends Protocol 
{
    private String username;
    private String password;
    private int port;
    final String DSurl = "messenger.hotmail.com";
    final int serverPort = 1863;
    private MSNTransaction tr;
    private SocketConnection sc;
    private OutputStream os;
    private InputStreamReader isr;
    /**
     * The constructor method.
     * @param URL URL of the server. No protocolis specified here! Example: messenger.hotmail.com.
     * @param PORT PORT of the server(inputs as a String). Example: "1863".
     */
    public MSNProtocol(String username, String password)
    {
        this.username = username;
        this.password= password;        
    }
    /**
     * Initializes SocketConnection.
     */
    public void connect()
    {
        try
        {
                this.sc = (SocketConnection)Connector.open("socket://" + this.DSurl + ":" + Integer.toString(this.serverPort));
                this.os = this.sc.openOutputStream();
                this.isr = new InputStreamReader(this.sc.openInputStream());
                
                String message = "VER 0 MSNP8 CVR0\r\n";
                this.sendRequest(message);
                System.out.print(message);
                System.out.println(this.getReply());
                System.out.println("*************************************");
                message = "CVR 2 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS avgustin.ocepek@yahoo.com.au\r\n";
               //sc.disconnect();
                //sc.connect();
                this.sendRequest(message);

                System.out.println(message);
                System.out.println(this.getReply());
                System.out.println("*************************************");        
                message="USR 3 TWN I avgustin.ocepek@yahoo.com.au\r\n";
                this.sendRequest(message);
                System.out.println(message);
                System.out.println(this.getReply());
                System.out.println("*************************************");                  
                
      
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * method disconnects the SocketConnection of this class.
     */
    public void disconnect()
    {
        try 
        {
            this.isr.close();
            this.os.close();
            this.sc.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    /**
     * Sends a message to the remote server with the OutputStream.
     * @param message Message to be sent with OutputStream to the remote server using SocketConnection.
     */
    public void sendRequest(String message)
    {
        try 
        {
            //this.os = this.sc.openOutputStream();
            byte[] data = message.getBytes();
            
            this.os.write(data);
            this.os.flush();
         
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    /**
     * Disconnects OutputStream.
     */
    public void closeOutput()
    {
        try 
        {
            this.os.flush();
            this.os.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    /**
     * Disconnects OutputStream.
     */
    public void openOutput()
    {
            //this.osw = new OutputStreamWriter(this.os);

    }    
    /**
     * Accepts a message from the remote server.
     * @return Returns the message from the remote server as a String.
     */
    public String getReply()
    {
            StringBuffer sb = new StringBuffer();             
        try 
        {
            //this.isr = new InputStreamReader(this.sc.openInputStream());
            int ch; 
           

            while ((ch = this.isr.read())!='\n')
            {  
                    sb.append((char)ch);
                    //System.out.println("a: "+ch);
            }   
            return sb.toString();
      
            
        } catch (IOException ex) 
        {
                        //System.out.println(sb.toString());
            //ex.printStackTrace();

            return sb.toString();
        }
    }
    /**
     * Disonnects InputStream.
     */
    public void closeInput()
    {
        try 
        {
            this.isr.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
}
