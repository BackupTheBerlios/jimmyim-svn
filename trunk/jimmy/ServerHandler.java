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
import java.lang.Thread;
import javax.microedition.io.*;
/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class ServerHandler extends Thread
{
    private String url_;
    private int port_;
    private SocketConnection sc_;
    private DataOutputStream os_;
    private DataInputStream isr_;
    final long sleepTime_ = 250;
    final int sleepCount_ = 4;
	
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
     * Accept a message from the remote server.
     * @return Message from the remote server as a String.
     */
    public String getReply()
    {          
        try 
        {
            byte[] buffer;
            
            int bs = this.isr_.available(); // stands for buffer size
            if(bs!=0)
            {
                buffer = new byte[bs];
                this.isr_.read(buffer);
                return new String(buffer);
            }
            else
            {
                Thread t = new Thread(this);
                for(int i=0; i<this.sleepCount_; i++)
                {
                    t.sleep(this.sleepTime_);
                    bs = this.isr_.available();
                    if(bs!=0)
                    {
                        buffer = new byte[bs];
                        this.isr_.read(buffer);
                        return new String(buffer);                    
                    }
                }
                return null;
            }      
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

