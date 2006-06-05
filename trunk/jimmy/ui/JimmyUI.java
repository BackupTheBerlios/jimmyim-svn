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

 Author(s): Matevz Jekovec
*/

package jimmy.ui;

import javax.microedition.lcdui.*;
import java.util.*;

import jimmy.Account;
import jimmy.Jimmy;
import jimmy.util.RMS;


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
	
	static private Hashtable commands_ = new Hashtable(); //commands list
	static private Displayable lastDisplayable_; //displayable object
	static private JimmyUI jimmyUI_; //JimmyUI object
        static private Jimmy jimmy_;
        static private Account[] acc_;  //Array of accoounts
	
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
	}
        
        //Screens
        private static MainMenu    scrMenu;
        private static Splash      scrSplash;
        private static NewAccount  scrNewAcc;
	
	public JimmyUI() {
		jimmyUI_ = this;
                jimmy_   = Jimmy.getInstance();
                
                RMS rs = new RMS(Jimmy.RS);
                acc_    = rs.getAccounts();
                
                scrMenu = new MainMenu(acc_);
                scrNewAcc = new NewAccount();
                jimmy_.setDisplay(scrMenu);
	}
	
	public static void jimmyCommand(Command c, Displayable d) {
            if(d == scrMenu){
                if(c == cmdExit)
                    jimmy_.exitJimmy();
                if(c == cmdNew)
                    jimmy_.setDisplay(scrNewAcc);
            }
            else if(d == scrNewAcc){
                if(c == cmdOk){
                    Vector data     = ((NewAccount)d).getData();
                    String user     = (String)data.elementAt(0);
                    String pass     = (String)data.elementAt(1);
                    String server   = (String)data.elementAt(2);
                    int protocol    = ((Integer)data.elementAt(4)).intValue();
                    saveAccount(user,pass,server,protocol);
                }
                ((NewAccount)d).clearForm();
                RMS rs = new RMS(Jimmy.RS);
                jimmyUI_.setAccount(rs.getAccounts());
                
                Jimmy.getInstance().setDisplay(scrMenu);
                
            }
	}
        
        public static JimmyUI getInstance(){
            return jimmyUI_;
        }
        
        public Hashtable getCommands(){
            return commands_;
        }
        
        public void setAccount(Account[] a){
            this.acc_ = a;
            ((MainMenu)scrMenu).setAccountList(acc_);
        }
        
        private static void saveAccount(String u, String p, String s, int protocol){
            RMS rs = new RMS(Jimmy.RS);
            u = u+"@"+s;
            rs.addRecord((char)protocol+""+(char)u.length()+u+p);
        }   
}
