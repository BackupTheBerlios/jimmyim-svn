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
 File: jimmy/Account.java
 Version: pre-alpha  Date: 2006/05/21
 Author(s): Dejan Sakelsak, Matevz Jekovec
 */

package jimmy;

/**
 * This class stores private information about the user login settings.
 *
 * @author slashrsm
 * @author matevz
 */
public class Account {
    private byte protocolType_;	//used protocol - see Protocol.PR_*
    private String username_;	//login username
    private String password_;	//login password
    private String server_;		//login server name (optional)
    private int port_ = 0;	//server port (optional)
    private boolean autoLogin_;	//automatically login at startup
    private boolean connected_;
    
    /**
     * Creates a new instance of the user account.
     * 
     * @param u Login username
     * @param p Login password
     * @param s Server name which to connect to
     * @param port Server port which to connect to
     */
    public Account(String u, String p, byte protocolType, String s, int port, boolean autoLogin) {
        this.protocolType_ = protocolType;
        this.username_   = u;
        this.password_   = p;
        this.server_     = s;
        this.port_       = port;
        this.autoLogin_  = autoLogin;
        this.connected_   = false;
    }
    
    /**
     * Creates a new instance of the user account with the default port for the protocol.
     * This method is provided for the convenience.
     * 
     * @param u Login username
     * @param p Login password
     * @param s Server name which to connect to
     */
    public Account(String u, String p, byte protocolType) {
    	this(u, p, protocolType, null, 0, false);
    }
    
    public String toString() {
    	String out = "Protocol type (0 Jabber, 1 ICQ, 2 MSN, 3 Yahoo!): ";
    	out.concat(String.valueOf(protocolType_) + "\n");
    	out.concat("User name: " + username_);
    	out.concat("Password: " + password_);
    	out.concat("Server name (optional): " + server_);
    	out.concat("Server port (optional): " + String.valueOf(port_));
    	out.concat("AutoLogin: " + autoLogin_);
    	
    	return out;
    }
    
    public void setUser(String u)       {this.username_ = u;}
    public void setPassword(String p)   {this.password_ = p;}
    public void setServer(String s)     {this.server_ = s;}
    public void setPort(int port)       {this.port_ = port;}
    public void setAutoLogin(boolean al) {this.autoLogin_ = al;}
    public void setConnected(boolean p) {this.connected_ = p;}
   
    public String getUser()         {return this.username_;}
    public String getPassword()     {return this.password_;}   
    public String getServer()       {return this.server_;}    
    public int getPort()            {return this.port_;}   
    public byte getProtocolType()	{return this.protocolType_;}
    public boolean getAutoLogin()	{return this.autoLogin_;}
    public boolean isConnected()   {return this.connected_;}
}
