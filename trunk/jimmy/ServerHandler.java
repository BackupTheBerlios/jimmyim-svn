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
 File: jimmy/ServerHandler.java

 Author(s): Zoran Mesec, Matevz Jekovec
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
	 
    public ServerHandler(String url, int port) 
    {
        url_ = url;
        port_ = port;        
    }
	
    /**
     * The constructor method. If no port is used, use this constructor method.
     * @param URL URL of the server.
     */
    public ServerHandler(String url) 
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
                os_ = sc_.openDataOutputStream();
                isr_ = new DataInputStream(sc_.openDataInputStream());
            }
            else
            {
                sc_ = (SocketConnection)Connector.open("socket://" + url_ + ":" + String.valueOf(port_));
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
     * Changes URL of the server.
     */
    public void changeURL(String newURL)
    {
        this.url_ = newURL;
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
     * Send a message to the remote server using the OutputStream.
     * @param message Message to be sent using the OutputStream to the remote server using SocketConnection.
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

