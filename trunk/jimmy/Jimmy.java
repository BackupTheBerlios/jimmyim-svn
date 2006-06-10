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
import jimmy.icq.ICQProtocol;

import jimmy.ui.JimmyUI;
import jimmy.ui.MainMenu;
import jimmy.ui.Splash;

public class Jimmy extends MIDlet implements Runnable {
	public static Jimmy jimmy_;         //Application main object
	final public static String VERSION	=	"pre-alpha"; //JIMMY version
        final public static String RS = "JimmyIM";
	private static Display display_; //Display object
	private static Vector protocolList_; //List of active protocols
        private static Vector newConnections_; //List of connections to be established
        private Thread thr_;        

	//User interface stuff:
	public static JimmyUI ui_; //User Interface object
	public static Splash splash_; //Splash screen object
	public static MainMenu mainMenu_; //Main menu object
	
	protected void startApp() throws MIDletStateChangeException {
		jimmy_          = this;
                newConnections_ = null;
                protocolList_   = new Vector();
		            
		ui_ = new JimmyUI();
                ui_.setView(JimmyUI.SCR_MAIN);
                
                thr_ = new Thread(this);
                thr_.start();
		
		//protocolList_.addElement(new MSNProtocol());
		//protocolList_.addElement(new JabberProtocol());
		
		//((Protocol)protocolList_.elementAt(0)).login("avgustin.ocepek@yahoo.com.au","0c3p3k");
		//((Protocol)protocolList_.elementAt(0)).login("jimmy@gristle.org","jimmy");
	}

        public void run(){
            //infinite loop
            while(true){
                try{ Thread.sleep(1000); } catch(Exception e){};    //stop for one second

                //if there are some new connections to establish
                if(newConnections_ != null){
                    Account current;
                    int i=0;
                    
                    //read data from vector and establish connections
                    while(!newConnections_.isEmpty()){
                        current = (Account)newConnections_.elementAt(i);  //current connections
                        
                        //which protocol to connect?
                        switch(current.getProtocolType()){
                            case 0:
                                JabberProtocol jabber = new JabberProtocol();
                                jabber.login(current);                                
                                protocolList_.addElement(jabber);
                                break;
                            case 1:
                                ICQProtocol icq = new ICQProtocol();
                                icq.login(current);
                                protocolList_.addElement(icq);
                                break;
                            case 2:
                                MSNProtocol msn = new MSNProtocol();
                                msn.login(current);
                                protocolList_.addElement(msn);
                                break;
                            case 3:
                                //yahoo
                                break;
                        }//switch
                        
                        current.setIndex(protocolList_.size()-1);                    
                        i++;
                    }//while newConnections is not empty
                    
                    newConnections_ = null;
                }//if newConnections is not null
                
                Protocol currentProt;
                Vector contacts = new Vector();
                Vector currentCont;
                for(int i=0; i<protocolList_.size(); i++){
                    currentProt = (Protocol)protocolList_.elementAt(i);
                    currentCont = currentProt.getContacts();   //get contacts from current account
                    
                    for(int j=0; j<currentCont.size(); j++){
                        contacts.addElement(currentCont.elementAt(j));  //add them to the contacts list
                    }//for j < currentCont.size()
                }//for i < protocolList_.size()
                
                //TO-DO - sort contacts list according to Group
                ui_.setContacts(contacts);
            }//while true
        }//run

        public void exitJimmy(){
            //display_.setCurrent(null);
            
            try {
		destroyApp(true);
            } catch (MIDletStateChangeException e) {
		e.printStackTrace();
            }		
            notifyDestroyed();
        }//exit jimmy        

	protected void pauseApp() {}
	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {}
    
        public static Jimmy getInstance()           {return jimmy_;}
        public static Vector getProtocolList()      {return protocolList_;}
        public void setNewConnections(Vector list)  {this.newConnections_ = list;}
                
        public void setDisplay(Displayable d)   {Display.getDisplay(this).setCurrent(d);}
}
