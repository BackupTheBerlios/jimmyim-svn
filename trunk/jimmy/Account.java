/*
 * Account.java
 *
 * Created on May 20, 2006, 11:47 AM
 */

/**
 *
 * @author slashrsm
 */
public class Account {
    private String username;
    private String password;
    private String server;
    
    /** Creates a new instance of Account */
    public Account(String u, String p, String s) {
        this.username   = u;
        this.password   = p;
        this.server     = s; 
    }
    
    public void setUser(String u){
        this.username = u;
    }
    
    public void setPass(String p){
        this.password = p;
    }
    
    public void setServer(String s){
        this.server = s;
    }
    
    public String getUser(){
        return this.username;
    }
    
    public String getPass(){
       return this.password;
    }
    
    public String getServer(){
        return this.server;
    }
}
