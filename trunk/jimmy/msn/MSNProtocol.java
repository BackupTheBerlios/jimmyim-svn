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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import jimmy.net.ServerHandler;
import jimmy.util.MD5;
import jimmy.Protocol;
import jimmy.Account;
import jimmy.Contact;
import jimmy.ChatSession;
import jimmy.ProtocolInteraction;
import jimmy.util.Utils;
/**
 * This class is used to connect with a remote server using SocketConnection class.
 * @author Zoran Mesec
 */
public class MSNProtocol extends Protocol
{
    final public static String CMD_CVR = "CVR";
    final public static String CMD_VER = "VER";
    final public static String CMD_USR = "USR";
    final public static String CMD_CHG = "CHG";
    final public static String CMD_XFR = "XFR";
    final public static String CMD_FLN = "FLN";    
    
    final public static String CMD_ILN = "ILN";
    final public static String CMD_NLN = "NLN";
    final public static String CMD_ADG = "ADG";
    final public static String CMD_CHL = "CHL";
    final public static String CMD_RNG = "RNG";    
    final public static String CMD_OUT = "OUT";
    final public static String CMD_LST = "LST";    
    final public static String CMD_LSG = "LSG";    
    final public static String CMD_MSG = "MSG";        
    
    private String username;
    private String password;
    private boolean connected_;
    private boolean stop;
    
    private MSNTransaction tr;
    private Contact pendingUser = null; //this user is waiting for a chatsession
    private MSNContact movingUser = null;
    private ServerHandler sh;
    private boolean busy = false;
    private Hashtable userHashes; 
    private Hashtable userLists;
    private Hashtable contacts_;
    private Hashtable groupHashes;
    private Hashtable commands;

	public Hashtable getChatIds_() {
		return ChatIds_;
	}
    /**
     * A Vector of all ChatSession classes. Each ChatSession represents a conversation with a contact.
     */
    protected Vector chatSessions_;	//list of active chat sessions
    /**
     * A Vector of all ServerHandler classes that are required for Chatsessions. MSN 
     * Protocol has different IP number for every conversation between two users, 
     * therefore a ServerHandler for each conversation is required.
     */
    protected Vector SessionHandlers_;	//list of IPs of alive chat sessions   
    /**
     * This Hashtable maps Chatsessions and their Transaction IDs. With every message 
     * the transaction ID of Chatsession increases.
     */
    protected Hashtable ChatIds_;
    private Hashtable groupID;
    
    // definitions of constants
    //const of protocol MSNP10
    final String NsURL = "messenger.hotmail.com";
    final String ProductKey = "YMM8C_H7KCQ2S_KL"; 
    final String ProductIDhash = "Q1P7W2E4J9R8U3S5";
    final String ProductID = "msmsgs@msnmsgr.com";
    //final String NSredirectURL = "207.46.114.22";

    final String ProductIDMSNP12 = "PROD0090YUAUV{2B"; //ilya

    private static final long MSNP11_MAGIC_NUM = 0x0E79A9C1;
    final int serverPort = 1863;
    final int NexusPort = 443;    
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
        this.commands = new Hashtable();
        this.commands.put(CMD_ADG,"1");
        this.commands.put(CMD_LSG,"2");
        this.commands.put(CMD_LST,"3");        
        this.commands.put(CMD_CHL,"4");
        this.commands.put(CMD_FLN,"5");
        this.commands.put(CMD_ILN,"6");
        this.commands.put(CMD_NLN,"7");
        this.commands.put(CMD_RNG,"8");
        this.commands.put(CMD_USR,"9");
        this.commands.put(CMD_VER,"10");     
        this.commands.put(CMD_XFR,"11");        
        this.commands.put(CMD_OUT,"12");    
        this.commands.put(CMD_CHG,"13");
        this.commands.put(CMD_CVR,"14");
        
    }
    /**
     * 
     * @see method login(String, String)
     */
    public boolean login(Account acc)
    {
        this.status_ = CONNECTING;
        this.username = acc.getUser();
        this.password = acc.getPassword();          
        try
        {
                this.tr = new MSNTransaction();            
                this.tr.newTransaction();            
                this.sh= new ServerHandler(this.NsURL, this.serverPort);           
                this.sh.connect();
                
                this.tr.setType(CMD_VER);
                this.tr.addArgument("MSNP11");
                this.tr.addArgument("MSNP10");
                this.tr.addArgument("CVR0");
                //String message = "VER 1 MSNP8 CVR0\r\n";
                this.sh.sendRequest(this.tr.toString());
                //System.out.print(this.tr.toString());
                this.sh.getReply();
                /*System.out.println(this.sh.getReply());   
                System.out.println("*************************************");*/
                //message = "CVR 2 0x0409 win 4.10 i386 MSNMSGR 5.0.0544 MSMSGS avgustin.ocepek@yahoo.com.au\r\n"; //MSNP10
                //message = "CVR 2 0x040c winnt 5.1 i386 MSNMSGR 7.0.0777 msmsgs idanilov@ua.fm\r\n"; \\MSNP11
                //message = "CVR 2 0x0409 winnt 5.1 i386 MSNMSGR 7.5.0324 msmsgs idanilov@ua.fm\r\n"; \\MSNP12
                //message = "CVR 2 0x0409 winnt 5.1 i386 MSG80BETA 8.0.0566 msmsgs alice@hotmail.com\r\n"; \\MSNP13
                this.tr.newTransaction();
                this.tr.setType(CMD_CVR);
                this.tr.addArgument("0x040c winnt 5.1 i386 MSNMSGR 7.0.0777 msmsgs"); //MSNP11
                this.tr.addArgument(username);
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                this.sh.getReply();
                //System.out.println(this.sh.getReply());
                //System.out.println("*************************************");  
                
                //message="USR 3 TWN I avgustin.ocepek@yahoo.com.au\r\n";
                this.tr.newTransaction();
                this.tr.setType(CMD_USR);
                this.tr.addArgument("TWN I");
                this.tr.addArgument(this.username);                
                
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                String data = this.sh.getReply();
                /*System.out.println(data);
                System.out.println("*************************************"); */
                String NSredirectURL = data.substring(data.indexOf("NS")+3, data.indexOf(" ", 10));
                //System.out.println("[DEBUG] Redirect URL:"+NSredirectURL);
                this.sh.disconnect();
                
                
                this.sh = new ServerHandler(NSredirectURL);
                try {
                    this.sh.connect();   
                } catch (Exception e) {
                    return false;
                }
 
                this.tr.newTransaction();
                this.tr.setType(CMD_VER);
                this.tr.addArgument("MSNP9");	    // support for version 9
                this.tr.addArgument("MSNP10");	    // and 10
                //this.tr.addArgument("MSNP11");    //  only in v2 :)
                this.tr.addArgument("CVR0");
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                this.sh.getReply();  
                //parseReply(this.sh.getReply());    
                
                this.tr.newTransaction();
                this.tr.setType(CMD_CVR);
                this.tr.addArgument("0x0409 win 5.1 i386 MSNMSGR 7.0.0777 msmsgs");
                this.tr.addArgument(this.username);
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());   
                this.sh.getReply();                 
                //parseReply(this.sh.getReply());         
                
                this.tr.newTransaction();
                this.tr.setType(CMD_USR);
                this.tr.addArgument("TWN I");             
                this.tr.addArgument(this.username); 
                this.sh.sendRequest(this.tr.toString());
                //System.out.println(this.tr.toString());  
                String USRreply = this.sh.getReply();
                String challenge = USRreply.substring(12);
                
                //this is where the password stuff fun starts
                                
                // We have to establish a connection MSN Passport network 
                // for authentification with user password.
                //Warning: connection to the NS should remain open during the communication with the PassportNexus                                
                
                /* Ilya: old Passport 1.4
                PassportNexus pn = new PassportNexus();
                pn.getPassportLoginServer();
                String ticket = pn.requestAuthorizationTicket(this.username, this.password, challenge);
                 */

                //Ilya: use of latest SOAP XML based Passport 3.0
                //its required for S40 handsets which has a bug with header length and due this unable to use Passport 1.4
                PassportLoginNet pn = new PassportLoginNet();
                String ticket = pn.requestAuthorizationTicket(this.username, this.password, challenge);
                //String ticket = pn.GetIt(this.username, this.password, challenge);
                
                if(ticket == null)
                { 
                    this.status_ = WRONG_PASSWORD;
                    return false;
                }
                
                this.tr.newTransaction();    
                this.tr.setType(CMD_USR);
                this.tr.addArgument("TWN S");
                this.tr.addArgument(ticket);
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                this.sh.getReply();    //gets the SBS or anything else that is not important            

                this.tr.newTransaction();
                this.tr.setType("SYN");
                this.tr.addArgument("2007-08-14T06:03:32.863-07:00 2006-08-16T06:03:33.177-07:00");                  
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());  
                parseReply(this.sh.getReply());
             
                this.tr.newTransaction();
                this.tr.setType(CMD_CHG);
                this.tr.addArgument(CMD_NLN);
                //this.tr.addArgument("HDN"); //log in as anavailable(for testing)
                this.tr.addArgument("0");
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());                           
                parseReply(this.sh.getReply());
                
                this.status_ = CONNECTED;
		this.stop = false;
                this.thread_.start();
                return true;
        }
        catch (Exception e)
        {
            this.status_ = NO_CONNECTION;
            e.printStackTrace();
            return false;
        }
    }    
    
    /**
     * method disconnects the SocketConnection of this class.
     */
    public void logout()
    {
	    this.status_ = DISCONNECTED;
            System.out.println("[DEBUG] Logging out.");         
            this.sh.sendRequest(this.tr.getLogoutString());

	    this.stop = true;
            System.out.println("[DEBUG] Logout successful.");
	    //this.thread_.interrupt();
    }
    /**
     * Parses a reply from the server according to first three letters.
     * @param reply raw data from the server, presented as a string
     */
    public void parseReply(String reply) {  
	System.out.println("Reply:"+reply);
        if(reply == null) {
            return;
        }
        /*if(reply.substring(0,3).compareTo("OUT")==0) {
            this.connected_=false;
            this.status_=DISCONNECTED;
            this.logout();  
        } */         
        
        /*if(reply.indexOf(CMD_XFR)!=-1) {
            startSession(reply);
        } */           
        /*if(reply.indexOf("FLN")!=-1) {
            userGoesOffline(reply);
        } */          
        /*if(reply.indexOf("ILN")!=-1) {
            parsePresence(reply); 
        }    */     
        /*if(reply.indexOf("NLN")!=-1) {
            changePresence(reply);
        }  */   
        /*if(reply.indexOf("ADG")!=-1) {
            groupAdded(reply);
        }  */       
        /*if(reply.indexOf("CHL")!=-1) {    
            parseChallenge(reply);
            System.gc();    // let's clean this mess
        } */    
        /*if(reply.indexOf("RNG")!=-1) {
            SBhandle(reply);
        }*/
        
        int ind = reply.indexOf("\r\n");
        String cmd;
        int cmd_id;
        while(ind!=-1)
        {
            /*System.out.println("CMD:"+one_line.substring(0,3));*/
            if (reply.length()>=3){
                cmd = (String)this.commands.get(reply.substring(0,3));
            } else {
                cmd = null;
            }
            
            cmd_id = (cmd!=null) ?   Integer.parseInt(cmd) : 0;
            switch (cmd_id)
            {
                case 1: //ADG
                    groupAdded(reply.substring(0,ind+2));
                    break;
                
                case 2: //LSG
                    parseGroups(reply.substring(0,ind+2));
                    break;
                case 3: //LST
                    parseContacts(reply.substring(0,ind+2));
                    break;
                case 4: //CHL
                    parseChallenge(reply.substring(0,ind+2));
                    System.gc();    // let's clean this mess                          
                    break;
                case 5: //FLN
                    userGoesOffline(reply.substring(0,ind+2));
                    break;
                case 6: //ILN
                    parsePresence(reply.substring(0,ind+2)); 
                    break; 
                case 7: //NLN
                    changePresence(reply.substring(0,ind+2));
                    break;     
                case 8: //RNG
                    SBhandle(reply);
                    break;                       
                case 11: //XFR
                    startSession(reply.substring(0,ind+2));
                    break;
                case 12: //OUT
                    this.connected_=false;
                    this.status_=DISCONNECTED;
                    this.logout(); 
                    break;
                default:
                    //unknown command
                    break;
            }
            //ensure we can use substring()
            if ((ind+2) < reply.length()){
                reply = reply.substring(ind+2);
            } else {
                reply = "";
            }
            //System.out.println("Reply Cut:" + reply);
            ind = reply.indexOf("\r\n");
        }
                
    }
    private void parseMessage(String data, int SHid)
    {
        // there can be a lot of different structures of MSG incoming string
        // therefore a complex structure of this function
        int t = data.indexOf(CMD_MSG);
        int end;
        String msg;
        while(t!=-1) {
            end = data.indexOf(CMD_MSG, t+1);
            if(end == -1) {   
                // we have reached the last MSG piece in the string
                msg = data.substring(t);
                //System.out.println("************Last msg:\n" + msg);
                //System.out.println("***************");
                t = -1;
                if(msg.indexOf("TypingUser:")==-1 && msg.indexOf("Content-Type: text")!=-1) {
		    ChatSession activeCS = (ChatSession)this.chatSessions_.elementAt(SHid);

		    Vector allContacts = activeCS.getContactsList();
		    String uID =  msg.substring(4,msg.indexOf(" ", 10));
		    //System.out.println("[DEBUG] Receiving message from:" + uID);
		    Contact c = null;
		    for(int i=0;i<allContacts.size(); i++) {
			c = (Contact)allContacts.elementAt(i);
			if(uID.compareTo(c.userID())==0)
			{
			    break;
			}
		    }
		    int marker = msg.indexOf("\r\n\r\n");
		    if(marker==-1)
		    {
			t = end;
			continue;
		    }
		    //System.out.println("[DEBUG] Message:"+ marker + msg.substring(msg.indexOf("\n",marker+4)+3));
		    //String message = msg.substring(msg.indexOf("\n",marker+4)+3);
                    String message = msg.substring(marker+4);
		    this.jimmy_.msgRecieved(activeCS, c, message);	    
                 }                 
            }
            else
            {
		// there are more MSG strings in parameter string
                msg = data.substring(t,end);                
                if(msg.indexOf("TypingUser:")==-1)
               {
                    ChatSession activeCS = (ChatSession)this.chatSessions_.elementAt(SHid);

                    Vector allContacts = activeCS.getContactsList();
                    String uID =  msg.substring(4,msg.indexOf(" ", 10));
                    Contact c = null;
                    for(int i=0;i<allContacts.size(); i++)
                    {
                        c = (Contact)allContacts.elementAt(i);
                        if(uID.compareTo(c.userID())==0)
                        {
                            break;
                        }
                    }
                    int marker = msg.indexOf("X-MMS-IM-Format:");
                    if(marker==-1)
                    {
                        t = end;
                        continue;
                    }
                    String message = msg.substring(msg.indexOf("\n",marker+4)+3);
                    this.jimmy_.msgRecieved(activeCS, c, message);
                }                    
                t = end;
            }
	}
    }
    private void parseADC(String data)
    {
        //ADC 0 RL N=odar_5ra@hotmail.com F=Kalypso\r\n
        System.out.println("parseADC:" + data);
        
        String line = data.substring(data.indexOf("ADC"), data.indexOf('\n')+1);
        System.out.println("Line:" + line);

        //line = ADC 0 RL N=odar_5ra@hotmail.com F=Kalypso
        String[] atoms = Utils.tokenize(line);
        //atoms[0] = ADC
        //atoms[1] = cid
        //atoms[2] = FL, AL, RL, BL
        //atoms[2] = N=username
        //atoms[3] = F=full name
        
        if(atoms[2].compareTo("RL")==0)
        {
            String userID = line.substring(11,data.indexOf("F=")-1);
            System.out.println(userID);
            Contact c = new Contact(userID, this);
            System.out.println(line.substring(line.indexOf("F=")+2, line.length()-2));
            c.setScreenName(line.substring(line.indexOf("F=")+2, line.length()-2));
            
            if(this.contacts_==null)
            {
                this.contacts_ = new Hashtable();
            }
            
            this.addContact(c);
            jimmy_.addContact(c);
        }
    }
    private void userBYE(String data, int SHid)
    {
        //BYE zoran.mesec@siol.net

        String uID = data.substring(3);
        System.out.println(uID);
    	ChatSession activeCS = (ChatSession)this.chatSessions_.elementAt(SHid);
        Contact c = null;
        Vector allContacts = activeCS.getContactsList();
        for(int i=0;i<allContacts.size(); i++)
        {
            c = (Contact)allContacts.elementAt(i);
            if(uID.compareTo(c.userID())==0)
            {
                break;
            }
        }
        this.jimmy_.msgRecieved(activeCS, c, c.screenName() + " has closed the conversation window.");
    }
    private void groupAdded(String data)
    {
        //ADG 15 New%20Group b145b37f-7e09-47e7-880e-9daa5346eaab
        
    }
    private void parseContacts(String data)
    {  
        /**
         * TODO: rewrite this with the new string tokenizer in Utils
         */
        //Normal form:
        //LST N=matevz.jekovec@guest.arnes.si F=Matevz C=d954638f-1963-4e45-b157-2029eae8714f 3 406c6d87-043d-4f20-b569-0450b49ca65d
        //Someone got a reply like this once(repeatedly):
        //LST N=p
        if(this.contacts_ == null) {
            this.contacts_ = new Hashtable();
        }
        int ind;
        MSNContact person; 
        String username;           

        ind = data.indexOf("N=");   
        int ind2 = data.indexOf(" ", ind+1);
        if(ind==-1 || ind2==-1){
            return;
        }            
        username = data.substring(ind+2, ind2);
        person = new MSNContact(username, (Protocol)this);

        ind = data.indexOf("F=");   
        ind2 = data.indexOf(" ", ind+1);
        if(ind==-1 || ind2==-1){
            person.setScreenName(username);
        } else {
            String screenname = data.substring(ind+2,ind2);
            System.out.println("Screen name:"+screenname);
            person.setScreenName(Utils.urlDecode(screenname));
        }
        
        ind = data.indexOf("C=");
        if(ind==-1) {
            return;
        }
        ind2 = data.indexOf(" ", ind);
        String contactHash = data.substring(ind+2, ind2);   
        //System.out.println("UserHash:" + contactHash);
        person.setUserHash(contactHash);

        ind = data.indexOf(" ", ind2+1);
        String lists;
        if(ind ==-1){
            //user does not belong to a group
            lists = data.substring(ind2+1, data.length()-2);
        } else {   
            lists = data.substring(ind2+1, ind);
            username = data.substring(ind+1, data.length()-2);
            person.setGroupHash(username);
            person.setGroupName((String)this.groupID.get(username));   
        }
        short list = Short.parseShort(lists);
        person.setLists(list);
        if(this.userHashes==null) {
            this.userHashes = new Hashtable(); }
        if(this.userLists==null) {
            this.userLists = new Hashtable(); }	   
        this.userHashes.put(new Integer(person.hashCode()), contactHash);
        this.userLists.put(new Integer(person.hashCode()), lists);            
        this.contacts_.put(person.userID(), person); 
        
        //the forward list is 1, the allow list is 2, the block list is 4, 
        //the reverse list is 8 and the pending list 16.
        
        switch (list) {
            case 1: //user in my FL list, but i'm not in his/her FL list
                break;
            case 2:
            case 3:
            case 9: //user in my FL list only, i'm in his FL and AL. Means he can't see my presence, so add user to AL list
                this.tr.newTransaction();
                this.tr.setType("ADC");
                this.tr.addArgument("AL N="+person.userID());
                System.out.println(this.tr.toString());
                this.sh.sendRequest(this.tr.toString());      
                break;
            case 11: //user in my FL and AL. Means he can see my presence
            case 17:
              break;
            default:
                break;
        }
        System.out.println("Adding contact:" + person.userID()+ " " + person.groupName()+ " " +person.screenName());

        jimmy_.addContact(person);
    }
    private void userGoesOffline(String data)
    {
        //FLN matevz.jekovec@guest.arnes.si
        String uID = data.substring(4, data.length()-2);
        //System.out.println("[DEBUG] User to go ofline:"+uID);
        MSNContact c = null;
        c = (MSNContact)this.contacts_.get(uID);
        if(c==null)
        {
            return;
        }
        /*for(int i=0; i<this.contacts_.size();i++)
        {
            c = (Contact)this.contacts_.elementAt(i);
            if(uID.compareTo(c.userID())==0)
            {
                 break;
            }
        }*/
        c.setStatus(Contact.ST_OFFLINE);
        this.jimmy_.changeContactStatus(c);
    }
    private void changePresence(String data)
    {
	if(data.length()<20 || data.substring(0,3).compareTo("NLN")!=0)
	{
	    return;
	}
	String presence = data.substring(4, 7);
	String uID = data.substring(8, data.indexOf(" ", 10));
	System.out.println("[DEBUG] Data:"+data+"Presence:"+presence+", Uid:"+uID);
	MSNContact con = (MSNContact)this.contacts_.get(uID);

	 if(con!=null)
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
	    else
	    {
		con.setStatus(Contact.ST_ONLINE);
	    }
	    this.jimmy_.changeContactStatus(con);
	}	
    }
    private void startSession(String line)
    {
	ServerHandler switchHandler;
        //System.out.println("[DEBUG] Server RNG ip:"+line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6));
        switchHandler = new ServerHandler(line.substring(line.indexOf("SB")+3, line.indexOf("CKI")-6),1863);
        switchHandler.connect(); 
        switchHandler.sendRequest("USR 1 " + this.username + " " + line.substring(line.indexOf("CKI")+4, line.indexOf("\r")) + "\r\n");
	switchHandler.sendRequest("CAL 2 "+this.pendingUser.userID()+"\r\n");
	this.pendingUser = null;
	this.SessionHandlers_.addElement(switchHandler);
    }
    private void parseGroups(String data)
    {
        if(this.groupID == null)
        {
            this.groupID = new Hashtable();
        }    
        if(this.groupHashes == null)
        {
            this.groupHashes = new Hashtable();
        }           
        int i = 4;
        char c;
        StringBuffer group;
        StringBuffer groupID;    
        group = new StringBuffer();
        while((c=data.charAt(i))!= ' ')
        {
            group.append(c);
            i++;
        } 
        i++;
        groupID = new StringBuffer();
        while((c=data.charAt(i))!= '\n')
        {
            groupID.append(c);
            i++;
        }
        //System.out.println("First:"+groupID.toString().substring(0, groupID.length()-1)+", \nsecond:"+group.toString());
        //this.groupID.put(group.toString(), new Integer(groupID.toString().substring(0, groupID.length()-1).hashCode()));
        this.groupID.put(groupID.toString().substring(0, groupID.length()-1), group.toString());
        this.groupHashes.put(group.toString(), groupID.toString().substring(0, groupID.length()-1));
    }
    
    private void parsePresence(String data)
    {
         //data = ILN 11 NLN idanilov@ua.fm Ilya%20Danilov 1342177280
         System.out.println("[DEBUG] parsePresence:data:" + data);
         //this.printContacts();
         String[] atoms = Utils.tokenize(data);
         //atoms[0] = ILN or NLN
         //atoms[1] = cid number
         //atoms[2] = status
         //atoms[3] = contact name (email)
         //atoms[4] = url decoded screen name
         //atoms[5] = contact capabilities (see: http://www.hypothetic.org/docs/msn/notification/presence.php)
         String presence = atoms[2];
         MSNContact con;
         System.out.println("[DEBUG] type:" + presence);
         //System.out.println("[DEBUG] user:" + con.uID);
         con = (MSNContact)this.contacts_.get(atoms[3]);
         System.out.println("[DEBUG] con:" + con);
         if(con!=null)
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
            else
            {
                con.setStatus(Contact.ST_ONLINE);
            } 
            this.jimmy_.changeContactStatus((Contact)con);
         }  
    }
    
    private void parseChallenge(String data)
    {       
            String line = data.substring(data.indexOf("CHL"), data.indexOf('\r')); //remove \r\n
        
            //data = CHL 0 25270234921473318824
            String[] atoms = Utils.tokenize(line);
            //atoms[0] = CHL
            //atoms[1] = ?
            //atoms[2] = challenge
            String challenge = atoms[2];
            System.out.println(challenge);
            
        
            // first step for v11
            //StringBuffer challenge =new StringBuffer(data.substring(6,data.length()-2));
            //challenge.append(this.ProductKey);
            //String challenge = data.substring(6,26);
            //System.out.println(challenge);
            
            MD5 md5 = new MD5();
            
	    // v11 challenge
            //String hash = new String(md5.toHex(md5.fingerprint(challenge.toString().getBytes())));
            //System.out.println(hash);
            //challenge.concat("YMM8C_H7KCQ2S_KL");
            //challenge = "15570131571988941333";
            
            challenge+=this.ProductIDhash;
            System.out.println(challenge);
            String hash = new String(md5.toHex(md5.fingerprint(challenge.getBytes())));
            this.tr.newTransaction();
            this.tr.setType("QRY");
            this.tr.addArgument(this.ProductID);
            this.tr.addArgument("32\r\n"+hash);
            System.out.println(this.tr.toStringNN());            
            this.sh.sendRequest(this.tr.toStringNN());            
    }  
    private void SBhandle(String data)  //Switchboard handle
    {
	//RNG 1083693517 207.46.26.110:1863 CKI 18321026.739392 zoran.mesec@siol.net Zoran%20Mesec
	//ANS 1 name_123@hotmail.com 18321026.739392 11752013\r\n
        System.out.println("SB handle function **************");
        String authNr = data.substring(data.indexOf(" ")+1, data.indexOf(" ", 7));
        //System.out.println("Number:"+authNr);
        String sbIP = data.substring(data.indexOf(" ", 7)+1,data.indexOf("CKI")-1);
        //System.out.println("Switchboard IP:"+sbIP);
        String authKey = data.substring(data.indexOf("CKI")+4, data.indexOf(" ", data.indexOf("CKI")+5));
        //System.out.println(authKey);
        MSNTransaction answer = new MSNTransaction();
        answer.newTransaction();    //set id to 1
        answer.setType("ANS");
        answer.addArgument(this.username);
        answer.addArgument(authKey);
        answer.addArgument(authNr);
        
        //System.out.println("Transaction:" + answer.toString());
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
        this.SessionHandlers_.addElement(sbHandler);
        
        int t = data.indexOf(" ", data.indexOf("CKI")+5);
        String uID = data.substring(t+1, data.indexOf(" ", t+1));
        //System.out.println("[DEBUG] Calling user:" + uID + this.contacts_.size());
        
        MSNContact c = (MSNContact)this.contacts_.get(uID);
        
        if(c!=null)
        {
            ChatSession cs = new ChatSession(this, c);
            this.ChatIds_.put(new Integer(cs.hashCode()), new Integer(2));
            this.chatSessions_.addElement(cs);
            System.out.println("[DEBUG] End of SBhandle");            
        }
        else
        {
            System.out.println("Invalid user!");
            return;
        }
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
    /**
     * yy
     * @deprecated This method does nothing.
     */
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
           MSNContact c;
            for(Enumeration e = this.contacts_.elements();e.hasMoreElements();)
            {
               c =(MSNContact) e.nextElement();
                System.out.println("Contact:"+c.userID()+", screen:" + c.screenName() + ", group:" + c.groupName() + ", status:" + c.status());
                System.out.println("\tGroup hash:"+c.getGroupHash()+", lists:" + c.getLists()+", userHash:"+c.getUserHash());
            }
        }  
       System.out.println("*********************************************************");
       System.out.println("END OF CONTACT LISTING");
       System.out.println("*********************************************************");        
   }  
/*   void printGroups() 
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
        }
       System.out.println("*********************************************************");
       System.out.println("END OF GROUP LISTING");
       System.out.println("*********************************************************");           
   }*/ 
    /**
     * returns Vector of all active ChatSesisons.
     * @return returns Vector of all active ChatSesisons.
     */
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
        ChatSession cs = new ChatSession(this, c);
        this.busy = true;
        this.tr.newTransaction();
        this.tr.setType("XFR");
        this.tr.addArgument("SB");
        this.sh.sendRequest(this.tr.toString());
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
        int Sid;    // session ID
        Integer id;
        for(int i=0; i<this.chatSessions_.size();i++)
        {
            if(this.chatSessions_.elementAt(i).equals(session))
            {
		//System.out.println("[DEBUG] Inside:"+i+"size:"+this.SessionHandlers_.size());
                ServerHandler sh = (ServerHandler) this.SessionHandlers_.elementAt(i);
                String payload = "MIME-Version: 1.0\r\nContent-Type: text/x-msmsgscontrol\r\nTypingUser: "+this.username+"\r\n\r\n\r\n";
                id = (Integer)this.ChatIds_.get(new Integer(session.hashCode()));
                Sid = id.intValue();
                sh.sendRequest("MSG " + Sid + " U "+payload.length() + "\r\n"+payload);              
                System.out.println("[DEBUG] MSG "+ Sid + " U "+payload.length() + "\r\n"+payload);
                Sid++;
                
                payload = "MIME-Version: 1.0\r\nContent-Type: text/plain; charset=UTF-8\r\nX-MMS-IM-Format: FN=MS%20Sans%20Serif; EF=; CO=0; CS=0; PF=0\r\n\r\n";                                      
                sh.sendRequest("MSG "+Sid+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                Sid++;
                System.out.println("[DEBUG] MSG "+Sid+" U "+(payload.length()+msg.length())+"\r\n"+payload+msg);
                this.ChatIds_.put(new Integer(session.hashCode()), new Integer(Sid));
                return;
            }
        }    
    }
    /**
     * This method is implemented by convenience.
     * @param msg Message in String
     * @param user Specific user this message should be sent to
     * @param session Active Chat Session to send the message to
     */
    public void sendMsg(String msg, Vector users, ChatSession session)
    {  
    }
    /**
     * returns a Vector of contacts(class Contact).
     * @return Vector of contacts(class Contact).
     */
    public Vector getContacts() { return null; }
    
    /**
     * This method is called when Thread starts. It is a infinite loop that checks all
     * ServerHandlers whether any message has arrived from servers(that you are 
     * currently connected to).
     */
    public void run() 
    {
        while(!stop)
        {
            try 
            {
                Thread.sleep(1000L);
            } catch (InterruptedException e) 
            {
		// TODO Auto-generated catch block
		e.printStackTrace();
            }  
            if(this.SessionHandlers_==null)
            {
                this.SessionHandlers_=new Vector();
            }
            String reply = null;
            for(int i=0; i<this.SessionHandlers_.size();i++)
            {
                ServerHandler shTemp = (ServerHandler)this.SessionHandlers_.elementAt(i);
                reply = null;
                reply = shTemp.getReply();
                //System.out.println("[DEBUG] Server handler"+reply);
                if(reply!=null && this.status_==CONNECTED)
                {
                    if(reply.indexOf("BYE")!=-1)
                    {
                        userBYE(reply, i);
                    }                    
                    if(reply.indexOf(CMD_MSG)!=-1)
                    {
                        parseMessage(reply, i);
                    }
                }
            }            
            if(this.status_ != CONNECTED) 
            {
                continue;
            }
            parseReply(this.sh.getReply());
        }  
	this.connected_ = false;
        System.out.println("End of run() method!");
    }
    /**
     * Removes a contact from all lists.
     * @param Contact c the contact to be removed
     * @return true for success or false if the Contact is not on local list of Contacts
     */
    public boolean removeContact(Contact c)
    {
        System.out.println("Remove contact");
	MSNContact con=(MSNContact)this.contacts_.get(c.userID());

	     if(con!=null)
	     {
		 short list = con.getLists();
		 System.out.println("LisT:" + list);
                 this.tr.newTransaction();
                 this.tr.setType("REM");			 
                 this.tr.addArgument("FL "+this.userHashes.get(new Integer(con.hashCode())));
                 System.out.println(this.tr.toString());
                 this.sh.sendRequest(this.tr.toString());
		 switch (list)
		 {
		     case 11: //user also in my AL list, so don't forget to remove him from this list also
			 this.tr.newTransaction();
			 this.tr.setType("REM");	
			 this.tr.addArgument("AL "+c.userID());
			 System.out.println(this.tr.toString());
			 this.sh.sendRequest(this.tr.toString());
		     case 1:
                     case 3: //???
			 this.tr.newTransaction();
			 this.tr.setType("REM");			 
			 this.tr.addArgument("FL "+this.userHashes.get(new Integer(con.hashCode())));
			 this.sh.sendRequest(this.tr.toString());
			 System.out.println(this.tr.toString());
			 break; 
		 }
				 
		 this.contacts_.remove(c.userID());
		 return true;
	     }
	return false;
    }
    /**
     * This method adds a contact to the local list and then sends a message to the 
     * MSN server in order to add a contact to the main list on the server.
     * @param Contact c The contact to be removed.
     */
    public void addContact(Contact c)
    {
	//ADC 16 FL N=passport@hotmail.com F=Display%20Name\r\n
        //ADC 16 AL N=passport@hotmail.com F=Display%20Name\r\n
        String name = Utils.urlDecode(c.screenName());
        System.out.println(name);
        c.setScreenName(name);
        
        MSNContact ms = new MSNContact(c.userID(), this);
        ms.setScreenName(c.screenName());
        System.out.println("1");
        
	if(this.contacts_==null)
	{
	    this.contacts_=new Hashtable();
	}
            System.out.println("2");        
	    this.contacts_.put(ms.userID(), ms);
            
            System.out.println("3");
	    this.tr.newTransaction();
	    this.tr.setType("ADC");
	    this.tr.addArgument("FL N="+c.userID() + " " + "F="+c.screenName());
	    System.out.println(this.tr.toString());
	    this.sh.sendRequest(this.tr.toString());   
            
            System.out.println("4");
	    /*this.tr.newTransaction();
	    this.tr.setType("ADC");
	    this.tr.addArgument("AL N="+c.userID() + " " + "F="+c.screenName());
	    //this.tr.addArgument("FL N="+c.userID() + " " + "F="+c.screenName());
	    System.out.println(this.tr.toString());
	    this.sh.sendRequest(this.tr.toString());      
            */
            /*//ILYA: allow user to see our status
            this.tr.newTransaction();
            this.tr.setType("ADD");
            this.tr.addArgument("AL "+c.userID().toString()+" "+c.screenName());
            System.out.println(this.tr.toString());
            this.sh.sendRequest(this.tr.toString());*/
            
            System.out.println("Konec metode add contact");
 
            //this.updateContactProperties(c);
	    /*if(c.groupName()!=null)
	    {
		this.tr.newTransaction();
		this.tr.setType("ADC");
		this.tr.addArgument("FL C=" + this.userHashes.get(new Integer(c.hashCode())));
		this.tr.addArgument(" "+this.groupHashes.get(new Integer(c.groupName().hashCode())));
		System.out.println("ADD"+this.tr.toString());
		this.sh.sendRequest(this.tr.toString());
	    }*/
    }
    public void updateContactProperties(Contact c) 
    {
        MSNContact con = (MSNContact)c;
        if(c.groupName()==null)
        {
            return;
        }
        String groupName = "";
        if(con.getGroupHash()!=null)
        {
            groupName = (String)this.groupID.get(con.getGroupHash());    
        }

        if(groupName.compareTo(c.groupName())!=0)
        {
            String gHash=null;
            boolean groupExists = false;
            System.out.println("Changing group!");
            for(Enumeration e = this.groupID.keys();e.hasMoreElements();)
            {
                gHash = (String)e.nextElement();
                System.out.println(gHash);
                groupName = (String)this.groupID.get(gHash);
                if(groupName.compareTo(c.groupName())==0)
                {
                    System.out.println("Found match!!!");
                    groupExists = true;
                    break;
                }
            }
            if(groupExists)
            {
                
                System.out.println("Group exists!!!");
                //move a contact to an existing group and delete him from the old one(if any)
                
                //ADC 47 FL C=ec90b44e-eac3-495e-b09f-19579793dbdb 5cb95ee2-b678-47e3-8eb1-ccfaa7963d73
                //REM 48 FL ec90b44e-eac3-495e-b09f-19579793dbdb 3c9b3456-a8fa-4164-beb5-68bea34365bd
                
                //add the contact to an existing group
                this.tr.newTransaction();
                this.tr.setType("ADC");
                this.tr.addArgument("FL");
                this.tr.addArgument("C="+con.getUserHash());
                this.tr.addArgument(gHash);
                this.sh.sendRequest(this.tr.toString());
                System.out.println(this.tr.toString());     
                
                //delete the contact from the old group
                if(con.getGroupHash()!=null)
                {
                    System.out.println("Removing from the old group!!!");
                    this.tr.newTransaction();
                    this.tr.setType("REM");
                    this.tr.addArgument("FL");
                    this.tr.addArgument(con.getUserHash());
                    this.tr.addArgument(con.getGroupHash());
                    this.sh.sendRequest(this.tr.toString());
                    System.out.println(this.tr.toString()); 
                }
                
                //set hash of the new group locally
                con.setGroupHash(gHash);
            }
            else
            {
                System.out.println("This is a new group!!!");
                this.movingUser = (MSNContact)c;
                //addGroup(c.groupName());                
            }
        }
        else
        {
            System.out.println("No group change!!!");
            //only screen name of the contact has changed
            //this can not be implemented in MSN protocol
        }
        
        
        //ADC 89 FL C=e527fe06-4208-40cf-a8c9-d6ba636f4021 d788c190-591d-44f8-a304-ff365a41f3ce
        /*Contact con=null;
        String oldGroupID=null;
        for(int i=0; i<this.contacts_.size(); i++)
	{
	     con = (Contact)this.contacts_.elementAt(i);
	     if(con.userID().compareTo(c.userID())==0)
	     {     
                 oldGroupID = con.groupName();
                 this.contacts_.insertElementAt(c, i);
                 break;
             }       
        }        
        System.out.println("Update contact properties!");
        String gHash;
        if(c.groupName()==null)
        {
            gHash =null;
        }
        else
        {
            gHash = (String)this.groupHashes.get(new Integer(c.groupName().hashCode()));
        }
        System.out.println("ghash" + gHash+"old group:" + oldGroupID);
        if(gHash == null)
        {
            //ADG 169 New%20Group
            this.tr.newTransaction();
            this.tr.setType("ADG");
            this.tr.addArgument(c.groupName());
            this.sh.sendRequest(this.tr.toString());
            this.movingUser = c;
        }
        else
        {
            System.out.println("We have found a match!!!");
            
            //REM 174 FL e527fe06-4208-40cf-a8c9-d6ba636f4021 3c9b3456-a8fa-4164-beb5-68bea34365bd
            if(oldGroupID==null)
            {
                System.out.println("oldGroupID==null");
                this.tr.newTransaction();
                this.tr.setType("REM");
                this.tr.addArgument("FL");
                this.tr.addArgument((String)this.userHashes.get(new Integer(c.hashCode())));
                this.tr.addArgument((String)this.groupHashes.get(new Integer(oldGroupID.hashCode())));
                System.out.println(this.tr.toString());
                this.sh.sendRequest(this.tr.toString());      
            }
            //ADC 215 FL C=e91bf740-50b2-4df6-aa45-ec35e26230d6 406c6d87-043d-4f20-b569-0450b49ca65d
            this.tr.newTransaction();
            this.tr.setType("ADC");
            this.tr.addArgument("FL");
            this.tr.addArgument("C="+this.userHashes.get(new Integer(c.hashCode())));
            this.tr.addArgument(gHash);
            System.out.println(this.tr.toString());
            this.sh.sendRequest(this.tr.toString());            
        }
        
        
        //this.tr.newTransaction();
  
        String gID;

            /*gID = (String)this.groups.elementAt(i);
            System.out.println(gID);
            if(gID.compareTo(c.groupName())==0)
            {
                System.out.println("We have found a match!!!");
                this.tr.setType("ADC");
                this.tr.addArgument("FL");
                this.tr.addArgument("C="+this.userHashes.get(new Integer(c.hashCode())));
                this.tr.addArgument((String)this.groupHashes.elementAt(i));
                System.out.println(this.tr.toString());
                this.sh.sendRequest(this.tr.toString());
                return;
            }*
        /*this.pendingUser = null;
	this.tr.setType("ADG");
	this.tr.addArgument("N="+c.userID());
	this.tr.addArgument("F="+c.screenName());     
	//this.sh.sendRequest(this.tr.toString());
        System.out.println(this.tr.toString());*/
    }
    public void addGroup(String groupName)
    {
        System.out.println("Add group method! Name of the new group:" + groupName);
        //ADG 15 New%20Group
        this.tr.newTransaction();
        this.tr.setType("ADG");
        this.tr.addArgument(groupName.replace(' ', '_'));
        this.sh.sendRequest(this.tr.toString());
    }    
}
