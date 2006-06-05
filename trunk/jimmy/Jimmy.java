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
 File: jimmy/Jimmy.java
 Version: pre-alpha  Date: 2006/04/08
 Author(s): Matevz Jekovec, Zoran Mesec, Dejan Sakelsak, Janez Urevc
 */

package jimmy;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import java.io.IOException;
import java.util.Vector;

import jimmy.jabber.JabberProtocol;
import jimmy.msn.MSNProtocol;
import jimmy.ui.JimmyUI;
import jimmy.ui.MainMenu;
import jimmy.ui.Splash;

public class Jimmy extends MIDlet {
	public static Jimmy jimmy_; //Application main object
	final public static String VERSION	=	"pre-alpha"; //JIMMY version
        final public static String RS = "JimmyIM";
	public static Display display_; //Display object
	private static Vector protocolList_; //List of active protocols 

	//User interface stuff:
	public static JimmyUI ui_; //User Interface object
	public static Splash splash_; //Splash screen object
	public static MainMenu mainMenu_; //Main menu object
	
	protected void startApp() throws MIDletStateChangeException {
		jimmy_ = this;
		
                Account[] testAccounts = new Account[4];
                testAccounts[0] = new Account("janez","janez",0,"jabber.org",2222);
                testAccounts[1] = new Account("zoran","zoran",0,"jabber.org",2222);
                testAccounts[2] = new Account("matevz","matevz",0,"jabber.org",2222);
                testAccounts[3] = new Account("dejan","dejan",0,"jabber.org",2222);
                
		ui_ = new JimmyUI();
		
		//protocolList_.addElement(new MSNProtocol());
		//protocolList_.addElement(new JabberProtocol());
		
		//((Protocol)protocolList_.elementAt(0)).login("avgustin.ocepek@yahoo.com.au","0c3p3k");
		//((Protocol)protocolList_.elementAt(1)).login("jimmy@gristle.org","jimmy");
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}
    
        public static Jimmy getInstance(){
            return jimmy_;
        }
        
        public void setDisplay(Displayable d){
            Display.getDisplay(this).setCurrent(d);
        }
    
        public void exitJimmy(){
            //display_.setCurrent(null);
            
            try {
		destroyApp(true);
            } catch (MIDletStateChangeException e) {
		e.printStackTrace();
            }		
            notifyDestroyed();
        }        
}
