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
 Author(s): Dejan Sakelsak
 */

package jimmy;

/**
 *
 * @author slashrsm
 */
public class Account {
    private String username_;
    private String password_;
    private String server_;
    
    /** Creates a new instance of Account */
    public Account(String u, String p, String s) {
        this.username_   = u;
        this.password_   = p;
        this.server_     = s; 
    }
    
    public void setUser(String u){
        this.username_ = u;
    }
    
    public void setPass(String p){
        this.password_ = p;
    }
    
    public void setServer(String s){
        this.server_ = s;
    }
    
    public String getUser(){
        return this.username_;
    }
    
    public String getPass(){
       return this.password_;
    }
    
    public String getServer(){
        return this.server_;
    }
}
