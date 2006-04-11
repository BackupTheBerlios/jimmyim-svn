/*
 * Povezava.java
 *
 * Created on Torek, 2006, april 11, 23:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

import java.io.*;
import javax.microedition.io.*;

/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class Povezava 
{
    private String url;
    private String port;
    private SocketConnection sc;
    private OutputStream os;
    private InputStreamReader isr;
    /**
     * The constructor method.
     * @param URL URL of the server. No protocolis specified here! Example: messenger.hotmail.com.
     * @param PORT PORT of the server(inputs as a String). Example: "1863".
     */
    public Povezava(String URL, String PORT) 
    {
        this.url = URL;
        this.port = PORT;        
    }
    /**
     * Initializes SocketConnection.
     */
    public void povezi()
    {
        try
        {
            if(this.port==null)
            {
                this.sc = (SocketConnection)Connector.open("socket://" + this.url);
            }
            else
            {
                this.sc = (SocketConnection)Connector.open("socket://" + this.url + ":" + this.port);
            }        
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
    /**
     * method disconnects the SocketConnection of this class.
     */
    public void prekiniPovezavo()
    {
        try 
        {
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
    public void poslji(String message)
    {
        try 
        {
            this.os = this.sc.openOutputStream();
            byte[] data = message.getBytes();
            os.write(data);
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    /**
     * Disconnects OutputStream.
     */
    public void prekiniOutput()
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
     * Accepts a message from the remote server.
     * @return Returns the message from the remote server as a String.
     */
    public String sprejmi()
    {
        try 
        {
            this.isr = new InputStreamReader(this.sc.openInputStream());
            int ch; 
            StringBuffer sb = new StringBuffer();
            ch = this.isr.read();                  
            while (ch != -1)
            {  
                    sb.append((char)ch);
                    //System.out.println("a: "+sb.toString());
                    ch = this.isr.read();
            }           
            return sb.toString();
      
            
        } catch (IOException ex) 
        {
            ex.printStackTrace();
            return null;
        }
    }
    /**
     * Disonnects InputStream.
     */
    public void prekiniInput()
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
