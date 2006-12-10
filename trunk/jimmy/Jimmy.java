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
import javax.microedition.lcdui.Alert;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import java.io.IOException;
import java.util.Vector;
import java.lang.Runtime;

import jimmy.jabber.JabberProtocol;
import jimmy.msn.MSNProtocol;
import jimmy.icq.ICQProtocol;
import jimmy.yahoo.YahooProtocol;
import jimmy.util.Store;

import jimmy.ui.JimmyUI;
import jimmy.ui.Localization;

public class Jimmy extends MIDlet implements Runnable, ProtocolInteraction {
	public static Jimmy jimmy_;								//Application main object
	final public static String VERSION	=	"1.0";	//JIMMY version
	final public static String RS = "JimmyIM";				//Record store name
	private static Display display_;						//Display object
	private static Vector protocolList_;					//List of active protocols
	private static Vector newConnections_; //List of connections to be established
	private Thread thr_;      

	//User interface stuff:
	public static JimmyUI ui_; //User Interface object
	
	protected void startApp() throws MIDletStateChangeException {
		jimmy_ = this;
		newConnections_ = null;
		protocolList_   = new Vector();
		
		Localization.setTranslation(System.getProperty("microedition.locale"), this);
		
		ui_ = new JimmyUI();
                
                ui_.setSplashMess(Localization.tr("Loading settings...")); //[LOCALE]
                String[] settings = Store.readSettings();
                ui_.setSplashMess(Localization.tr("Loading account data..."));     //[LOCALE]  
                Vector accounts = Store.getAccounts();
                ui_.setSplashMess(Localization.tr("Optimizing store..."));//[LOCALE]
                Store.resetStore(settings,accounts);
                
                if(accounts.size() == 0){
                    ui_.setSplashMess(Localization.tr("Welcome to Jimmy!"));//[LOCALE]
                    try {  
                    Thread.sleep(1000);
                    }catch (InterruptedException e){
                    		System.out.println(e.getMessage());
                    }
                    ui_.setView(JimmyUI.SCR_NEWACC);
                }
                else{
                    Account a = null;
                    boolean connect = false;
                    for(int i=0; i<accounts.size(); i++){
                        a = (Account)accounts.elementAt(i);
                        if(!a.getAutoLogin()){
                            ui_.setSplashMess(Localization.tr("Connecting to ")+a.getUser()+"...");//[LOCALE]
                            connectAccount(a);
                            connect = true;
			    
			    if(!a.isConnected()){
				protocolList_.removeElementAt(protocolList_.size()-1);				
				ui_.setSplashMess(Localization.tr("Connecting to ")+a.getUser()+Localization.tr(" failed!!!"));//[LOCALE]
				try{
				    Thread.sleep(1000);
				}catch(InterruptedException e){System.out.println(e.getMessage());}
			    }
				
                        }
                    }
                    if(connect){
                        ui_.setAccount(accounts);
                        ui_.setView(JimmyUI.SCR_CONT);
                    }
                    else{
                        ui_.setSplashMess(Localization.tr("Welcome to Jimmy!"));//[LOCALE]
                        try{
                            Thread.sleep(1000);
                        }catch(InterruptedException e){System.out.println(e.getMessage());}
                        ui_.setView(JimmyUI.SCR_MAIN);
                    }
                }

                
		thr_ = new Thread(this);
		thr_.start();
        }
	
	public void run(){
		//infinite loop
		while (true){
			try { Thread.sleep(1000); } catch (InterruptedException e) {System.out.println(e.getMessage());}    //stop for one second
                
			//if there are some new connections to establish
			if (newConnections_ != null) {
				Account current;

				//read data from vector and establish connections
				for (int i=0; i < newConnections_.size(); i++) {
					current = (Account)newConnections_.elementAt(i);  //current connections
					System.out.println("[DEBUG] Logging in user: " + current.getUser());
					
                                        connectAccount(current);
					if(current.isConnected())
					    ui_.setView(JimmyUI.SCR_CONT);
					else{
					    protocolList_.removeElementAt(protocolList_.size()-1);
					    ui_.warning(Localization.tr("Failed to connect! Check settings."),JimmyUI.SCR_MAIN);//[LOCALE]
					}
					    
				} //for i < newConnections_.size()
                    
				newConnections_ = null;
			}//if newConnections is not null
		}//while true
	}//run

	public void exitJimmy(){
		ui_.setSplashMess(Localization.tr("Destroying Jimmy..."));//[LOCALE]
		ui_.setView(JimmyUI.SCR_SPLASH);
		
		for (int i=0; i<protocolList_.size(); i++){
			((Protocol)protocolList_.elementAt(i)).logout();
		}
		protocolList_.removeAllElements();
		protocolList_ = null;
		thr_ = null;
		ui_ = null;
		
		Runtime r = Runtime.getRuntime();
		r.gc();
		
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
	
	public void setDisplay(Displayable d){Display.getDisplay(this).setCurrent(d);}
	public void setAlert(Alert a, Displayable d){Display.getDisplay(this).setCurrent(a,d);}
	
	public void stopProtocol(Protocol p) {
		p.logout();
		this.protocolList_.removeElement(p);
	}
	
	public void setProtocolStatus(Protocol p, byte status) {
		
	}
	
	public void addContact(Contact c) {ui_.addContact(c);}
    	public void addContacts(Vector c) {ui_.addContacts(c);}
        
	public void msgRecieved(ChatSession cs, Contact c, String msg){
	    ui_.msgRecieved(cs,c,msg);
	}


	/**
	 *  Changes contact status.
	 *  @param c contact to be modified.
	 */
	public void changeContactStatus(Contact c) {
		ui_.changeContactStatus(c);
	}
        
        public Protocol connectAccount(Account current){
            //which protocol to connect?
            switch(current.getProtocolType()){
                case Protocol.JABBER:
                    JabberProtocol jabber = new JabberProtocol(this);
                    current.setConnected(jabber.login(current));
                    protocolList_.addElement(jabber);
                    ui_.accountConnected(current,(Protocol)protocolList_.lastElement());
		    current.setProtocol(jabber);
                    return jabber;

		case Protocol.ICQ:
                    ICQProtocol icq = new ICQProtocol(this);
                    current.setConnected(icq.login(current));
                    protocolList_.addElement(icq);
                    ui_.accountConnected(current,(Protocol)protocolList_.lastElement());
		    current.setProtocol(icq);
                    return icq;

		case Protocol.MSN:
                    MSNProtocol msn = new MSNProtocol(this);
                    current.setConnected(msn.login(current));
                    protocolList_.addElement(msn);
                    ui_.accountConnected(current,(Protocol)protocolList_.lastElement());
		    current.setProtocol(msn);
                    return msn;

		case Protocol.YAHOO:
                    YahooProtocol yahoo = new YahooProtocol(this);
                    current.setConnected(yahoo.login(current));
                    protocolList_.addElement(yahoo);
                    ui_.accountConnected(current,(Protocol)protocolList_.lastElement());
		    current.setProtocol(yahoo);
                    return yahoo;

                default:
                    return null;
            } //switch
	}
}
