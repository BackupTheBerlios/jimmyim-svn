/*
 * MSNProtocol.java
 *
 * Created on Torek, 2006, april 11, 23:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import jimmy.MSNTransaction;
import java.io.*;
import java.util.Vector;
import javax.microedition.io.*;
//import hello.MSNServerHandler;
/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class MSNProtocol extends Protocol
{
    private String username;
    private String password;
    private int port;
    final String NsURL = "messenger.hotmail.com";
    final String ProductKey = "YMM8C_H7KCQ2S_KL"; 
    final String NSredirectURL = "baym-cs118.msgr.hotmail.com";
    //final String NexusURL = "nexus.passport.com/rdr/pprdr.asp";
    final int serverPort = 1863;
    final int NexusPort = 443;
    private PassportNexus pn;
    private MSNTransaction tr;
    private MSNServerHandler sh;
    private ServerHandler NexusHandler;
    private SocketConnection sc;
    private DataOutputStream os;
    private Vector contacts;
    private Vector groups;
    //private InputStreamReader isr;
   // private DataInputStream isr;
    
  final String               DALOGIN                      = "DALogin=";
  final String               DASTATUS                     = "da-status=";
  final String               TICKET                       = "from-PP=";
  final String               SUCCESS                      = "success";
  final String               KEY_PASSPORT_URLS            = "PassportURLs";
  final String               KEY_LOCATION                 = "Location";
  final String               KEY_AUTHENTICATION_INFO      = "Authentication-Info";
  static final String        PASSPORT_LIST_SERVER_ADDRESS = "https://nexus.passport.com/rdr/pprdr.asp";
    
    
    
    /**
     * The constructor method.
     * @param URL URL of the server. No protocolis specified here! Example: messenger.hotmail.com.
     * @param PORT PORT of the server(inputs as a String). Example: "1863".
     */
    public MSNProtocol(String username, String password)
    {
        this.username = username;
        this.password= password;        
    }
    /**
     * Initializes SocketConnection.
     */
    public void login()
    {
        try
        {
                this.sh= new MSNServerHandler(this.NsURL, this.serverPort);
                this.sh.connect();
            /*this.sc = (SocketConnection)Connector.open("socket://" + this.NsURL + ":" + Integer.toString(this.serverPort));
                this.os = this.sc.openDataOutputStream();
                this.isr = new DataInputStream(this.sc.openDataInputStream());*/
                
                this.tr = new MSNTransaction();
                this.tr.setType("VER");
                this.tr.addArgument("MSNP11");
                this.tr.addArgument("MSNP10");
                this.tr.addArgument("CVR0");
                //String message = "VER 1 MSNP8 CVR0\r\n";
                this.sh.sendRequest(this.tr.toString());
                System.out.print(this.tr.toString());
                System.out.println(this.sh.getReply());
                System.out.println("*************************************");
                //message = "CVR 2 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS avgustin.ocepek@yahoo.com.au\r\n";
                this.tr.newTransaction();
                this.tr.setType("CVR");
                this.tr.addArgument("0x040c winnt 5.1 i386 MSNMSGR 7.0.0777 msmsgs");
                this.tr.addArgument(this.username);
                
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                System.out.println(this.sh.getReply());
                System.out.println("*************************************");  
                
                //message="USR 3 TWN I avgustin.ocepek@yahoo.com.au\r\n";
                this.tr.newTransaction();
                this.tr.setType("USR");
                this.tr.addArgument("TWN I");
                this.tr.addArgument(this.username);                
                
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                System.out.println(this.sh.getReply());
                System.out.println("*************************************"); 
                this.sh.disconnect();

                this.sh.changeURL(this.NSredirectURL);
                this.sh.connect();
                
               
                this.tr.newTransaction();
                this.tr.setType("VER");
                this.tr.addArgument("MSNP11");
                this.tr.addArgument("MSNP10");
                this.tr.addArgument("CVR0");
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                System.out.println(this.sh.getReply());    
                System.out.println("*************************************");
                
                this.tr.newTransaction();
                this.tr.setType("CVR");
                this.tr.addArgument("0x0409 win 5.1 i386 MSNMSGR 7.0.0777 msmsgs");
                this.tr.addArgument(this.username);
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                System.out.println(this.sh.getReply());    
                System.out.println("*************************************");       
                
                this.tr.newTransaction();
                this.tr.setType("USR");
                this.tr.addArgument("TWN I");
                this.tr.addArgument(this.username); 
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());  
                String USRreply = this.sh.getReply();
                String challenge = USRreply.substring(12);
                System.out.println(USRreply);    
                System.out.println("*************************************");               
                
                //this is where the password stuff fun starts
                
                // We have to establish a connection MSN Passport network 
                // for authentification with user password.
                
                //Warning: connection to the NS should remain open during the communication with the PassportNexus
                                
                PassportNexus pn = new PassportNexus();
                pn.getPassportLoginServer();
                String ticket = pn.requestAuthorizationTicket(this.username, this.password, challenge);
                //System.out.println(ticket);
                
                this.tr.newTransaction();    
                this.tr.setType("USR");
                this.tr.addArgument("TWN S");
                this.tr.addArgument(ticket);
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                System.out.println(this.sh.getReply());    //gets the SBS
                System.out.println("*************************************");             
                
                
                this.tr.newTransaction();
                this.tr.setType("SYN");
                this.tr.addArgument("2006-04-14T06:03:32.863-07:00 2006-04-15T06:03:33.177-07:00");                  
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());  
                parseReply(this.sh.getReply());
                System.out.println("*************************************zzz");                  
                parseReply(this.sh.getReply()); 
                
                System.out.println("*************************************");                
                this.tr.newTransaction();
                this.tr.setType("CHG");
                this.tr.addArgument("BSY");
                this.tr.addArgument("0");
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                parseReply(this.sh.getReply());
                System.out.println("*************************************aaa");    
                System.out.println(this.sh.getReply());           
                
                System.out.println("*************************************bbb");   
                //StringBuffer challenge2 =new StringBuffer( this.sh.getReply().substring(6 ));
                parseReply(this.sh.getReply());

                System.out.println("*************************************Challenge:");
                parseReply(this.sh.getReply());

                /*MD5 md5 = new MD5();
                String hash = new String(md5.toHex(md5.fingerprint(challenge2.toString().getBytes())));
                System.out.println(hash);
                String[] hashTable = new String[4];
                hashTable[0] = hash.substring(0,8);
                hashTable[1] = hash.substring(8,16);
                hashTable[2] = hash.substring(16,24);
                hashTable[3] = hash.substring(24,hash.length());
                int[] md5Ints = new int[4];
                for(int i=0; i<hashTable.length; i++)
                {
                    md5Ints[i] = hexToInt(hashTable[i]) &  0x7FFFFFFF;
                    System.out.println(hashTable[i] + " -> "+md5Ints[i]);
                }*/
                
                
                //md5Ints[0] =  & 0x7fffffff;
                
                System.out.println("*************************************ee");  
                this.tr.newTransaction();
                this.tr.setType("QRY");
                

                //this.logout();
                while(true)
                {
                    if(5==3)
                    {
                        break;
                    }
                }             
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    /**
     * method disconnects the SocketConnection of this class.
     */
    public void logout()
    {
            System.out.println("*************************************");
            System.out.println("Logging out.");         
            this.sh.sendRequest(this.tr.getLogoutString());
            System.out.println("Logout successful.");
    }
 /*   /**
     * Sends a message to the remote server with the OutputStream.
     * @param message Message to be sent with OutputStream to the remote server using SocketConnection.
     */
    public void sendRequest(String message)
    {
        try 
        {
            //this.os = this.sc.openOutputStream();
            byte[] data = message.getBytes();
            
            this.os.write(data);
            this.os.flush();
         
        } catch (IOException ex) 
        {
            //ex.printStackTrace();
        }
    }
    public int hexToInt(String s)
    {
        int[] n = new int[s.length()];
        char c;
        int sum = 0;
        int koef = 1;
        for(int i=n.length-1; i>=0; i--)
        {
            c = s.charAt(i);
            //System.out.println(c);
            switch ((char)c)
            {
                case 48:
                    n[i] = 0;
                    break;
                case 49:
                    n[i] = 1;
                    break;
                case 50:
                    n[i] = 2;
                    break;
                case 51:
                    n[i] = 3;
                    break;
                case 52:
                    n[i] = 4;
                    break;
                case 53:
                    n[i] = 5;
                    break;
                case 54:
                    n[i] = 6;
                    break;
                case 55:
                    n[i] = 7;
                    break;
                case 56:
                    n[i] = 8;
                    break;
                case 57:
                    n[i] = 9;
                    break;                      
                case 97:
                    n[i] = 10;
                    break;
                case 98:
                    n[i] = 11;
                    break;
                case 99:
                    n[i] = 12;
                    break;
                case 100:
                    n[i] = 13;
                    break;
                case 101:
                    n[i] = 14;
                    break;
                case 102:
                    n[i] = 15;
                    break;
            }
            
            sum = sum + n[i]*koef;
            //System.out.println(sum);
            koef=koef*16;
        }
        return sum;
    }
    /**
     * Disconnects OutputStream.
     */
    public void closeOutput()
    {
        try 
        {
            this.os.flush();
            this.os.close();
        } catch (IOException ex) 
        {
            ex.printStackTrace();
        }
    }
    
    public void parseReply(String reply)
    {
        String type = reply.substring(0,3);
        System.out.println(reply);
        
        if(type.indexOf("LST")!=-1)
        {
            parseContacts(reply);
        }     
        if(type.indexOf("LSG")!=-1)
        {
            parseGroups(reply);
        }  
    }
    private void parseContacts(String data)
    {
         int t = data.indexOf("LST");
         int ind;
         int i;
         char c;
         StringBuffer contact;
        while(t!=-1)
        {   
             // parse email
             contact = new StringBuffer();
            ind = data.indexOf("N=", t);
            i = ind+2;
            while((c=data.charAt(i))!= ' ')
            {
                //System.out.println(c);
                contact.append(c);
                i++;
            }
            System.out.println(contact.toString());
            // parse nickname
            ind = data.indexOf("F=", t);
            i = ind+2;
            while((c=data.charAt(i))!= ' ')
            {
                //System.out.println(c);
                contact.append(c);
                i++;
            }            
            System.out.println("Contact: "+contact.toString());
            t = data.indexOf("LST", t+3);
        }
    }

    private void parseGroups(String data)
    {
        System.out.println("Parsing groups:" + data);
         int t = data.indexOf("LSG");
         int ind;
         int i;
         char c;
         StringBuffer group;
        while(t!=-1)
        {   
             // parse email
            group = new StringBuffer();
            i = t+4;
            while((c=data.charAt(i))!= ' ')
            {
                //System.out.println(c);
                group.append(c);
                i++;
            }            
            System.out.println("Group: "+group.toString());
            t = data.indexOf("LSG", t+3);
        }
    }    
    String getField( String strKey, String strField )  
    {

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
   public void disconnect()
   {
   
   }
   public void connect() 
   {
   
   }
   public String getReply()
   {
      return null;
   }
   
}
