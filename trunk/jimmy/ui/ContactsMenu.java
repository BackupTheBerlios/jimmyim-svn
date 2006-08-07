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
 * File: jimmy/ui/ContactsMenu.java
 * Version: pre-alpha  Date: 2006/06/10
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */

package jimmy.ui;

import jimmy.Contact;
import jimmy.Protocol;
import jimmy.ChatSession;

import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Screen;

import java.util.Vector;
import java.util.Hashtable;

public class ContactsMenu extends List implements CommandListener {
    private Vector      contacts_;
    private JimmyUI     ui_;
    private Hashtable   commands_;
    private Hashtable   chatWindows_;
    private Vector      currentChats_; 
    
    /** Creates a new instance of ContactsMenu */
    public ContactsMenu() {
        super("Personal contacts:",List.IMPLICIT);
        ui_ =           JimmyUI.getInstance();
        commands_ =     ui_.getCommands();
	chatWindows_ =  ui_.getChatWindows();
        currentChats_ = new Vector();        
        contacts_ =     new Vector();
	contacts_.addElement(new Vector());  //for contacts without group
        
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}	
	
        //add commands
        addCommand((Command)commands_.get(new Integer(ui_.CMD_EXIT)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_CHAT)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_NEWCONT)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_DELCONT)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_EDIT)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_ACC)));        
        addCommand((Command)commands_.get(new Integer(ui_.CMD_ABOUT)));
        
    }// ContactsMenu()
    
    public void addContacts(Vector v){
	Vector group;
	Contact current;
	String name;
	int j, screenIndex;
        for(int i=0; i < v.size(); i++){
	    j=1;
	    screenIndex = 1;
	    current = (Contact)v.elementAt(i);
	    
	    
	    if(((Contact)v.elementAt(i)).groupName() != null){
		screenIndex += ((Vector)contacts_.elementAt(0)).size();
		//find apropriate group
		while( j<contacts_.size() &&  !current.groupName().equals(((Contact)((Vector)contacts_.elementAt(j)).firstElement()).groupName())){
		    screenIndex += ((Vector)contacts_.elementAt(j)).size() + 1;		
		    j++;
		}
	    }
	    else{
		j=0;
		screenIndex=0;
	    }
	    
	    //if group already exists
	    if(j < contacts_.size()){
		group = (Vector)contacts_.elementAt(j);
	    }
	    //if it is a new group
	    else{
		group = new Vector();
		contacts_.addElement(group);
		this.append("--=="+current.groupName()+"==--",null);
                ui_.newGroup(current.groupName());
	    }
	    
	    if(current.screenName() != null)
		name = current.screenName();
	    else
		name = current.userID();
	    
	    j=0;
	    while( j<group.size() && current.userID().compareTo(((Contact)group.elementAt(j)).userID()) > 0 && current.status() != ((Contact)group.elementAt(j)).status())
		j++;
	    
	    screenIndex += j;
	    
	    this.insert(screenIndex,name,chooseImage(current));	    
            group.insertElementAt(current,j);
        }
        //addContactsToMenu();
    }//setContacts()
    
    public void addContact(Contact c){
        Vector contact = new Vector();
        contact.addElement(c);
        this.addContacts(contact);
    }
    
    /**
     *  Changes contact status.
     *  @param c contact to be modified.
     */
    public void changeContactStatus(Contact c) {
	Contact currentContact, firstInGroup;
	Vector currentGroup;
	String name;
	int i=1, j=0, screenIndex=0;
	System.out.println("[DEBUG] Change status to user "+c.userID()+" and group "+c.groupName()+".");
	
	//Find appropriate group
	if(c.groupName() != null){
	    firstInGroup = ((Contact)((Vector)contacts_.elementAt(i)).firstElement());
	    while( !firstInGroup.groupName().equals(c.groupName()) ){
		i++;
		firstInGroup = ((Contact)((Vector)contacts_.elementAt(i)).firstElement());
	    }
	    System.out.println("[DEBUG] Found group "+firstInGroup.groupName()+".");
	}
	else{
	    i=0;
	    System.out.println("[DEBUG] Contact belongs to no group.");	    
	}
        currentGroup = (Vector)contacts_.elementAt(i);

	
	//Find contact in appropriate group
	currentContact = (Contact)currentGroup.elementAt(j);
	while( j<currentGroup.size() && !(currentContact.userID().equals(c.userID()))){
	    j++;
	    currentContact = (Contact)currentGroup.elementAt(j);
	    
	    if( currentContact.userID().equals(c.userID()) && currentContact.protocol().getType() != c.protocol().getType() ){
		j++;
		currentContact = (Contact)currentGroup.elementAt(j);
	    }
	} 
	System.out.println("[DEBUG] Found user "+currentContact.userID()+".");
	
	//set status
	currentContact.setStatus(c.status());
	currentContact.setStatusMsg(c.statusMsg());
	
	//change display string
	if(currentContact.groupName() != null){
	    for(int k=0; k<i; k++)
		screenIndex += ((Vector)contacts_.elementAt(k)).size()+1;
	    screenIndex += j;
	}
	else
	    screenIndex = j;
		
	
	if(currentContact.screenName() != null)
	    name = new String(currentContact.screenName());
	else
	    name = new String(currentContact.userID());
	
        this.set(screenIndex,name,chooseImage(currentContact));	
	System.out.println("[DEBUG] User "+currentContact.userID()+" has status: "+currentContact.status());
    }
    
    /**
     *	This method is called when new chat is started
     */
    public void startChat(){
	int selected = this.getSelectedIndex(), i=0, j=0, selectedIndex;
	Contact currentContact = this.findContact(selected,this.getString(selected));
	System.out.println("[DEBUG] User to start chat with: "+this.getString(selected));

	if(currentContact != null)
	    System.out.println("[DEBUG] Found Contact to start chat with: "+currentContact.userID());
	else
	    System.out.println("[DEBUG] You want to chat with group label!");
	
	if(currentContact != null){
            if(currentChats_.contains(currentContact)){
                ChatSession cs = currentContact.protocol().getChatSession(currentContact);
                ChatWindow currentWindow = (ChatWindow)ui_.getChatWindows().get(cs);
                ui_.setView(JimmyUI.SCR_CHAT,cs);
            }
            else{
                ChatSession cs;
                String name;
                cs = currentContact.protocol().startChatSession(currentContact);
	    
                if(currentContact.screenName() != null)
                    name = currentContact.screenName();
                else    
                    name = currentContact.userID();
	    
                Screen chat = new ChatWindow(name, cs);
                chatWindows_.put(cs,chat);
                currentChats_.addElement(currentContact);
                ui_.setView(JimmyUI.SCR_CHAT,cs);
            }
	}	
    }
    
    private Contact findContact(int selected, String name){
        Vector candidates = new Vector();
        Vector index = new Vector();
        Contact current;
        int indexCount = 0;
        Vector group;
        for(int i=0; i<contacts_.size(); i++){
            group = (Vector)contacts_.elementAt(i);
            for(int j=0; j<group.size(); j++){
                current = (Contact)group.elementAt(j);
                
                if(current.screenName() != null && current.screenName().equals(name) || current.userID().equals(name)){
                    candidates.addElement(current);
                    index.addElement(new Integer(indexCount+j));
                }
            }
            indexCount += group.size();
        }
        
        switch(candidates.size()){
            case 0:
                return null;
            case 1:
                return (Contact)candidates.firstElement();
            default:
                for(int i=0; i<candidates.size(); i++){
                    if(((Integer)index.elementAt(i)).intValue() == selected){
                        return (Contact)candidates.elementAt(i);
                    }
                }
        }
        return null;
    }

    /**
     * Called when action should be handled
     */
    public void commandAction(Command c, Displayable d) {
        if(c == (Command)commands_.get(new Integer(ui_.CMD_DEL))){
            int sel = this.getSelectedIndex();
        }
        JimmyUI.jimmyCommand(c,d);
    }
    
    private Image chooseImage(Contact c){
	Image image = null;
	
	try{
	    switch(c.protocol().getType()){
		case Protocol.JABBER:
		    if(c.status() == Contact.ST_ONLINE)
			image = Image.createImage("/jimmy/jabber-online.png");
		    else
			image = Image.createImage("/jimmy/jabber-offline.png");
		    break;
		case Protocol.ICQ:
		    if(c.status() == Contact.ST_ONLINE)
			image = Image.createImage("/jimmy/icq-online.png");
		    else
			image = Image.createImage("/jimmy/icq-offline.png");		
		    break;
		case Protocol.MSN:
		    if(c.status() == Contact.ST_ONLINE)
			image = Image.createImage("/jimmy/msn-online.png");
		    else
			image = Image.createImage("/jimmy/msn-offline.png");		
		    break;
		case Protocol.YAHOO:
		    if(c.status() == Contact.ST_ONLINE)
			image = Image.createImage("/jimmy/msn-online.png");
		    else
			image = Image.createImage("/jimmy/msn-offline.png");		
		    break;	
	    }   
	}catch(Exception e){System.out.println("[ERROR] Failed loading contact icon:"+e.getMessage());};
	return image;
    }
    
    public Vector getChatContactList(){return this.currentChats_;}
}//class ContactsMenu
