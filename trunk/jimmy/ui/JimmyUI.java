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
        
	//Screen codes
	final public static int SCR_SPLASH  = 1;
	final public static int SCR_MAIN    = 2;
	final public static int SCR_NEWACC  = 3;
	final public static int SCR_CHAT    = 4;
	final public static int SCR_ABOUT   = 5;
        

	//Commands:
	final private static Command cmdOk     = new Command("OK",          Command.OK,     1);
	final private static Command cmdCancel = new Command("Cancel",      Command.BACK,   2);
	final private static Command cmdYes    = new Command("Yes",         Command.OK,     1);
	final private static Command cmdNo     = new Command("No",          Command.CANCEL, 2);
	final private static Command cmdFind   = new Command("Find",        Command.OK,     1);
	final private static Command cmdBack   = new Command("Back",        Command.BACK,   2);
	final private static Command cmdExit   = new Command("Exit",        Command.EXIT,   1);
	final private static Command cmdLogin  = new Command("Login",       Command.ITEM,   1);
	final private static Command cmdNew    = new Command("New account", Command.ITEM,   1);
	final private static Command cmdAbout  = new Command("About",       Command.ITEM,   1);
	final private static Command cmdChat   = new Command("Chat",        Command.ITEM,   1);
	
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
	}
        
	//Screens
	private static MainMenu     scrMenu;
	private static Splash       scrSplash;
	private static NewAccount   scrNewAcc;
	private static ContactsMenu scrContacts;
	private static About        scrAbout;
	
	//private Vector contacts_;
        
	final private String about_ = 
                "JIMMY - Instant Mobile Messenger\n"+
                "Copyright (C) 2006  JIMMY Project\n"+
                "-------------------------------------\n"+
                "Authors (sorted in alphabet order):\n\tMatevz Jekovec,\n\tZoran Mesec,\n\tDejan Sakelsak,\n\tJAnez Urevc\n\n"+
                "WWW: http://jimmyim.berlios.de";
        
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
		scrContacts =   new ContactsMenu();
		scrAbout =      new About(about_);
		
		//Read configuration data from record store
		Store rs = new Store();
		scrMenu.addAccounts(rs.getAccounts());
	}
        
	public static JimmyUI getInstance(){return jimmyUI_;}
	public Hashtable getCommands(){return commands_;}
	public Vector getAccount(){return scrMenu.getAccounts();}
	
	public void setAccount(Vector a){((MainMenu)scrMenu).setAccounts(a);}
	public void addAccount(Account a){scrMenu.addAccount(a);}
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
		}
	}
	
	public static void jimmyCommand(Command c, Displayable d) {
            //commands from main menu
            if(d == scrMenu){
                if(c == cmdExit){
                    jimmy_.exitJimmy();
                }//if c == cmdExit
                else if(c == cmdNew){
                    jimmy_.setDisplay(scrNewAcc);
                }//if c == cmdNew
                else if(c == cmdLogin){
                    int selected = scrMenu.getSelectedIndex();
		    System.out.println("Index of selected account: "+selected);
                    Vector newConnections = new Vector();
                    newConnections.addElement(scrMenu.getAccounts().elementAt(selected));
                    jimmy_.setNewConnections(newConnections);
                    
                    jimmy_.setDisplay(scrContacts);
                }//if c == cmdLogin
                else if(c == cmdAbout){
                    jimmy_.setDisplay(scrAbout);                    
                }//if c == cmdAbout
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
                    
                    if(port.equals("")) port = "0";
                    /*System.out.println("----- Begin data entered for new account -----");
                    System.out.println("User: "+user+"\nPass: "+pass+"\nProtocol: "+protocol);
                    System.out.println("----- Begin data entered for new account -----");*/
                    newAccount = new Account(user,pass,protocol,server,Integer.parseInt(port),false);
                    saveAccount(newAccount);
		    jimmyUI_.addAccount(newAccount);		    
                }//if c == cmdOk
                ((NewAccount)d).clearForm();
                //Store rs = new Store();
                //jimmyUI_.setAccount(rs.getAccounts());
                //rs.close();
                jimmy_.setDisplay(scrMenu);
            }// if d == scrNewAcc
            
            //commands from contacts page
            else if(d == scrContacts){
                if(c == cmdBack){
                    jimmy_.setDisplay(scrMenu);
                }//c == cmdBack
                else if(c == cmdChat){
                    
                }//c == cmdChat
            }//d == scrContacts
            
            //commads from about page
            else if(d == scrAbout){
                if(c == cmdBack){
                    jimmy_.setDisplay(scrMenu);                    
                }// c == cmdBack
            }// if d == scrAbout
	}
        
        /*
         * This method is used for saving new account into record store.
         */
        private static void saveAccount(Account a){
            Store rs = new Store();
            /*if(rs.addAccount(a))
                System.out.println("New account saved in RS");*/
	    rs.addAccount(a);
        }  
}
