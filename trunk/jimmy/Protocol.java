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
 * @author Matevz Jekovec
 * version 1.0
 */
public abstract class Protocol 
{
    /**
     * This abstract method initializes connection and logs in.
     * 
     * @param username User ID for login
     * @param passwd User password for login
     * @return true if connected and logged in successfully, otherwise false
     */
    public abstract boolean login(String username, String passwd);
    
    /**
     * Log out and close connection. 
     */
    public abstract void logout();
    
    /**
     * Return connection status.
     * 
     * @return Connection status - true if connected, false otherwise
     */
    public boolean isConnected() {return connected_;}
    
    protected boolean connected_;
}
