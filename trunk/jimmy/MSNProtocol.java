/*
 * MSNProtocol.java
 *
 * Created on Torek, 2006, april 11, 23:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.*;

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
    final int serverPort = 1863;
    final int NexusPort = 443;
    private PassportNexus pn;
    private MSNTransaction tr;
    private ServerHandler sh;
    private ServerHandler NexusHandler;
    private Vector contacts_;
    protected Vector chatSessions_;	//list of active chat sessions
    protected Vector SessionIPs_;	//list of IPs of alive chat sessions    
    private Hashtable groupHash_;
    private String mySwitchboard;
    
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
    public MSNProtocol()
    {
        this.connected_ = false;
    }
    /**
     * Initializes SocketConnection.
     */
    public boolean login(String username, String password)
    {
        this.username = username;
        this.password= password;          
        try
        {
                this.sh= new ServerHandler(this.NsURL, this.serverPort);
                this.tr = new MSNTransaction();
                /*this.sh.connect();
                 *this.tr.newTransaction();
                
                this.tr.setType("VER");
                this.tr.addArgument("MSNP11");
                this.tr.addArgument("MSNP10");
                this.tr.addArgument("CVR0");
                //String message = "VER 1 MSNP8 CVR0\r\n";
                this.sh.sendRequest(this.tr.toString());
                System.out.print(this.tr.toString());
                
                /*System.out.println(this.sh.getReply());   
                System.out.println(this.sh.getReply());
                System.out.println(this.sh.getReply());
                System.out.println(this.sh.getReply());
                System.out.println(this.sh.getReply());
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
                this.sh.disconnect();*/

                this.sh = new ServerHandler(this.NSredirectURL, this.serverPort);
                this.sh.connect();
                
                this.tr.newTransaction();
                this.tr.setType("VER");
                this.tr.addArgument("MSNP11");
                this.tr.addArgument("MSNP10");
                this.tr.addArgument("CVR0");
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());                           
                parseReply(this.sh.getReply());    
                //System.out.println("*************************************");
                
                this.tr.newTransaction();
                this.tr.setType("CVR");
                this.tr.addArgument("0x0409 win 5.1 i386 MSNMSGR 7.0.0777 msmsgs");
                this.tr.addArgument(this.username);
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());    
                parseReply(this.sh.getReply());    
                //System.out.println("*************************************");       
                
                this.tr.newTransaction();
                this.tr.setType("USR");
                this.tr.addArgument("TWN I");
                this.tr.addArgument(this.username); 
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                String USRreply = this.sh.getReply();
                String challenge = USRreply.substring(12);
                //System.out.println(USRreply);    
                //System.out.println("*************************************");               
                
                //this is where the password stuff fun starts
                
                // We have to establish a connection MSN Passport network 
                // for authentification with user password.
                
                //Warning: connection to the NS should remain open during the communication with the PassportNexus
                                
                PassportNexus pn = new PassportNexus();
                pn.getPassportLoginServer();
                String ticket = pn.requestAuthorizationTicket(this.username, this.password, challenge);
                //System.out.println(ticket);
                
                if(ticket == null)
                {
                    this.connected_ = false;
                    return false;
                }
                
                this.tr.newTransaction();    
                this.tr.setType("USR");
                this.tr.addArgument("TWN S");
                this.tr.addArgument(ticket);
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());                           
                parseReply(this.sh.getReply());    //gets the SBS
                //System.out.println("*************************************");                
                this.tr.newTransaction();
                this.tr.setType("SYN");
                this.tr.addArgument("2006-04-14T06:03:32.863-07:00 2006-04-15T06:03:33.177-07:00");                  
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                parseReply(this.sh.getReply());
             
                this.tr.newTransaction();
                this.tr.setType("CHG");
                this.tr.addArgument("BSY");
                this.tr.addArgument("0");
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());                           
                parseReply(this.sh.getReply());

                //System.out.println("*************************************Challenge:");
                String reply;
                while((reply=this.sh.getReply()) != null)
                {
                    parseReply(reply);
                }    
                this.connected_ = true;
                return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
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
    
    public void parseReply(String reply)
    {
        //System.out.println(reply);
        if(reply == null)
        {
            return;
        }
        if(reply.indexOf("LSG")!=-1)
        {
            parseGroups(reply);
        }          
        if(reply.indexOf("LST")!=-1)
        {
            parseContacts(reply);
        }     
        if(reply.indexOf("CHL")!=-1)
        {
            parseChallenge(reply);
        }          
    }
    private void parseContacts(String data)
    {
         if(this.contacts_ == null)
         {
                this.contacts_ = new Vector();
         }
         //System.out.println("Parsing contacs:");
         int t = data.indexOf("LST");
         int ind;
         int i;
         char c;
         Contact person;
         StringBuffer contact;
         StringBuffer username;         
         StringBuffer groupID;
        while(t!=-1)
        {   
            i = t;
            contact = new StringBuffer();
            username = new StringBuffer();
            c=data.charAt(i);
            while(c!= '\n')
            {
                contact.append(c);
                i++;
                c=data.charAt(i);
            }
            
            //System.out.println("Parsed contact line:"+contact.toString());
            
             // parse email
            ind = contact.toString().indexOf("N=");           
            i = ind+2;
            while((c=contact.charAt(i))!= ' ')
            {
                //System.out.println(c);
                username.append(c);
                i++;
            }
            //System.out.println("Username:" + username.toString());
            person = new Contact(username.toString(), this);
            // parse nickname
            ind = contact.toString().indexOf("F=");
            i = ind+2;
            username = new StringBuffer();
            while((c=contact.charAt(i))!= ' ')
            {
                //System.out.println(c);
                username.append(c);
                i++;
            }
            //System.out.println("Screen name:"+ username.toString());
            person.setScreenName(username.toString());
            //person = new Contact();
            
            ind = contact.toString().indexOf("C=");
            ind = contact.toString().indexOf(' ', ind+1);
            ind = contact.toString().indexOf(' ', ind+1);
            if(ind!=-1)
            {
                i = ind+1;
                
                username = new StringBuffer();
                // read group id
                while((c=contact.charAt(i))!= '\r')
                {
                    //System.out.println(c);
                    username.append(c);
                    i++;
                }
                //System.out.println("Group id:"+ username.toString()); 
                
                person.setGroupName((String)this.groupHash_.get(new Integer(username.toString().hashCode())));
            }
            this.contacts_.addElement(person);
            //System.out.println("Contact: "+contact.toString());
            t = data.indexOf("LST", t+3);
        }
    }
    private void parseGroups(String data)
    {
        if(this.groupHash_ == null)
        {
            this.groupHash_ = new Hashtable();
        }
         //System.out.println("Parsing groups:" + data);
         int t = data.indexOf("LSG");
         int ind;
         int i;
         int count=0;
         
         char c;
         StringBuffer group;
         StringBuffer groupID;
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
            i++;
            groupID = new StringBuffer();
            while((c=data.charAt(i))!= '\n')
            {
                //System.out.println(c);
                groupID.append(c);
                i++;
            }
            
            //System.out.println("Group id:"+groupID.toString());
            //System.out.println("Group: "+group.toString());
            this.groupHash_.put(new Integer(groupID.toString().substring(0, groupID.length()-1).hashCode()), group.toString());
            //this.groupName_.addElement(group.toString());
            count++;
            t = data.indexOf("LSG", t+3);
        }
    }    
    private void parseChallenge(String data)
    {
            StringBuffer challenge =new StringBuffer(data.substring(6));
            MD5 md5 = new MD5();
            
            String hash = new String(md5.toHex(md5.fingerprint(challenge.toString().getBytes())));
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
            }
            
            this.tr.newTransaction();
            this.tr.setType("QRY");
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
   void printContacts() 
   {
       System.out.println("*********************************************************");
       System.out.println("START OF CONTACT LISTING");
       System.out.println("*********************************************************");       
        if(this.contacts_ ==null)
        {
            System.out.println("No contacts available.");
        }
        else
        {
           Contact c;
            for(int i = 0; i<this.contacts_.size();i++)
            {
               c =(Contact) this.contacts_.elementAt(i);
                System.out.println("Contact " + i +":" + c.screenName() + ", group:" + c.groupName());
            }
        }  
       System.out.println("*********************************************************");
       System.out.println("END OF CONTACT LISTING");
       System.out.println("*********************************************************");        
   }   
   void printGroups() 
   {
       System.out.println("*********************************************************");
       System.out.println("START OF GROUP LISTING");
       System.out.println("*********************************************************");           
       /*if(this.groupName_ == null)
        {
            System.out.println("No groups available.");
        }
        else
        {
            for(int i=0; i<this.groupName_.size(); i++)
            {
                System.out.println("Group " + i +":" + this.groupName_.elementAt(i));
            }
        }*/
       System.out.println("*********************************************************");
       System.out.println("END OF GROUP LISTING");
       System.out.println("*********************************************************");           
   } 
   public Vector getChatSessions() {return chatSessions_;}
   public ChatSession startChatSession(Contact c)
   { 
        if(this.chatSessions_==null)
        {
            this.chatSessions_ = new Vector();
        }
        if(this.SessionIPs_==null)
        {
            this.SessionIPs_ = new Vector();
        }        
        ServerHandler switchHandler;
        ChatSession cs = new ChatSession(this);
        this.tr.newTransaction();
        this.tr.setType("XFR");
        this.tr.addArgument("SB");
        this.sh.sendRequest(this.tr.toString());
        String line = this.sh.getReply();
        //System.out.println(line);
        switchHandler = new ServerHandler(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6),1863);
        switchHandler.connect(); 
        switchHandler.sendRequest("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
        //System.out.println(switchHandler.getReply());
        switchHandler.sendRequest("CAL 2 "+c.userID()+"\r\n");
        cs.setID(2);
        while((line = switchHandler.getReply())!=null)
        {   
            // System.out.println(line);
            // do something, not necessary :)
        }
        this.chatSessions_.addElement(cs);
        this.SessionIPs_.addElement(switchHandler);
        //System.out.println(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        //System.out.println("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")-2) + "\r\n");        
        return cs;
   }
    /**
     * Send a message.
     * 
     * @param msg Message in String
     * @param session Active Chat Session to send the message to
     */
    public void sendMsg(String msg, ChatSession session)
    {
        //System.out.println("Sending message...");
        for(int i=0; i<this.chatSessions_.size();i++)
        {
            if(this.chatSessions_.elementAt(i).equals(session))
            {
                ServerHandler sh = (ServerHandler) this.SessionIPs_.elementAt(i);
                String payload = "MIME-Version: 1.0\r\nContent-Type: text/x-msmsgscontrol\r\nTypingUser: "+this.username+"\r\n\r\n\r\n";
                //System.out.println("Payload:"+payload);
                //System.out.println("Payload length:"+payload.length());
                sh.sendRequest("MSG "+session.getID()+" U "+payload.length() + "\r\n"+payload);
                //System.out.println("MSG "+session.getID()+" U "+payload.length() + "\r\n"+payload);
                session.setID(session.getID()+1);
                payload = "MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nX-MMS-IM-Format: FN=MS%20Sans%20Serif; EF=; CO=0; CS=0; PF=0\r\n\r\n";
                //System.out.println("Payload2:"+payload);
                //System.out.println("Payload length2:"+payload.length());                
                sh.sendRequest("MSG "+session.getID()+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                //System.out.println("MSG "+session.getID()+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                session.setID(session.getID()+1);
                return;
            }
        }    
    }
    /**
     * This method is implemented by convenience.
     * 
     * @param msg Message in String
     * @param user Specific user this message should be sent to
     * @param session Active Chat Session to send the message to
     */
    public void sendMsg(String msg, Contact user, ChatSession session)
    {  
    }
    public Vector getContacts() { return this.contacts_; }
}
