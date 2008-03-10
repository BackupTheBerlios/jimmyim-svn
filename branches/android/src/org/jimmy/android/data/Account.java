/**
 * 
 */
package org.jimmy.android.data;

/**
 * This class stores private information about the user login settings.
 *
 * @author slashrsm
 * @author matevz
 */
public class Account
{
  public static final String NEWLINE_ = "\n";

  private byte protocolType_; //used protocol - see Protocol.PR_*
  private String name;
    private String username_; //login username
    private String password_; //login password
    private String server_;   //login server name (optional)
    private int port_;      //server port (optional)
    private boolean useSSL_;  //use a secure socket layer connection
    private boolean autoLogin_; //automatically login at startup
    private boolean connected_;
    private Protocol protocol_;
    
    /**
     * Creates a new instance of the user account.
     * 
     * @param u Login username
     * @param p Login password
     * @param s Server name which to connect to
     * @param port Server port which to connect to
     */
    public Account(String n, String u, String p, byte protocolType, String s, int port, boolean useSSL, boolean autoLogin) {
      this.name = n;
        this.protocolType_ = protocolType;
        this.username_   = u;
        this.password_   = p;
        this.server_     = s;
        this.port_       = port;
        this.useSSL_   = useSSL;
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
    public Account(String n, String u, String p, byte protocolType, boolean useSSL) {
      this(n, u, p, protocolType, null, 0, false, useSSL);
    }
    
    public Account(String n, String u, String p, byte protocolType) {
      this(n, u, p, protocolType, null, 0, false, false);
    }

    public String toString() {
      String out = "Protocol type (0 Jabber, 1 ICQ, 2 MSN, 3 Yahoo!): ";
      out.concat(String.valueOf(protocolType_) + "\n");
      out.concat("User name: " + username_);
      out.concat("Password: " + password_);
      out.concat("Server name (optional): " + server_);
      out.concat("Server port (optional): " + String.valueOf(port_));
      out.concat("AutoLogin: " + autoLogin_);
      out.concat(useSSL_?"This account uses SSL connection!":"This account doesn't use SSL connection.");
      
      return out;
    }
    
    public void setName(String n)       {this.name = n;}
    public void setUser(String u)       {this.username_ = u;}
    public void setPassword(String p)   {this.password_ = p;}
    public void setServer(String s)     {this.server_ = s;}
    public void setPort(int port)       {this.port_ = port;}
    public void setAutoLogin(boolean al) {this.autoLogin_ = al;}
    public void setConnected(boolean p) {this.connected_ = p;}
    public void setProtocol(Protocol p) {this.protocol_ = p;}
    public void setUseSSL(boolean ssl)  {this.useSSL_ = ssl;}
   
    public String getName()         {return this.name;}
    public String getUser()         {return this.username_;}
    public String getPassword()     {return this.password_;}   
    public String getServer()       {return this.server_;}    
    public int getPort()            {return this.port_;}   
    public byte getProtocolType() {return this.protocolType_;}
    public boolean getAutoLogin() {return this.autoLogin_;}
    public boolean getUseSSL()      {return this.useSSL_;}
    public boolean isConnected()    {return this.connected_;}
    public Protocol getProtocol()   {return this.protocol_;}
}
