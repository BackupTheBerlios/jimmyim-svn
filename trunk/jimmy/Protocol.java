/*
 * Protocol.java
 *
 * Created on Ponedeljek, 2006, april 17, 21:33
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

/**
 * Astract class to be extended with implementations of various protocols
 * @author Zoran Mesec
 * version 1.0
 */
public interface Protocol 
{
    /**
     * Method for sending a request to the server with the specified protocol.
     * @param message Request string.
     */
    public abstract void sendRequest(String message);
    /**
     * This abstract method receives the reply of the server from the previous sendRequest call.
     * @return Returns the servers reply as a stringè.
     */
    public abstract String getReply();
    /**
     * This abstract method initializes connection.
     * 
     */
    public abstract void connect();     
    /**
     * This abstract method closes the connection that is used for communication with the server.
     * 
     */
    public abstract void disconnect();           
}
