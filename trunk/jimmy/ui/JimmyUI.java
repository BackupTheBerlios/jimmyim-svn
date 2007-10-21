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
 File: jimmy/JimmyUI.java

 Author(s): Matevz Jekovec, Janez Urevc
*/

package jimmy.ui;

import javax.microedition.lcdui.*;
import java.util.*;
import java.lang.*;

import jimmy.Account;
import jimmy.*;
import jimmy.util.Store;
import jimmy.icq.*;
import jimmy.msn.*;
import jimmy.jabber.*;
import jimmy.ui.Localization;

public class JimmyUI {
	//Commands codes:
	final public static int CMD_OK     = 1;
	final public static int CMD_CANCEL = 2;
	final public static int CMD_YES    = 3;
	final public static int CMD_NO     = 4;
	final public static int CMD_FIND   = 5;
	final public static int CMD_BACK   = 6;
	final public static int CMD_EXIT   = 7;
	final public static int CMD_LOGIN  = 8;
	final public static int CMD_NEW    = 9;
	final public static int CMD_ABOUT  = 10;
	final public static int CMD_CHAT   = 11;
        final public static int CMD_SEND   = 12;
        final public static int CMD_DEL    = 13;
        final public static int CMD_EDIT   = 14;
        final public static int CMD_ACC    = 15;
        final public static int CMD_NEWCONT= 16;
        final public static int CMD_DELCONT= 17;
	final public static int CMD_LOGOUT = 18;
        
	//Screen codes
	final public static int SCR_SPLASH  = 1;
	final public static int SCR_MAIN    = 2;
	final public static int SCR_NEWACC  = 3;
	final public static int SCR_CHAT    = 4;
	final public static int SCR_ABOUT   = 5;
	final public static int SCR_CONT    = 6;
        

	//Commands:
	final private static Command cmdOk     = new Command(Localization.tr("OK"),          Command.OK,     1);//[LOCALE]
        final private static Command cmdSend   = new Command(Localization.tr("Send"),        Command.OK,     1);//[LOCALE]
	final private static Command cmdCancel = new Command(Localization.tr("Cancel"),      Command.BACK,   2);//[LOCALE]
	final private static Command cmdYes    = new Command(Localization.tr("Yes"),         Command.OK,     1);//[LOCALE]
	final private static Command cmdNo     = new Command(Localization.tr("No"),          Command.CANCEL, 2);//[LOCALE]
	final private static Command cmdFind   = new Command(Localization.tr("Find"),        Command.OK,     1);//[LOCALE]
	final private static Command cmdBack   = new Command(Localization.tr("Back"),        Command.BACK,   2);//[LOCALE]
	final private static Command cmdExit   = new Command(Localization.tr("Exit"),        Command.EXIT,   3);//[LOCALE]
	final private static Command cmdLogin  = new Command(Localization.tr("Login"),       Command.ITEM,   1);//[LOCALE]
	final private static Command cmdNew    = new Command(Localization.tr("New"),         Command.ITEM,   1);//[LOCALE]
	final private static Command cmdAbout  = new Command(Localization.tr("About"),       Command.ITEM,   1);//[LOCALE]
	final private static Command cmdChat   = new Command(Localization.tr("Chat"),        Command.ITEM,   1);//[LOCALE]
        final private static Command cmdDel    = new Command(Localization.tr("Remove"),      Command.ITEM,   1);//[LOCALE]
        final private static Command cmdEdit   = new Command(Localization.tr("Edit"),        Command.ITEM,   1);//[LOCALE]
        final private static Command cmdAccount= new Command(Localization.tr("Accounts"),    Command.ITEM,   1);//[LOCALE]
        final private static Command cmdNewCont= new Command(Localization.tr("Add contact"), Command.ITEM,   1);//[LOCALE]
        final private static Command cmdDelCont= new Command(Localization.tr("Remove contact"),Command.ITEM,   1);//[LOCALE]
	final private static Command cmdLogout = new Command(Localization.tr("Logout"),	    Command.ITEM,   1);//[LOCALE]
	
	static private Hashtable commands_ = new Hashtable();   //commands list
	static private Displayable lastDisplayable_;            //displayable object
	static private JimmyUI jimmyUI_;                        //JimmyUI object
	static private Jimmy jimmy_;
	//static private Vector acc_;                          //List of accounts
	
//	 Associate commands and commands codes
	static
	{
		commands_.put(new Integer(CMD_OK),      cmdOk       );
		commands_.put(new Integer(CMD_CANCEL),  cmdCancel   );
		commands_.put(new Integer(CMD_YES),     cmdYes      );
		commands_.put(new Integer(CMD_NO),      cmdNo       );
		commands_.put(new Integer(CMD_FIND),    cmdFind     );
		commands_.put(new Integer(CMD_BACK),    cmdBack     );
		commands_.put(new Integer(CMD_EXIT),    cmdExit     );
		commands_.put(new Integer(CMD_LOGIN),   cmdLogin    );
		commands_.put(new Integer(CMD_NEW),     cmdNew      );
		commands_.put(new Integer(CMD_ABOUT),   cmdAbout    );
		commands_.put(new Integer(CMD_CHAT),    cmdChat     );
                commands_.put(new Integer(CMD_SEND),    cmdSend     );
                commands_.put(new Integer(CMD_DEL),     cmdDel      );
                commands_.put(new Integer(CMD_EDIT),    cmdEdit     );
                commands_.put(new Integer(CMD_ACC),     cmdAccount  );
                commands_.put(new Integer(CMD_NEWCONT), cmdNewCont  );
                commands_.put(new Integer(CMD_DELCONT), cmdDelCont  );
		commands_.put(new Integer(CMD_LOGOUT),	cmdLogout   );
	}
        
	//Screens
	private static MainMenu     scrMenu;
	private static Splash       scrSplash;
	private static NewAccount   scrNewAcc;
	private static ContactsMenu scrContacts;
	private static About        scrAbout;
	private static Hashtable    scrChats;
        private static EditContact  scrNewCont;
	private static Alert	    scrAlert;
	
	//private Vector contacts_;
        
	final private String about_ = 
		Localization.tr("JIMMY - Instant Mobile Messenger\n")+
		Localization.tr("Copyright (C) 2006  JIMMY Project\n")+
                "-------------------------------------\n"+
                Localization.tr("Authors (sorted in alphabet order):\n\tMatevz Jekovec,\n\tZoran Mesec,\n\tDejan Sakelsak,\n\tJAnez Urevc,\n\tMatej Ušaj\n\n")+
                "WWW: http://jimmyim.berlios.de"; //[LOCALE]
        
	/**
	 * The constructor creates an instance of JimmyUI. JimmyUI is main class
	 * for handling GUI. In constructor we create screens (e.g. splash, main menu, ...)
	 * and we read configuration data from record store. 
	 * Constructor draws splash screen. It can be changed later using setView(int) method
	 * and screen codes (SCR_SPLASH, SCR_MAIN, ...) 
	 */        
	public JimmyUI() {
		jimmyUI_ = this;
		jimmy_   = Jimmy.getInstance();
		
		//draw splash screen
		scrSplash = new Splash();
		jimmy_.setDisplay(scrSplash);
		
		//Create screens
		scrMenu =       new MainMenu();
		scrNewAcc =     new NewAccount();
		scrAbout =      new About(about_);
		scrChats =	new Hashtable();
		scrContacts =   new ContactsMenu();
                scrNewCont =    new EditContact();
		scrAlert =	new Alert(null);
		
		scrAlert.setTimeout(Alert.FOREVER);
	}
        
	public static JimmyUI getInstance(){return jimmyUI_;}
	public Hashtable getCommands(){return commands_;}
	public Hashtable getChatWindows(){return scrChats;}
	public Vector getAccount(){return scrMenu.getAccounts();}
	
	public void setAccount(Vector a){((MainMenu)scrMenu).setAccounts(a);}
	public void addAccount(Account a){scrMenu.addAccount(a);}
        public void removeAccount(int index){scrMenu.removeAccount(index);}
	public void setSplashMess(String s){((Splash)scrSplash).setMess(s);}
	public void addContacts(Vector v){this.scrContacts.addContacts(v);}
        public void addContact(Contact c){this.scrContacts.addContact(c);}
        
	/**
	 *  This method changes displayed Screen. 
	 *  @param display each screen has its own code. Use SCR_* int constants.
	 */
	public void setView(int display){
		switch (display) {
                    case 1:
                        jimmy_.setDisplay(scrSplash);
			break;
                    case 2:
			jimmy_.setDisplay(scrMenu);
			break;
                    case JimmyUI.SCR_NEWACC:
                        jimmy_.setDisplay(scrNewAcc);
                        break;
		    case 6:
			jimmy_.setDisplay(scrContacts);
			break;
		}
	}
	
	public void setView(int display, ChatSession key){
		switch(display){
		    case 4:
			    jimmy_.setDisplay((Displayable)scrChats.get(key));
			    break;
		}
	    
	}
        
        public void accountConnected(Account a, Protocol p){
            scrNewCont.addAccount(a,p);
        }
        
        public void newGroup(String g){
            scrNewCont.addGroup(g);
        }
	
	public void removeGroup(String g){
	    scrNewCont.delGroup(g);
	}
	
	public static void jimmyCommand(Command c, Displayable d) {
            //commands from main menu
            if(d == scrMenu){
                if(c == cmdBack){
                    jimmy_.setDisplay(scrContacts);
                }//c == cmdBack                
                else if(c == cmdNew){
                    jimmy_.setDisplay(scrNewAcc);
                }//if c == cmdNew
                else if(c == cmdLogin){
                    int selected = scrMenu.getSelectedIndex();
                    if(selected > -1){
                        System.out.println("[DEBUG] Index of selected account: "+selected);
                        Account currAcc = (Account)scrMenu.getAccounts().elementAt(selected); 

                        if(currAcc.isConnected()){
                            scrAlert.setString(Localization.tr("Alredy connected!")); //[LOCALE]
                            scrAlert.setType(AlertType.WARNING);
                            jimmy_.setAlert(scrAlert,scrMenu);                        
                        }
                        else{
                            Vector login = new Vector();
                            login.addElement(currAcc);
                            jimmy_.setNewConnections(login);

                            ((Splash)scrSplash).setMess(Localization.tr("Connecting ")+currAcc.getUser()+"...");//[LOCALE]
                            jimmy_.setDisplay(scrSplash);
                        }
                    }
                    else
                        System.out.println("[DEBUG] None selected.");                     
                }//if c == cmdLogin
		else if(c == cmdLogout){
		    int selected = scrMenu.getSelectedIndex();
                    if(selected > -1){
                        System.out.println("[DEBUG] Index of selected account: "+selected);

                        Account logoutAccount =  (Account)scrMenu.getAccounts().elementAt(selected);
                        if(logoutAccount.isConnected())
                            logout(logoutAccount);
                        else{
                            scrAlert.setString(Localization.tr("Account is not connected!"));//[LOCALE]
                            scrAlert.setType(AlertType.WARNING);
                            jimmy_.setAlert(scrAlert,scrMenu);
                        }
                    }
                    else
                        System.out.println("[DEBUG] None selected.");                        
		}
                else if(c == cmdDel){
                    int selected = scrMenu.getSelectedIndex();
                    if(selected > -1){
                        Account account = (Account)scrMenu.getAccounts().elementAt(selected);
                        if(account.isConnected())
                            logout(account);
                        Store.removeAccount(account);
                        jimmyUI_.removeAccount(selected);
                    }
                    else
                        System.out.println("[DEBUG] None selected.");                        
                }//if c == cmdDel
                else if(c == cmdEdit){
                    int selected = scrMenu.getSelectedIndex();
                    if(selected > -1){
                        scrNewAcc.enableEdit((Account)scrMenu.getAccounts().elementAt(selected),selected);
                        jimmy_.setDisplay(scrNewAcc);
                    }
                    else
                        System.out.println("[DEBUG] None selected.");                    
                }//if c == cmdEdit
            }//if d == scrMenu
            
            //commands from new account page
            else if(d == scrNewAcc){
		Account newAccount;
                if(c == cmdOk){
                    Vector data     = ((NewAccount)d).getData();
                    String user     = (String)data.elementAt(0);
                    String pass     = (String)data.elementAt(1);
                    String server   = (String)data.elementAt(2);
                    String port     = (String)data.elementAt(3);
                    byte protocol   = ((Integer)data.elementAt(4)).byteValue();
                    boolean auto    = ((Boolean)data.elementAt(5)).booleanValue();
		    
		    if(server.equals(""))
			server  = null;
			
		    
                    
                    if(port.equals("")) port = "0";
                    /*System.out.println("----- Begin data entered for new account -----");
                    System.out.println("User: "+user+"\nPass: "+pass+"\nProtocol: "+protocol);
                    System.out.println("----- Begin data entered for new account -----");*/
                    
                    if(scrNewAcc.isInEditMode()){
                        newAccount = scrNewAcc.getEditAccount();
                        Store.removeAccount(newAccount);
                        
                        //newAccount.setUser(user);
                        newAccount.setPassword(pass);
                        newAccount.setServer(server);
                        newAccount.setPort(Integer.parseInt(port));
                        newAccount.setAutoLogin(auto);
                        
                        Store.addAccount(newAccount);
                    }
                    else{
                        newAccount = new Account(user,pass,protocol,server,Integer.parseInt(port),false, auto);	//slashrsm TODO: Set useSSL by the value - false by default! -Matevz
                        saveAccount(newAccount);
                        jimmyUI_.addAccount(newAccount);
                    }
                }//if c == cmdOk
                ((NewAccount)d).clearForm();
                //Store rs = new Store();
                //jimmyUI_.setAccount(rs.getAccounts());
                //rs.close();
                jimmy_.setDisplay(scrMenu);
            }// if d == scrNewAcc
            
            //commands from contacts page
            else if(d == scrContacts){
                if(c == cmdExit){
                    jimmy_.exitJimmy();
                }//if c == cmdExit
                else if(c == cmdChat){
                    ((ContactsMenu)d).startChat();
                }//c == cmdChat
                else if(c == cmdAccount){
                    jimmy_.setDisplay(scrMenu);
                }
                else if(c == cmdAbout){
                    jimmy_.setDisplay(scrAbout);                    
                }//if c == cmdAbout                
                else if(c == cmdNewCont){
                    jimmy_.setDisplay(scrNewCont);
                }
		else if(c == cmdEdit && scrNewCont.isInEditMode()){
		    jimmy_.setDisplay(scrNewCont);
		}
            }//d == scrContacts
            //commads from about page
            else if(d == scrAbout){
                if(c == cmdBack){
                    jimmy_.setDisplay(scrContacts);                    
                }// c == cmdBack
            }// if d == scrAbout
            else if(d == scrNewCont){
                jimmy_.setDisplay(scrContacts);                
            }
            else if(scrChats.contains(d)){
                if(c == cmdBack)
                    jimmy_.setDisplay(scrContacts);
            }

	}
        
        /*
         * This method is used for saving new account into record store.
         */
        private static void saveAccount(Account a){
	    Store.addAccount(a);
	    
	    Vector storeTest = Store.getAccounts();
	    System.out.println("[DEBUG]-------data in RS---------");
	    for(int i=0; i<storeTest.size(); i++)
		System.out.println("[DEBUG] User saved in RS: "+((Account)storeTest.elementAt(i)).getUser());
	    System.out.println("[DEBUG]--------------------------");
        } 

	/**
	 *  Changes contact status.
	 *  @param c contact to be modified.
	 */
	public void changeContactStatus(Contact c) {
		scrContacts.changeContactStatus(c);
	}
	
	public void msgRecieved(ChatSession cs, Contact c, String msg){
            if (cs == null)
                return;

            ChatWindow currentWindow = (ChatWindow)scrChats.get(cs);
            
            if(currentWindow == null){
                String name = (c.screenName()!=null) ? c.screenName():c.userID();
                currentWindow = new ChatWindow(name,cs);
                scrChats.put(cs,currentWindow);
            }

            scrContacts.getChatContactList().addElement(c);            
            currentWindow.msgRecieved(c,msg);
                
            //temporary
            jimmy_.setDisplay(currentWindow);
        }
	
	private static void logout(Account a){
	    Protocol p = a.getProtocol();
	    
	    scrContacts.removeContacts(p);
	    scrNewCont.delAccount(a);
            jimmy_.getProtocolList().removeElement(p);
	    p.logout();
	    a.setConnected(false);
	}
	
	public void warning(String s,int display){
	    Displayable disp = null;
	    switch (display) {
                    case 1:
                        disp = scrSplash;
			break;
                    case 2:
			disp = scrMenu;
			break;
                    case JimmyUI.SCR_NEWACC:
                        disp = scrNewAcc;
                        break;
		    case 6:
			disp = scrContacts;
			break;
		}
	    
	    scrAlert.setString(s);
	    scrAlert.setType(AlertType.WARNING);
	    jimmy_.setAlert(scrAlert,scrMenu);		    
	}
	
	public void modifyContact(Contact c, int index){
	    c.protocol().updateContactProperties(c);
	    scrContacts.removeContact(c,index,false);
	    scrContacts.addContact(c);
	}
	
	public void editContact(Contact c, int index){
	    scrNewCont.editMode(c,index);
	}
}
