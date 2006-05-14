/*
 * MSNServerHandler.java
 *
 * Created on Sobota, 2006, maj 6, 14:42
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import java.io.*;
import javax.microedition.io.*;
/**
 *
 * @author Zoran Mesec
 */
public class MSNServerHandler
{
    private String url_;
    private int port_;
    private SocketConnection sc_;
    private DataOutputStream os_;
    private DataInputStream isr_;
	
    /**
     * The constructor method.
     * @param url URL of the server. No protocolis specified here! Example: messenger.hotmail.com.
     * @param port Server's port which to connect to. eg. 1863 for MSN.
     */
	 
    public MSNServerHandler(String url, int port) 
    {
        url_ = url;
        port_ = port;        
    }
	
    /**
     * The constructor method. If no port is used, use this constructor method.
     * @param URL URL of the server.
     */
    public MSNServerHandler(String url) 
    {
        url_ = url;
        port_ = 0;        
    }
	
    /**
     * Initialize SocketConnection.
     */
    public void connect()
    {
        try
        {
            if(port_ == 0)
            {
                sc_ = (SocketConnection)Connector.open("socket://" + url_);
                System.out.println("local port:"+sc_.getLocalPort());
                
                os_ = sc_.openDataOutputStream();
                isr_ = new DataInputStream(sc_.openDataInputStream());
            }
            else
            {
                sc_ = (SocketConnection)Connector.open("socket://" + url_ + ":" + String.valueOf(port_));
                System.out.println("local port:"+sc_.getLocalPort());
                os_ = sc_.openDataOutputStream();
                isr_ = new DataInputStream(sc_.openDataInputStream());
            }        
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
	
    /**
     * Disconnect the SocketConnection of this class.
     */
    public void disconnect()
    {
        try 
        {
            isr_.close();
            os_.close();
            sc_.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
	
    /**
<<<<<<< .mine
     * Changes URL of the server.
     */
    public void changeURL(String newURL)
    {
        this.url_ = newURL;
    }    
    /**
     * Sends a message to the remote server with the OutputStream.
     * @param message Message to be sent with OutputStream to the remote server using SocketConnection.
=======
     * Send a message to the remote server using the OutputStream.
     * @param message Message to be sent using the OutputStream to the remote server using SocketConnection.
>>>>>>> .r24
     */
    public void sendRequest(String message)
    {
        try 
        {
            //os_ = sc_.openOutputStream();
            byte[] data = message.getBytes();
            
            os_.write(data);
            os_.flush();
         
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
	
    /**
     * Close OutputStream.
     */
    public void closeOutput()
    {
        try 
        {
            os_.flush();
            os_.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
	
    /**
     * Open OutputStream.
     */
    public void openOutput()
    {
            //this.osw = new OutputStreamWriter(this.os);

    }
	
    /**
     * Return the next character from the input stream.
     * @return int Representation of a character.
     */
    public int getNextCharacter() throws IOException 
    {
        return isr_.read();  
    }
    
    
    /**
     * Accept a message from the remote server.
     * @return Message from the remote server as a String.
     */
    public String getReply()
    {           
        try 
        {
            byte[] b = new byte[2048];
            this.isr_.read(b);
            StringBuffer sb = new StringBuffer();
            int it = 0;
            while(b[it]!= 0)
            {
                    sb.append((char)b[it]);
                    it++;
            }
            String reply = sb.toString();
            return reply.substring(0,reply.length()-2);
        } 
        catch (Exception ex) 
        {
            ex.printStackTrace();
            return null;
        }
    }
	
    /**
     * Close InputStream.
     */
    public void closeInput()
    {
        try 
        {
            isr_.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
}
