/**
 * 
 */
package org.jimmy.provider;

import android.net.ContentURI;
import android.provider.BaseColumns;

/**
 * @author matej
 */
public final class JimmyIM
{
  public static final class Accounts implements BaseColumns
  {
    public static final ContentURI CONTENT_URI 
        = ContentURI.create("content://org.jimmy.provider.JimmyIM/accounts");
    
    /**
     * The default sort order for this table
     */
    public static final String DEFAULT_SORT_ORDER = _ID + " ASC";

    /**
     * Account name
     * <P>Type: TEXT</P>
     */
    public static final String NAME = "name";
    
    /**
     * Protocol
     * <P>Type: INTEGER</P>
     */
    public static final String PROTOCOL = "protocol";
    
    /**
     * Username
     * <P>Type: TEXT</P>
     */
    public static final String USER_NAME = "username";
    
    /**
     * Password
     * <P>Type: TEXT</P>
     */
    public static final String PASSWORD = "password";
    
    /**
     * Server
     * <P>Type: TEXT</P>
     */
    public static final String SERVER = "server";
    
    /**
     * Port
     * <P>Type: INTEGER</P>
     */
    public static final String PORT = "port";
    
    /**
     * Auto login
     * <P>Type: BOOLEAN</P>
     */
    public static final String AUTO_LOGIN = "autologin";
    
    /**
     * Use SSL
     * <P>Type: BOOLEAN</P>
     */
    public static final String SSL = "ssl";

    /**
     * Is connected
     * <P>Type: BOOLEAN</P>
     */
    public static final String CONNECTED = "connected";
  }
}
