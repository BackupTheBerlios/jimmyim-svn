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
    private String username_;	//login username
    private String password_;	//login password
    private int protocolType_;	//used protocol - see Protocol.PR_*
    private String server_;		//login server name (optional)
    private int port_;	//server port (optional)
    
    /**
     * Creates a new instance of the user account.
     * 
     * @param u Login username
     * @param p Login password
     * @param s Server name which to connect to
     * @param port Server port which to connect to
     */
    public Account(String u, String p, int protocolType, String s, int port) {
        this.username_   = u;
        this.password_   = p;
        this.protocolType_ = protocolType;
        this.server_     = s;
        this.port_       = port;
    }
    
    /**
     * Creates a new instance of the user account with the default port for the protocol.
     * This method is provided for the convenience.
     * 
     * @param u Login username
     * @param p Login password
     * @param s Server name which to connect to
     */
    public Account(String u, String p, int protocolType) {
    	this(u, p, protocolType, null, 0);
    }
    
    public void setUser(String u){
        this.username_ = u;
    }
    
    public void setPassword(String p){
        this.password_ = p;
    }
    
    public void setServer(String s){
        this.server_ = s;
    }
    
    public void setPort(int port){
    	this.port_ = port;
    }
    
    public String getUser(){
        return this.username_;
    }
    
    public String getPassword(){
       return this.password_;
    }
    
    public String getServer(){
        return this.server_;
    }
    
    public int getPort() {
    	return this.port_;
    }
}
