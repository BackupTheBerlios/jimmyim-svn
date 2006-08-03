/*
 * JIMMY - Instant Mobile Messenger
 *   Copyright (C) 2006  JIMMY Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * **********************************************************************
 * File: jimmy/ui/MainMenu.java
 * Version: pre-alpha  Date: 2006/06/05
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */
package jimmy.ui;

import javax.microedition.lcdui.*;
import java.util.*;

import jimmy.Account;
import jimmy.util.Store;
import jimmy.Protocol;

public class MainMenu extends List implements CommandListener {
    private Vector al_;          //list of accounts
    private Hashtable commands_;    //commands
    private JimmyUI ui_;

    /**
     * This constructor creates an instance of MainMenu. 
     * @param a array of accounts to be displayed in main menu
     */
    public MainMenu(){
        super("Accounts:",List.IMPLICIT);
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
        
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}
        
        //add commands to menu
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_EXIT) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_NEW)  ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_LOGIN)));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_ABOUT)));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_DEL)));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_EDIT)));
        
        //add accounts to menu
        this.al_ = new Vector();
	addAccounts(Store.getAccounts());    
    }
    
    /**
     * Adds accounts to menu
     * @param a new accounts to be displayed in main menu
     */
    public void addAccounts(Vector a){
	Image icon;
	for(int i=0; i<a.size(); i++){
	    
	    int j=0;
	    while( j<al_.size() && ((Account)a.elementAt(i)).getUser().compareTo(((Account)al_.elementAt(j)).getUser()) > 0){
		j++;
	    }
	    
	    al_.insertElementAt(a.elementAt(i),j);		    //add to local account list
	    icon = chooseImage((Account)a.elementAt(i));
	    this.insert(j,((Account)a.elementAt(i)).getUser(),icon);   //add to screen
	}
    }
    
    public void addAccount(Account a){
	int j=0;
	Image icon;
	while( j < al_.size() && a.getUser().compareTo(((Account)al_.elementAt(j)).getUser()) > 0 ){
	    j++;
	}
	    
	al_.insertElementAt(a,j);	    //add to local account list
	icon = chooseImage(a);
	this.insert(j,((Account)al_.elementAt(j)).getUser(),icon);   //add to screen
    }
    
    public void removeAccount(int index){
        this.delete(index);
        al_.removeElementAt(index);
    }
    
    /**
     * Sets vector of accounts to be displayed in main menu. Vector is beeing sorted here.
     * @param a accounts to be displayed in main menu
     */    
    public void setAccounts(Vector a){
	this.deleteAll();
	this.al_ = a;		//set local account list
	
	//Sort account list
	Object temp;
	int index;
	for(int i=0; i<al_.size(); i++){
	    temp = al_.elementAt(i);
	    index = i;
	    for(int j=0; j<al_.size();j++){
		if( ((Account)al_.elementAt(i)).getUser().compareTo(((Account)al_.elementAt(j)).getUser()) < 0 ){
		    al_.removeElementAt(index);
		    al_.insertElementAt(al_.elementAt(j),index);
		    
		    al_.removeElementAt(j);
		    al_.insertElementAt(temp,j);
		    index = j;
		}
	    }
	}
	
	Image icon;
	for(int i=0; i<a.size(); i++){
	    icon = chooseImage((Account)a.elementAt(i));
	    this.append(((Account)a.elementAt(i)).getUser(),icon);   //add to screen
	}
    }
        
    public Vector getAccounts(){return this.al_;}
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command c, Displayable d) {
        JimmyUI.jimmyCommand(c,d);
    }
    
    /**
     *	Coose right icon according to protocol.
     *	@param a account to be checked
     */
    private Image chooseImage(Account a){
	Image i = null;
	try{
	    switch(a.getProtocolType()){
		case Protocol.JABBER:
                    i = Image.createImage("/jimmy/jabber-online.png");
		    break;
		case Protocol.MSN:
		    i = Image.createImage("/jimmy/msn-online.png");
		    break;
		case Protocol.ICQ:
		    i = Image.createImage("/jimmy/icq-online.png");
		    break;
		case Protocol.YAHOO:
		    i = Image.createImage("/jimmy/yahoo-online.png");
		    break;		
	    }
	}catch(Exception e){System.out.println("[ERROR] Problem loading account icon: "+e.getMessage());};
	
	return i;
    }
}
