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

import javax.microedition.lcdui.List;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import java.util.Vector;
import java.util.Hashtable;

public class ContactsMenu extends List implements CommandListener {
    private Vector contacts_;
    private JimmyUI ui_;
    private Hashtable commands_;
    
    /** Creates a new instance of ContactsMenu */
    public ContactsMenu() {
        super("Personal contacts:",List.IMPLICIT);
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
        contacts_ = new Vector();
        
        //add commands
        addCommand((Command)commands_.get(new Integer(ui_.CMD_BACK)));
        addCommand((Command)commands_.get(new Integer(ui_.CMD_CHAT)));
        
    }// ContactsMenu()
    
    public void addContacts(Vector v){
	Vector group;
	Contact current;
	String name;
	int j, screenIndex;
        for(int i=0; i < v.size(); i++){
	    j=0;
	    screenIndex = 1;
	    current = (Contact)v.elementAt(i);
	    
	    
	    //find apropriate group
	    while( j<contacts_.size() &&  !current.groupName().equals(((Contact)((Vector)contacts_.elementAt(j)).firstElement()).groupName())){
		screenIndex += ((Vector)contacts_.elementAt(j)).size() + 1;		
		j++;
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
	    }
	    
	    if(current.screenName() != null)
		name = current.screenName();
	    else
		name = current.userID();
	    
	    j=0;
	    while( j<group.size() && current.userID().compareTo(((Contact)group.elementAt(j)).userID()) > 0 && current.status() != ((Contact)group.elementAt(j)).status())
		j++;
	    
	    screenIndex += j;
	    
	    switch(current.status()){
		case Contact.ST_OFFLINE:
		    name = name.concat(" - offline");
		    break;
		case Contact.ST_AWAY:
		    name = name.concat(" - away");
		    break;
		case Contact.ST_BUSY:
		    name = name.concat(" - busy");
		    break;
		case Contact.ST_ONLINE:
		    name = name.concat(" - online");
		    break;	
	    }
	 
	    this.insert(screenIndex,name,null);
            group.insertElementAt(current,j);
        }
        //addContactsToMenu();
    }//setContacts()
    
    public void addContact(Contact c){
        this.contacts_.addElement(c);
        this.append(c.screenName(),null);
    }
    
    /**
     *  Changes contact status.
     *  @param c contact to be modified.
     */
    public void changeContactStatus(Contact c) {
	Contact currentContact, firstInGroup;
	Vector currentGroup;
	String name;
	int i=0, j=0, screenIndex=0;
	
	//Find appropriate group
	firstInGroup = ((Contact)((Vector)contacts_.elementAt(i)).firstElement());
	while( !firstInGroup.groupName().equals(c.groupName()) ){
	    i++;
	    firstInGroup = ((Contact)((Vector)contacts_.elementAt(i)).firstElement());
	}
	currentGroup = (Vector)contacts_.elementAt(i);
	
	//Find contact in appropriate group
	currentContact = (Contact)currentGroup.elementAt(j);
	while( !currentContact.userID().equals(c.userID()) && currentContact.protocol().getType() != c.protocol().getType() ){
	    j++;
	    currentContact = (Contact)currentGroup.elementAt(j);
	} 
	
	//set status
	currentContact.setStatus(c.status());
	currentContact.setStatusMsg(c.statusMsg());
	
	//change display string
	for(int k=0; k<i; k++)
	    screenIndex += ((Vector)contacts_.elementAt(k)).size()+1;
	screenIndex += j+1;
	
	if(currentContact.screenName() != null)
	    name = new String(currentContact.screenName());
	else
	    name = new String(currentContact.userID());
	
	switch(c.status()){
	    case Contact.ST_OFFLINE:
		name = name.concat(" - offline");
		break;
	    case Contact.ST_AWAY:
		name = name.concat(" - away");
		break;
	    case Contact.ST_BUSY:
		name = name.concat(" - busy");
		break;
	    case Contact.ST_ONLINE:
		name = name.concat(" - online");
		break;	
	}
	
	System.out.println("[DEBUG] User "+currentContact.userID()+" has status: "+currentContact.status());
	this.set(screenIndex,name,null);
    }

    public void commandAction(Command c, Displayable d){
        ui_.jimmyCommand(c,d);
    }//commandAction()
}//class ContactsMenu
