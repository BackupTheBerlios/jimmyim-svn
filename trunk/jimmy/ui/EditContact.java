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
 * File: jimmy/ui/EditContact.java
 * Version: pre-alpha  Date: 2006/08/07
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */

package jimmy.ui;

import jimmy.Account;
import jimmy.Protocol;
import jimmy.Contact;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Image;

import java.util.Hashtable;
import java.util.Vector;


public class EditContact extends Form implements CommandListener{
    private JimmyUI ui_;
    private Hashtable commands_;
    private Hashtable connections_;
    private boolean edit_;
    private Contact editContact_;
    private int editIndex_;
    
    private ChoiceGroup accounts_;
    private TextField   screenName_;
    private TextField   userID_;
    private ChoiceGroup groups_;
    private TextField   otherGroup_;    
    
    public EditContact() {
        super("Add/edit contact:");
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
        connections_ = new Hashtable();
        edit_ = false;
	editContact_ = null;
	editIndex_ = -1;
    
        try {
            setCommandListener(this);
        } catch(Exception e) { e.printStackTrace(); }
        
        //add commands
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_OK)   ));
        
        //add gui elements
        accounts_ = new ChoiceGroup("Account:",ChoiceGroup.POPUP);
        userID_ = new TextField("Username:","",100,TextField.ANY);
        screenName_ = new TextField("Screen name:","",100,TextField.ANY);
        groups_ = new ChoiceGroup("Group:",ChoiceGroup.POPUP);
        groups_.append("No group",null);	
        groups_.append("Other",null);
        otherGroup_ = new TextField("Other group:","",100,TextField.ANY);
        
        accounts_.setLayout(Item.LAYOUT_NEWLINE_AFTER); append(accounts_);
        userID_.setLayout(Item.LAYOUT_NEWLINE_AFTER); append(userID_);
        screenName_.setLayout(Item.LAYOUT_NEWLINE_AFTER); append(screenName_);
        groups_.setLayout(Item.LAYOUT_NEWLINE_AFTER); append(groups_);
        otherGroup_.setLayout(Item.LAYOUT_NEWLINE_AFTER); append(otherGroup_);
    }
    
    public boolean isInEditMode(){return this.edit_;}
    
    public void commandAction(Command c, Displayable d){
        if(c == (Command)commands_.get(new Integer(JimmyUI.CMD_OK))){
	    if(edit_){
		editContact_.setScreenName(screenName_.getString().equals("") ? null:Contact.removeSpaces(screenName_.getString()));
		
		String group;
		if(groups_.getSelectedIndex() == 0){
		    group = null;
		}
		else if(groups_.getSelectedIndex() == 1){
		    group = Contact.removeSpaces(otherGroup_.getString());
		}
		else{
		    group = Contact.removeSpaces(groups_.getString(groups_.getSelectedIndex()));
		}
		editContact_.setGroupName(group);
		
		ui_.modifyContact(editContact_,editIndex_);
		
		this.insert(0,accounts_);
		this.insert(1,userID_);		
		editContact_ = null;
		editIndex_ = -1;
		edit_ = false;	
	    }
	    else{
		String user = userID_.getString();
		Protocol p = (Protocol)connections_.get(accounts_.getString(accounts_.getSelectedIndex()));
		String nick = (screenName_.getString().equals("")) ? null:screenName_.getString();
		String group;
		
		if(groups_.getSelectedIndex() == 1)
		    group = otherGroup_.getString();
		else if(groups_.getSelectedIndex() == 0)
		    group = null;
		else
		    group = groups_.getString(groups_.getSelectedIndex());

		Contact newCont = new Contact(user,p,Contact.ST_OFFLINE,group,nick);
		p.addContact(newCont);
		Vector addContact = new Vector();
		addContact.addElement(newCont);
		ui_.addContacts(addContact);
	    }
        }
	else if(c == (Command)commands_.get(new Integer(JimmyUI.CMD_BACK))){
	    if(edit_){
		this.insert(0,accounts_);
		this.insert(1,userID_);		
		editContact_ = null;
		editIndex_ = -1;
		edit_ = false;	
	    }
	}
	this.clearForm();
        JimmyUI.jimmyCommand(c,d);
    }
    
    public void addAccount(Account a, Protocol p){
        connections_.put(a.getUser(),p);
        accounts_.append(a.getUser(),chooseImage(a));
    }
    
    public void delAccount(Account a){
	connections_.remove(a.getUser());
	int i=0;
	while(i<accounts_.size()){
	    if(a.getUser().equals(accounts_.getString(i))){
		accounts_.delete(i);
		break;
	    }
	    i++;
	}	
    }
    
    public void addGroup(String g){
        groups_.append(g,null);
    }
    
    public void delGroup(String g){
	int i=0;
	while(i<groups_.size()){
	    if(g.equals(groups_.getString(i))){
		groups_.delete(i);
		break;
	    }
	    i++;
	}
    }
    
    public void clearForm(){
        accounts_.setSelectedIndex(0,true);
        userID_.setString("");
        screenName_.setString("");
        groups_.setSelectedIndex(0,true);
        otherGroup_.setString("");
    }
    
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
    
    public void editMode(Contact c, int index){
	this.delete(0);
	this.delete(0);

	String name = (c.screenName() == null)? "":c.screenName();
	screenName_.setString(name);
	
	String currGroup = (c.groupName() == null) ? "No group":Contact.insertSpaces(c.groupName());
	int i=0;
	do{
	     groups_.setSelectedIndex(i,true);
	     i++;
	}while(!groups_.getString(i-1).equals(currGroup));
	
	editContact_ = c;
	editIndex_ = index;
	edit_ = true;
    }
}
