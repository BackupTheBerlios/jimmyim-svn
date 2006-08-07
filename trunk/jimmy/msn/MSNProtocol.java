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
 File: jimmy/MSNProtocol.java
 Version: alpha  Date: 2006/04/11
 Author(s): Zoran Mesec
 */

package jimmy.msn;

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.io.*;
import jimmy.net.ServerHandler;
import jimmy.util.MD5;
import jimmy.*;
import jimmy.msn.*;


/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class MSNProtocol extends Protocol
{
    private String username;
    private String password;
    private boolean connected_;
    private int port;
    final String NsURL = "messenger.hotmail.com";
    final String ProductKey = "YMM8C_H7KCQ2S_KL"; 
    final String ProductID = "PROD0090YUAUV{2B";
    final String NSredirectURL = "baym-cs295.msgr.hotmail.com";
    private static final long MSNP11_MAGIC_NUM = 0x0E79A9C1L;
    final int serverPort = 1863;
    final int NexusPort = 443;
    private PassportNexus pn;
    private MSNTransaction tr;
    private Contact pendingUser=null;
    private ServerHandler sh;
    private boolean busy=false;
    private ServerHandler NexusHandler;
    private Vector contacts_;
    protected Vector chatSessions_;	//list of active chat sessions
    protected Vector SessionHandlers_;	//list of IPs of alive chat sessions   
    protected Hashtable ChatIds_;
    protected Hashtable csSHInteraction; //interaction between ChatSessions and ServerHandlers
    
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
    public MSNProtocol(ProtocolInteraction jimmy)
    {
    	super(jimmy);
        this.connected_ = false;
        this.protocolType_ = MSN;
        this.busy = false;
    }
    /**
     * 
     * @see 
     */
    public boolean login(Account acc)
    {
        return false;
    }    
    /**
     * This function logs a user to MSN service. A valid username& password is required to perform this task.
     * @param username Username for MSN Passport(example: john.doe@hotmail.com).
     * @param password Password for MSN Passport(example: john.doe@hotmail.com).
     * @return True if login succeded, or false on failure.
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
                this.tr.newTransaction();
                
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
                this.tr.addArgument(username);
                
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
                if(ticket == null)
                { 
                    this.status_ = WRONG_PASSWORD;
                    return false;
                }
                //System.out.println("Ticket:" + ticket);
                
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
                this.status_ = CONNECTED;
                startThread();
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
    /*public long longToHex(long n)
    {
        long r;     
            switch (n)
            {
                case 48:
                    r= 30;
                    break;
                case 49:
                    r = 31;
                    break;
                case 50:
                    r = 32;
                    break;
                case 51:
                    r = 33;
                    break;
                case 52:
                    r = 34;
                    break;
                case 53:
                    r = 35;
                    break;
                case 54:
                    r = 36;
                    break;
                case 55:
                    r = 37;
                    break;
                case 56:
                    r = 38;
                    break;
                case 57:
                    r = 39;
                    break; 
                default:
                    r = 0;
                    break;
            }            
            return r;
    } */   
    public int intToHex(int n)
    {
        int r;     
            switch (n)
            {
                case 48:
                    r= 30;
                    break;
                case 49:
                    r = 31;
                    break;
                case 50:
                    r = 32;
                    break;
                case 51:
                    r = 33;
                    break;
                case 52:
                    r = 34;
                    break;
                case 53:
                    r = 35;
                    break;
                case 54:
                    r = 36;
                    break;
                case 55:
                    r = 37;
                    break;
                case 56:
                    r = 38;
                    break;
                case 57:
                    r = 39;
                    break; 
                default:
                    r = 0;
                    break;
            }            
            return r;
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
    public long hexToLong(String s)
    {
        long[] n = new long[s.length()];
        char c;
        long sum = 0;
        long koef = 1;
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
     * This method ANDs a hexadecimal number(represented as a string) with 0x7FFFFFFF and returns the result as a string.
     * @return A string representation of a hexadecimal number.
     * @param s A string representation of the hexadecimal number.
     */
    public String andHex(String s)
    {
        StringBuffer sb = new StringBuffer();
        
        int n=0;
        char c;
       
        for(int i=0; i<s.length(); i++)
        {
            c = s.charAt(i);
            //System.out.println(c);
            
            switch ((char)c)
            {
                case 48:
                    n = 0;
                    break;
                case 49:
                    n = 1;
                    break;
                case 50:
                    n = 2;
                    break;
                case 51:
                    n = 3;
                    break;
                case 52:
                    n = 4;
                    break;
                case 53:
                    n = 5;
                    break;
                case 54:
                    n = 6;
                    break;
                case 55:
                    n = 7;
                    break;
                case 56:
                    n = 8;
                    break;
                case 57:
                    n = 9;
                    break;                      
                case 97:
                    n = 10;
                    break;
                case 98:
                    n = 11;
                    break;
                case 99:
                    n = 12;
                    break;
                case 100:
                    n = 13;
                    break;
                case 101:
                    n = 14;
                    break;
                case 102:
                    n = 15;
                    break;
            }
            if(i==0)
            {
                n = n & 0x7;
            }
            else
            {
                n = n & 0xF;
            }  
            
            switch(n)
            {
                case 10:
                    sb.append('a');
                    break;
                case 11:
                    sb.append('b');
                    break;
                case 12:
                    sb.append('c');
                    break;
                case 13:
                    sb.append('d');
                    break;
                case 14:
                    sb.append('e');
                    break;
                 case 15:
                    sb.append('f');
                    break;      
                 default:
                     sb.append(n);
                     break;
            }
                  
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }    
    
    /**
     * Parses a reply from the server according to first three letters.
     * @param reply raw data from the server, presented as a string
     */
    public void parseReply(String reply)
    {
        System.out.println("parse reply method **************");
        //this.parseChallenge("aaaa");   
        if(reply == null)
        {
            return;
        }
        System.out.println("Parse reply:"+reply);
        if(reply.indexOf("ILN")!=-1)
        {
            parsePresence(reply);   
            //this.printContacts();
            //jimmy_.addContacts(this.contacts_); 
        }        
        if(reply.indexOf("LSG")!=-1)
        {
            parseGroups(reply);
        }     
        if(reply.indexOf("XFR")!=-1)
        {
            startSession(reply);
        } 
        if(reply.indexOf("FLN")!=-1)
        {
            userGoesOffline(reply);
        }           
        if(reply.indexOf("LST")!=-1)
        {
            parseContacts(reply);
        }     
        if(reply.indexOf("CHL")!=-1)
        {
            //parseChallenge(reply);
            //System.gc();    // let's clean this mess
        }     
        if(reply.indexOf("RNG")!=-1)
        {
            SBhandle(reply);
        }      
        /*if(reply.indexOf("MSG")!=-1)
        {
            parseMessage(reply);
        }   */ 
        System.out.println("***************End of Parse Reply********************");
        
    }
    private void parseMessage(String data, int SHid)
    {
        System.out.println("parse message function **************");
        if(data.indexOf("TypingUser:")==-1)
        {
            System.out.println("Prejeto:" + data);
            ChatSession activeCS = (ChatSession)this.chatSessions_.elementAt(SHid);

            Vector allContacts = activeCS.getContactsList();
            String uID =  data.substring(4,data.indexOf(" ", 10));
            System.out.println("Receiving message from:" + uID);
            Contact c = null;
            for(int i=0;i<allContacts.size(); i++)
            {
                c = (Contact)allContacts.elementAt(i);
                if(uID.compareTo(c.userID())==0)
                {
                    break;
                }
            }
	    int marker = data.indexOf("X-MMS-IM-Format:");
	    int marker2 = data.indexOf("MSG", 10);
	    if(marker2==-1)
	    {
		System.out.println("Message:"+ marker + data.substring(data.indexOf("\n",marker+4)+3));

		this.jimmy_.msgRecieved(activeCS, c, data.substring(data.indexOf("\n",marker+4)+3));	    
	    }
	    else
	    {
		System.out.println("Message:"+ marker + data.substring(data.indexOf("\n",marker+4)+3,marker2));

		this.jimmy_.msgRecieved(activeCS, c, data.substring(data.indexOf("\n",marker+4)+3,marker2));	    
	    }

        }
    }
    private void parseContacts(String data)
    {        
        System.out.println("parse contacts function **************");
         Vector newContacts = new Vector(); 
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
            person = new Contact(username.toString(), (Protocol)this);
            
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
            newContacts.addElement(person);
            //System.out.println("Contact: "+contact.toString());
            t = data.indexOf("LST", t+3);
        }
        jimmy_.addContacts(newContacts);
    }
    private void userGoesOffline(String data)
    {
        //FLN matevz.jekovec@guest.arnes.si
        String uID = data.substring(4, data.length()-2);
        System.out.println("User to go ofline:"+uID);
        Contact c = null;
        for(int i=0; i<this.contacts_.size();i++)
        {
            c = (Contact)this.contacts_.elementAt(i);
            if(uID.compareTo(c.userID())==0)
            {
                break;
            }
        }
        c.setStatus(Contact.ST_OFFLINE);
        this.jimmy_.changeContactStatus(c);
    }
    private void startSession(String line)
    {
	ServerHandler switchHandler;
        System.out.println("Server RNG ip:"+line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        switchHandler = new ServerHandler(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6),1863);
        switchHandler.connect(); 
        switchHandler.sendRequest("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
        System.out.println("Posiljam:USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
	
	switchHandler.sendRequest("CAL 2 "+this.pendingUser.userID()+"\r\n");
	this.pendingUser = null;
	this.SessionHandlers_.addElement(switchHandler);
        /*while((line = switchHandler.getReply())!=null)
        {   
            // System.out.println(line);
            // do something, not necessary :)
        }
        
	 XFR 7 SB 207.46.26.51:1863 CKI 175908857.21410620.3819212

	 String line = null;
        this.busy = false;
        System.out.println("Ime:"+c.screenName());
        System.out.println("Test:"+line);
        if(line == null)
        {
            return null;
        }
        System.out.println("Server RNG ip:"+line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        switchHandler = new ServerHandler(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6),1863);
        switchHandler.connect(); 
        switchHandler.sendRequest("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
        //System.out.println(switchHandler.getReply());
        switchHandler.sendRequest("CAL 2 "+c.userID()+"\r\n");
        while((line = switchHandler.getReply())!=null)
        {   
            // System.out.println(line);
            // do something, not necessary :)
        }

        this.csSHInteraction.put(switchHandler, cs);
        
        this.SessionHandlers_.addElement(switchHandler);
        //System.out.println(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        //System.out.println("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")-2) + "\r\n");*/        
	
    }
    private void parseGroups(String data)
    {
        System.out.println("parse groups function **************");
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
    
    private void parsePresence(String data)
    {
         System.out.println("Parsing presence:" + data);
         int t = data.indexOf("ILN");
         int ind;
         int count=0;
        
         char c;
         StringBuffer group;
         StringBuffer groupID;
         String presence;
         String uID;
         Contact con;
         while(t!=-1)
         {   
             presence = data.substring(t+6, t+9);
             //System.out.println("type:" + presence);
             uID = data.substring(t+10, data.indexOf(' ', t+11));
             //System.out.println("user:" + uID);
             for(int i=0; i<this.contacts_.size(); i++)
             {
                 con = (Contact)this.contacts_.elementAt(i);
                 if(con.userID().compareTo(uID)==0)
                 {
                    if(presence.compareTo("BSY")==0)
                    {
                        con.setStatus(Contact.ST_BUSY);  
                    }
                    else if((presence.compareTo("IDL")==0) || (presence.compareTo("NLN")==0))
                    {
                        con.setStatus(Contact.ST_ONLINE);
                    }   
                    else if(presence.compareTo("AWY")==0)
                    {
                        con.setStatus(Contact.ST_AWAY);
                    }  
                    this.jimmy_.changeContactStatus(con);
                 }  
             }
            t = data.indexOf("ILN", t+3);
        }   
    }
    
    private void parseChallenge(String data)
    {
            System.out.println("parse challenge function **************");
            //System.out.println("Parsing challenge: " + data);
            
            // first step
            //StringBuffer challenge =new StringBuffer(data.substring(6,data.length()-2));
            //challenge.append(this.ProductKey);
            //System.out.println(challenge);
            
            MD5 md5 = new MD5();
            
            //String hash = new String(md5.toHex(md5.fingerprint(challenge.toString().getBytes())));
            
            //System.out.println(hash);
            String hash = new String(md5.toHex(md5.fingerprint("22210219642164014968YMM8C_H7KCQ2S_KL".getBytes())));
            
            System.out.println("MD5 hash:"+hash);
            StringBuffer[] hashes = new StringBuffer[4];
            hashes[0] = new StringBuffer(hash.substring(0,8));
            hashes[1] = new StringBuffer(hash.substring(8,16));
            hashes[2] = new StringBuffer(hash.substring(16,24));
            hashes[3] = new StringBuffer(hash.substring(24,hash.length()));
            
            int[] md5hash = new int[4];
            char c;
            for(int i=0; i<hashes.length; i++)
            {
                //System.out.println("bEFORE:"+hashes[i]);
                c = hashes[i].charAt(6);
                hashes[i].setCharAt(6, hashes[i].charAt(0));
                hashes[i].setCharAt(0, c);
                
                c = hashes[i].charAt(7);
                hashes[i].setCharAt(7, hashes[i].charAt(1));
                hashes[i].setCharAt(1, c);  
                
                c = hashes[i].charAt(2);
                hashes[i].setCharAt(2, hashes[i].charAt(4));
                hashes[i].setCharAt(4, c);
                
                c = hashes[i].charAt(3);
                hashes[i].setCharAt(3, hashes[i].charAt(5));
                hashes[i].setCharAt(5, c);                

                //System.out.println("aFTER:"+hashes[i].toString());
                //md5hash[i] = hexToInt(hashes[i].toString()) &  0x7FFFFFF;
                md5hash[i] = hexToInt(andHex(hashes[i].toString()));
                System.out.println(andHex(hashes[i].toString()) + " -> "+md5hash[i]);
                
                
            }
            for(int i=0; i<md5hash.length; i++)
            {
                
            }
            
            // second step
            StringBuffer chlString;
            /*chlString = new StringBuffer(data.substring(6,data.length()-2));
            chlString.append(this.ProductID);*/
            chlString = new StringBuffer("22210219642164014968");
            chlString.append(this.ProductID);
            System.out.println(chlString);
            int r = (int)Math.ceil((double) chlString.length() / 8) * 8 - chlString.length();
            for(int i=0; i< r; i++)
            {
                chlString.append('0');
            }
            System.out.println(chlString);
            
            String tempS = chlString.toString();
            
            String[] hashInts = new String[5];
            hashInts[0] = tempS.substring(0,4);
            hashInts[1] = tempS.substring(4,8);
            hashInts[2] = tempS.substring(8,12);
            hashInts[3] = tempS.substring(12,16);      
            hashInts[4] = tempS.substring(16,20);
            //hashInts[5] = hash.substring(20,24);
            /*hashInts[6] = hash.substring(24,28);
            hashInts[7] = hash.substring(28,32);         
            hashInts[8] = hash.substring(32,36);  
            hashInts[9] = hash.substring(36,hash.length());  */            
            
            
            int[] chlStringArray = new int[10];
            chlStringArray[5] = 1146049104;
            chlStringArray[6] = 809054256;
            chlStringArray[7] = 1430345049;
            chlStringArray[8] = 1110604630;
            chlStringArray[9] = 808464432;   
            
            StringBuffer piece;
            for(int i = 0; i<hashInts.length;i++)
            {
                //System.out.println("TEST:" + hashInts[i]);
                piece = new StringBuffer();
                for(int j=3;j>=0;j--)
                {
                    piece.append(this.intToHex((int)hashInts[i].charAt(j)));
                }                
                //System.out.println("Piece:"+piece.toString());
                chlStringArray[i] = hexToInt(piece.toString());
                //System.out.println("Hex:" + chlStringArray[i]);
            }
            for(int i=0; i<chlStringArray.length; i++)
            {
                System.out.println("Hex:" + chlStringArray[i]);
            }
            
            // third step
            
            long high = 0;
            long low = 0;

            for (int i = 0; i < chlStringArray.length; i = i + 2) 
            {
              long temp = chlStringArray[i];
              temp = (0x0E79A9C1L * temp) % 0x7FFFFFFF;
              temp += high;
              temp = md5hash[0] * temp + md5hash[1];
              temp = temp % 0x7FFFFFFF;

              high = chlStringArray[i + 1];
              high = (high + temp) % 0x7FFFFFFF;
              high = md5hash[2] * high + md5hash[3];
              high = high % 0x7FFFFFFF;

              low = low + high + temp;
            }
            high = (high + md5hash[1]) % 0x7FFFFFFF;
            low = (low + md5hash[3]) % 0x7FFFFFFF;

        /*long high = 0;
        long low = 0;
        for (int i = 0; i < chlStringArray.length; i = i + 2) {
            long temp = (((chlStringArray[i] * MSNP11_MAGIC_NUM) % 0x7FFFFFFF) + high);
            temp = ((temp * md5hash[0]) + md5hash[1]) % 0x7FFFFFFF;

            high = (chlStringArray[i + 1] + temp) % 0x7FFFFFFF;
            high = (md5hash[2] * high + md5hash[3]) % 0x7FFFFFFF;

            low = low + high + temp;
        }
        high = (high + md5hash[1]) % 0x7FFFFFFF;
        low = (low + md5hash[3]) % 0x7FFFFFFF;  */       
           
           
            /*long high = 0;
            long low = 0;
            for (int i = 0; i < chlStringArray.length; i = i + 2) {
                long temp = (((chlStringArray[i] * MSNP11_MAGIC_NUM) % 0x7FFFFFFF) + high);
                temp = ((temp * md5hash[0]) + md5hash[1]) % 0x7FFFFFFF;

                high = (chlStringArray[i + 1] + temp) % 0x7FFFFFFF;
                high = (md5hash[2] * high + md5hash[3]) % 0x7FFFFFFF;

                low = low + high + temp;
            }
            high = (high + md5hash[1]) % 0x7FFFFFFF;
            low = (low + md5hash[3]) % 0x7FFFFFFF;    */        
            
            
            
            System.out.println("low:" + low);
            System.out.println("high:" + high);
            // Gives high = 0x69A5A771 (1772463985 decimal) and low = 0xD4020628 (3556902440 decimal)

            long key = (high << 32) + low;
            // Gives 0x69a5a771d4020628 (7612674852469737000 decimal)            
            System.out.println("Key:" + key); 
            long challenge = hexToLong(hash.substring(0,16))^key + hexToLong(hash.substring(16, 16))^key;
            
            this.tr.newTransaction();
            this.tr.setType("QRY");
            this.tr.addArgument(this.ProductID);
            this.tr.addArgument("32\r\n");
            //this.tr.addArgument(longToHex(challenge));
            System.out.println(this.tr.toStringNN());
            
            this.sh.sendRequest(this.tr.toStringNN());            
    }  
    private void SBhandle(String data)
    {
//RNG 1083693517 207.46.26.110:1863 CKI 18321026.739392 zoran.mesec@siol.net Zoran%20Mesec
//ANS 1 name_123@hotmail.com 18321026.739392 11752013\r\n
        System.out.println("SB handle function **************");
        String authNr = data.substring(data.indexOf(" ")+1, data.indexOf(" ", 7));
        System.out.println("Number:"+authNr);
        String sbIP = data.substring(data.indexOf(" ", 7)+1,data.indexOf("CKI")-1);
        System.out.println("Switchboard IP:"+sbIP);
        String authKey = data.substring(data.indexOf("CKI")+4, data.indexOf(" ", data.indexOf("CKI")+5));
        System.out.println(authKey);
        MSNTransaction answer = new MSNTransaction();
        answer.newTransaction();    //set id to 1
        answer.setType("ANS");
        answer.addArgument(this.username);
        answer.addArgument(authKey);
        answer.addArgument(authNr);
        
        System.out.println("Transaction:" + answer.toString());
        ServerHandler sbHandler = new ServerHandler(sbIP);
        sbHandler.connect();
        sbHandler.sendRequest(answer.toString());
        if(this.chatSessions_==null)
        {
            this.chatSessions_ = new Vector();
        }
        if(this.SessionHandlers_==null)
        {
            this.SessionHandlers_ = new Vector();
        }      
        if(this.ChatIds_==null)
        {
            this.ChatIds_ = new Hashtable();
        }   
        if(this.csSHInteraction==null)
        {
            this.csSHInteraction = new Hashtable();
        }  
        this.SessionHandlers_.addElement(sbHandler);
        Contact c = null;
        int t = data.indexOf(" ", data.indexOf("CKI")+5);
        String uID = data.substring(t, data.indexOf(" ", t+1));
        System.out.println("Calling user:" + uID);
        for(int i=0; i<this.contacts_.size();i++)
        {
            c = (Contact)this.contacts_.elementAt(i);
            if(uID.compareTo(c.userID())==0)
            {
                break;
            }
        }
        ChatSession cs = new ChatSession(this, c);
        this.ChatIds_.put(new Integer(cs.hashCode()), new Integer(2));
        this.csSHInteraction.put(sbHandler, cs);
        this.chatSessions_.addElement(cs);   
	
    }    
    
    private String getField( String strKey, String strField )  
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
   public void printContacts() 
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
                System.out.println("Contact " + i +":" + c.screenName() + ", group:" + c.groupName() + ", status:" + c.status());
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
    /**
     * This method start a Chatsession(a conversation) with a person. The person is given as a function parameter.
     * @param c contact we wish to converse.
     * @return an instance of the newly created ChatSession.
     */
   public ChatSession startChatSession(Contact c)
   { 
       this.pendingUser = c;
        if(this.chatSessions_==null)
        {
            this.chatSessions_ = new Vector();
        }
        if(this.SessionHandlers_==null)
        {
            this.SessionHandlers_ = new Vector();
        }      
        if(this.ChatIds_==null)
        {
            this.ChatIds_ = new Hashtable();
        }     
        if(this.csSHInteraction==null)
        {
            this.csSHInteraction = new Hashtable();
        }     
        ChatSession cs = new ChatSession(this, c);
        this.busy = true;
        this.tr.newTransaction();
        this.tr.setType("XFR");
        this.tr.addArgument("SB");
        this.sh.sendRequest(this.tr.toString());
        System.out.println(this.tr.toString());
        /*String line = null;
        this.busy = false;
        System.out.println("Ime:"+c.screenName());
        System.out.println("Test:"+line);
        if(line == null)
        {
            return null;
        }
        System.out.println("Server RNG ip:"+line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        switchHandler = new ServerHandler(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6),1863);
        switchHandler.connect(); 
        switchHandler.sendRequest("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
        //System.out.println(switchHandler.getReply());
        switchHandler.sendRequest("CAL 2 "+c.userID()+"\r\n");
        while((line = switchHandler.getReply())!=null)
        {   
            // System.out.println(line);
            // do something, not necessary :)
        }
        this.ChatIds_.put(new Integer(cs.hashCode()), new Integer(2));
        this.chatSessions_.addElement(cs);
        this.csSHInteraction.put(switchHandler, cs);
        
        this.SessionHandlers_.addElement(switchHandler);
        //System.out.println(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        //System.out.println("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")-2) + "\r\n");*/        
        this.ChatIds_.put(new Integer(cs.hashCode()), new Integer(2));
        this.chatSessions_.addElement(cs);
	return cs;
   }
    /**
     * Send a message.
     * @param msg Message in String
     * @param session Active Chat Session to send the message to
     */
    public void sendMsg(String msg, ChatSession session)
    {
        //System.out.println("Sending message...");
        int Sid;    // session ID
        Integer id;
        for(int i=0; i<this.chatSessions_.size();i++)
        {
            if(this.chatSessions_.elementAt(i).equals(session))
            {
		System.out.println("Inside:"+i+"size:"+this.SessionHandlers_.size());
		/*while(this.SessionHandlers_.size()<i)
		{
		    
		}*/
                ServerHandler sh = (ServerHandler) this.SessionHandlers_.elementAt(i);
                String payload = "MIME-Version: 1.0\r\nContent-Type: text/x-msmsgscontrol\r\nTypingUser: "+this.username+"\r\n\r\n\r\n";
                //System.out.println("Payload:"+payload);
                //System.out.println("Payload length:"+payload.length());
                id = (Integer)this.ChatIds_.get(new Integer(session.hashCode()));
                Sid = id.intValue();
                sh.sendRequest("MSG " + Sid + " U "+payload.length() + "\r\n"+payload);              
                //System.out.println("MSG "+session.getID()+" U "+payload.length() + "\r\n"+payload);
                Sid++;
                
                payload = "MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nX-MMS-IM-Format: FN=MS%20Sans%20Serif; EF=; CO=0; CS=0; PF=0\r\n\r\n";
                //System.out.println("Payload2:"+payload);
                //System.out.println("Payload length2:"+payload.length());                
                sh.sendRequest("MSG "+Sid+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                Sid++;
                //System.out.println("MSG "+session.getID()+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                this.ChatIds_.put(new Integer(session.hashCode()), new Integer(Sid));
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
    public void sendMsg(String msg, Vector users, ChatSession session)
    {  
    }
    public Vector getContacts() { return this.contacts_; }
    
    public void run() 
    {
        System.out.println("run method function **************");
        while(true)
        {
            System.out.println("start of wihle loop **************");
            try 
            {
                Thread.sleep(1000);
            } catch (InterruptedException e) 
            {
				// TODO Auto-generated catch block
		e.printStackTrace();
            }  
            if(this.SessionHandlers_==null)
            {
                this.SessionHandlers_=new Vector();
            }
            for(int i=0; i<this.SessionHandlers_.size();i++)
            {
                ServerHandler shTemp = (ServerHandler)this.SessionHandlers_.elementAt(i);
                String reply = shTemp.getReply();
                
                if(reply!=null)
                {
                    if(reply.indexOf("MSG")!=-1)
                    {
                        //System.out.println("Handler reply:" + reply);
                        parseMessage(reply, i);
                    }
                }
            }            
            if(this.status_ != CONNECTED) 
            {
                continue;
            }
            parseReply(this.sh.getReply());
            System.out.println("end of wihle loop **************");
        }  
        
    }
    
    public void addContact(Contact c){
	    
    }
}
