/*
 * ServerHandler.java
 *
 * Created on Torek, 2006, april 11, 23:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import java.io.*;
import javax.microedition.io.*;
/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class ServerHandler 
{
    private String url;
    private int port;
    private SocketConnection sc;
    private DataOutputStream os;
    private DataInputStream isr;
    /**
     * The constructor method.
     * @param URL URL of the server. No protocolis specified here! Example: messenger.hotmail.com.
     * @param PORT PORT of the server(inputs as a String). Example: "1863".
     */
    public ServerHandler(String URL, int PORT) 
    {
        this.url = URL;
        this.port = PORT;        
    }
    /**
     * The constructor method. If no port is used, use this constructor method.
     * @param URL URL of the server.
     */
    public ServerHandler(String URL) 
    {
        this.url = URL;
        this.port = 0;        
    }    
    /**
     * Initializes SocketConnection.
     */
    public void connect()
    {
        try
        {
            if(this.port==0)
            {
                this.sc = (SocketConnection)Connector.open("socket://" + this.url);
                this.os = this.sc.openDataOutputStream();
                this.isr = new DataInputStream(this.sc.openDataInputStream());
            }
            else
            {
                this.sc = (SocketConnection)Connector.open("socket://" + this.url + ":" + String.valueOf(this.port));
                this.os = this.sc.openDataOutputStream();
                this.isr = new DataInputStream(this.sc.openDataInputStream());
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
     * Returns next character from the input stream.
     * @return int representation of a character.
     */
    public int getNextCharacter() throws IOException 
    {
        return isr.read();  
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
            boolean eol=false;
            //this.isr = new InputStreamReader(this.sc.openInputStream());
            while (true) 
            {
                int c = getNextCharacter();
                if (c<0) 
                { 
                    eol=true;
                    if (sb.length()==0) return null;
                    break;
                }
                if (c==0x0d || c==0x0a) 
                {
                    eol=true;
                    if (c==0x0a) 
                    {
                        break;
                    }
                }
                else 
                {
                    if (eol) 
                    {
                        //afterEol=c;
                        //inputstream.reset();
                        break;
                    }
                    sb.append((char) c);
                }
            } 
            return sb.toString();
            
        } 
        catch (IOException ex) 
        {
            ex.printStackTrace();

            return null;
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
