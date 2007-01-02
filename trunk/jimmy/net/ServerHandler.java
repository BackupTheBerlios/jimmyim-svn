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

package jimmy.net;

import java.io.*;
import java.lang.Thread;
import javax.microedition.io.*;

import jimmy.util.Utils;

/**
 * This class is used to connect to a remote server using SocketConnection class.
 * This is the way how all the communication between the mobile phone and IM servers should be done.
 * The class always creates read/write type of the connection automatically.
 * 
 * Usage:
 * 1) Class should be created passing the server URL without the "protocol://" prefix and a port which to connect to.
 *    ServerHandler sh_ = new ServerHandler("messenger.hotmail.com","1863");
 * 2) sh_.setTimeout(10000); //set timeout to 10 seconds - we have a really bad connection :)
 * 2) sh_.connect();
 * 3) sendRequest("VER 1 MSNP8 CVR0\r\n")
 * 4) String myReply = getReply();
 * 
 * @author Zoran Mesec
 * @author Matevz Jekovec
 */
public class ServerHandler
{
    private String url_;	//server URL without the leading "protocol://"
    private int outPort_;	//output port we connect to
    private int inPort_;	//input port the connection is made to
    private SocketConnection sc_;	//main connection
    private DataOutputStream os_;	//output stream linked with sc_
    private DataInputStream is_;	//input stream linked with sc_
    private final int SLEEPTIME_ = 50;	//sleep time per iteration in miliseconds
    private int timeout_ = 5000;	//timeout of getting the reply
    private boolean connected_ = false;	//connection status
	
    /**
     * The constructor method.
     * 
     * @param url URL of the server without the leading "protocol://" - No protocol type specified here! eg. messenger.hotmail.com
     * @param port Server port which to connect to. eg. 1863 for MSN.
     */
    public ServerHandler(String url, int port) 
    {
        url_ = url;
        outPort_ = port;        
        connected_ = false;
    }
	
    /**
     * The constructor method. If no port is used (ie. use the default port - 80 http), use this constructor method.
     * 
     * @param URL URL of the server.
     */
    public ServerHandler(String url) 
    {
        url_ = url;
        outPort_ = 0;        
    }
	
    /**
     * Set the timeout before stop checking the read buffer.
     * 
     * @param timeout Timeout value in miliseconds
     */
    public void setTimeout(int timeout) {
    	this.timeout_ = timeout;
    }
    
    /**
     * Open the connection to the specified URL and port passed in the constructor using the SocketConnection class.
     * 
     * @param useSSL Connect using Secure Socket Layer connection
     */
    public void connect(boolean useSSL)
    {
        try
        {
            this.sc_ = (SocketConnection)Connector.open( (useSSL?"ssl://":"socket://") + url_ +
            		((outPort_ == 0) ? "" : (":" + String.valueOf(outPort_))) );
//        	this.sc_ = (SocketConnection)Connector.open("socket://" + this.url_ + ":" + String.valueOf(this.outPort_));
            this.inPort_ = this.sc_.getLocalPort();
            this.os_ = this.sc_.openDataOutputStream();
            this.is_ = this.sc_.openDataInputStream();
            this.connected_ = true;
       }catch(SecurityException e){
    	   		//in case NO is pressed on overair question
       }catch (IOException e){
    	   	e.printStackTrace();
    	   	this.connected_ = false;
    	   }
    }
    /**
     * This method is provided for convenience.
     * It triggers connect(false).
     */
    public void connect() {
    	connect(false);
    }
    
    /**
     * Disconnect the SocketConnection and IO streams.
     */
    public void disconnect()
    {
        try 
        {
            is_.close();
            os_.flush();	//flush the output stream before closing it!
            os_.close();
            sc_.close();
            connected_ = false;
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
	
    /**
     * Send a message to the remote server using the OutputStream.
     * 
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
     * Send a message to the remote server using the OutputStream.
     * 
     * @param message Message to be sent using the OutputStream to the remote server using SocketConnection.
     */
    public void sendRequest(byte[] message){
    	
//    	System.out.println("[DEBUG] OUT:\n" + Utils.byteArrayToHexString(message));
    	
    	try 
        {            
    			os_.write(message);
    			os_.flush();
         
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    /**
     * Read a message from the remote server reading the waiting buffer. If waiting buffer is empty, wait until it gets filled or if timeout occurs.
     * This method reads and empties the WHOLE buffer (ie. doesn't stop at new line)! 
     *  
     * @return Message from the remote server as a String in UTF-8 encoding, null if timeout has occured.
     */
    public String getReply()
    {          
        try 
        {
            byte[] buffer = null;
            
            int bs = this.is_.available(); //get the amount of characters waiting in the buffer
            if (bs!=0) {
                buffer = new byte[bs];	//create an array of characters of size the ones in the buffer
                this.is_.read(buffer);	//read the whole buffer in the array
            } else {	//if the buffer is empty, wait and recheck every SLEEPTIME_ miliseconds
                for (int time=0; time < this.timeout_; time += this.SLEEPTIME_) {
                    Thread.sleep(this.SLEEPTIME_);
                    if ((bs = this.is_.available()) != 0) {
                        buffer = new byte[bs];
                        this.is_.read(buffer);
                        break;
                    }
                }
        	}
            
            if (buffer != null)
            	return new String(buffer, "UTF-8"); //buffer was get, return the String
            else
            	return null;	//buffer was not get - timeout occured, return null
        }catch (InterruptedIOException ex){
//        		ex.printStackTrace();

            return null;	
        }catch (IOException ex){
            ex.printStackTrace();

            return null;
        }catch (InterruptedException ex){
            ex.printStackTrace();

            return null;
        }
    }
    
    /**
     * Read a message from the remote server reading the waiting buffer. If waiting buffer is empty, wait until it gets filled or if timeout occurs.
     * This method reads and empties the WHOLE buffer (ie. doesn't stop at new line)!
     *  
     * @param enc Return String in the given encoding. See http://java.sun.com/j2se/1.5.0/docs/api/java/nio/charset/Charset.html for additional information on String encodings.
     * @return Message from the remote server as a String in UTF-8 encoding, null if timeout has occured.
     */
    public String getReply(String enc)
    {          
        try 
        {
            byte[] buffer = null;
            
            int bs = this.is_.available(); //get the amount of characters waiting in the buffer
            if (bs!=0) {
                buffer = new byte[bs];	//create an array of characters of size the ones in the buffer
                this.is_.read(buffer);	//read the whole buffer in the array
            } else {	//if the buffer is empty, wait and recheck every SLEEPTIME_ miliseconds
                for (int time=0; time < this.timeout_; time += this.SLEEPTIME_) {
                    Thread.sleep(this.SLEEPTIME_);
                    if ((bs = this.is_.available()) != 0) {
                        buffer = new byte[bs];
                        this.is_.read(buffer);
                        break;
                    }
                }
        	}
            
            if (buffer != null)
            	return new String(buffer, enc); //buffer was get, return the String
            else
            	return null;	//buffer was not get - timeout occured, return null
        }catch (InterruptedIOException ex){
//        		ex.printStackTrace();

            return null;	
        }catch (IOException ex){
            ex.printStackTrace();

            return null;
        }catch (InterruptedException ex){
            ex.printStackTrace();

            return null;
        }
    }    
    /**
     * Read a message from the remote server reading the waiting buffer. If waiting buffer is empty, wait until it gets filled or if timeout occurs.
     * This method reads and empties the WHOLE buffer (ie. doesn't stop at new line)! 
     *  
     * @return Message from the remote server as a byte[]. null if timeout has occured
     */
    public byte[] getReplyBytes()
    {          
        try 
        {
            byte[] buffer = null;
            
            int bs = this.is_.available(); //get the amount of characters waiting in the buffer
            if (bs!=0) {
                buffer = new byte[bs];	//create an array of characters of size the ones in the buffer
                this.is_.read(buffer);	//read the whole buffer in the array
            } else {	//if the buffer is empty, wait and recheck every SLEEPTIME_ miliseconds
                for (int time=0; time < this.timeout_; time += this.SLEEPTIME_) {
                    Thread.sleep(this.SLEEPTIME_);
                    if ((bs = this.is_.available()) != 0) {
                        buffer = new byte[bs];
                        this.is_.read(buffer);
                        break;
                    }
                }
        	}
            
            if (buffer != null)
            	return buffer; //buffer was get, return the String
            else
            	return null;	//buffer was not get - timeout occured, return null
        }catch (InterruptedIOException ex){
//        		ex.printStackTrace();

            return null;	
        }catch (IOException ex){
            ex.printStackTrace();

            return null;
        }catch (InterruptedException ex){
            ex.printStackTrace();

            return null;
        }
    }
    
    /**
     * Returns the created input (local) port when an outgoing connection was established.
     * 
     * @return Incoming port
     */
    public int getInputPort() {return inPort_;}
    
    /**
     * Returns the connection outgoing port passed in the constructor.
     *  
     * @return Outgoing port
     */
    public int getOutputPort() {return outPort_;}
    
    /**
     * Returns true if the connection is established, false otherwise.
     */
    public boolean isConnected() {return connected_;}
    
    /**
     * Sets a new url to connect to
     * @param u the url String
     */
    public void setURL(String u) {this.url_ = u;}
    
    /**
     * Sets tne new port to connect to
     * @param p the port int number
     */
    public void setPort(int p) {this.outPort_ = p;}
}

