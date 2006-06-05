/*
 * PassportNexus.java
 *
 * Created on Èetrtek, 2006, april 20, 15:51
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy.msn;
import java.io.*;
import javax.microedition.io.*;

/**
 *
 * @author Zoran Mesec
 */
public class PassportNexus 
{
    final String               DALOGIN                      = "DALogin=";
    final String               DASTATUS                     = "da-status=";
    final String               TICKET                       = "from-PP=";
    final String               SUCCESS                      = "success";
    final String               KEY_PASSPORT_URLS            = "PassportURLs";
    final String               KEY_LOCATION                 = "Location";
    final String               KEY_AUTHENTICATION_INFO      = "Authentication-Info";
    static final String        PASSPORT_LIST_SERVER_ADDRESS = "https://nexus.passport.com/rdr/pprdr.asp";
    private HttpsConnection httpsConn;
    private String loginServerURL;
    
    /** Creates a new instance of PassportNexus */
    public PassportNexus() 
    {
        try {
            httpsConn = (HttpsConnection)Connector.open(PASSPORT_LIST_SERVER_ADDRESS);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    /**
    * Returns the field value of returned 
    * server data.
    *
    * @param strKey The field name to retrieve
    * @oaram strField The complete field to search the key value
    */
    String getField( String strKey, String strField )  {

    try  {
      int       nIniPos        = strField.indexOf( strKey );
      int       nEndPos        = 0;


      if( nIniPos < 0 )
        return "";

      nIniPos+=strKey.length();
      nEndPos = strField.indexOf( ',', nIniPos );

      if( nEndPos < 0 )
        return "";

      return strField.substring( nIniPos, nEndPos );
    } catch( Exception e )  {
      return "";
    }
    }   
    
    /**
    * Return the Passport Login server
    */
    void getPassportLoginServer()  
    {
        if( httpsConn != null )  
        {
            String strPassportServer=null;
            String strPassportURL;
            try 
            {
                strPassportURL = httpsConn.getHeaderField(KEY_PASSPORT_URLS);
                strPassportServer = "https://" + getField( DALOGIN, strPassportURL );
                httpsConn = null;    
                this.loginServerURL = strPassportServer;
            } catch (IOException ex) 
            {
                ex.printStackTrace();
            }   
            //return strPassportServer;
        }  
    }
  /**
   * Request authorization ticket to login in passport
   * server.
   *
   * @param strLoginURL The login URL
   * @param strUserName The login user name
   * @param strPassword The user password
   * @strChallenge The challenge string sent by passport server
   */
    String requestAuthorizationTicket(String strUserName, String strPassword, String strChallenge )  
    {

        if( ( strUserName  != null ) && 
            ( strPassword  != null ) &&
            ( strChallenge != null ) )  {

            int t = strUserName.indexOf('@');
            StringBuffer username =new StringBuffer(strUserName);
            username.deleteCharAt(t);
            username.insert(t, "%40");
            
          String strAuthString = "Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=" + username.toString() + ",pwd=" + strPassword + "," + strChallenge.substring(0, strChallenge.length()-2);
          //String strAuthString = "Passport1.4 OrgVerb=GET,OrgURL=http%3A%2F%2Fmessenger%2Emsn%2Ecom,sign-in=" + username.toString()+ ",pwd=" + strPassword;          

            try  
          {
            //URL         url = new URL( strLoginURL );
            String      strLocation;
            String      strAuthInfo;
            String      strAuthTicket;


            //System.out.println(">>> Starting login:" + this.loginServerURL);

            httpsConn = (HttpsConnection)Connector.open(this.loginServerURL);
            //httpsConn.setDoOutput( true );  
            //httpsConn.setRequestMethod("GET");
            httpsConn.setRequestProperty( "Authorization", strAuthString);
            httpsConn.setRequestProperty( "Host",this.loginServerURL);
            //httpsConn.setUseCaches( false );
            strLocation = httpsConn.getHeaderField( KEY_LOCATION );
            //System.out.println("KEY LOCATION:" + strLocation);
            // Server is redirecting login ???
            if( strLocation != null )  
            {
              //System.out.println( "<<< Server request redirecting login " );

              //url = new URL( strLocation );

              //System.out.println( ">>> Redirecting login to " + strLocation );

              httpsConn = null;
              httpsConn = (HttpsConnection)Connector.open(strLocation);
              //httpsConn.setDoOutput( true );      
              httpsConn.setRequestProperty( "Authorization", strAuthString );
              //httpsConn.setUseCaches( false );
            }

            strAuthInfo = httpsConn.getHeaderField( KEY_AUTHENTICATION_INFO );
            //httpsConn.close();
            httpsConn   = null;

            if( getField( DASTATUS, strAuthInfo ).compareTo( SUCCESS ) != 0 )
              return null;

            strAuthTicket = getField( TICKET, strAuthInfo );

            if( strAuthTicket != null )
              strAuthTicket = strAuthTicket.substring( 1, strAuthTicket.length() - 1 );

            return strAuthTicket;

          } catch(IOException e )  
          {
              e.printStackTrace();
            return null;
          }
        }

        return null;
    }    
}
