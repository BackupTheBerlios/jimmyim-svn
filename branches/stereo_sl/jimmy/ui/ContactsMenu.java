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
import java.lang.String;
import java.lang.StringBuffer;

public class ContactsMenu extends List implements CommandListener {
    private Vector      contacts_;
    private JimmyUI     ui_;
    private Hashtable   commands_;
    private Hashtable   chatWindows_;
    private Vector      currentChats_; 
    
    /** Creates a new instance of ContactsMenu */
    public ContactsMenu() {
        super(Localization.tr("Personal contacts:"),List.IMPLICIT);//[LOCALE]
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
		this.append("--=="+Contact.insertSpaces(current.groupName())+"==--",null);
                ui_.newGroup(Contact.insertSpaces(current.groupName()));
	    }
	    
	    if(current.screenName() != null)
		name = current.screenName();
	    else
		name = current.userID();
	    
	    j = findPosition(current, group);
	    screenIndex += j;
	    
	    this.insert(screenIndex,Contact.insertSpaces(name),chooseImage(current));	    
            group.insertElementAt(current,j);
        }
        //addContactsToMenu();
    }//setContacts()
    
    public void addContact(Contact c){
        Vector contact = new Vector();
        contact.addElement(c);
        this.addContacts(contact);
    }
    
    private int findPosition(Contact c, Vector g)
    {
      int j = 0;
      if (c.status() == Contact.ST_OFFLINE)
      {
        for (j = 0; j < g.size(); j++)
          if (((Contact)g.elementAt(j)).status() == Contact.ST_OFFLINE &&
              compareContacts(c, (Contact)g.elementAt(j), true) < 0)
            break;
      }
      else
      {
        for (j = 0; j < g.size(); j++)
        {
          if (((Contact)g.elementAt(j)).status() != Contact.ST_OFFLINE)
            if (compareContacts((Contact)g.elementAt(j), c, false) < 0)
              continue;
            else if (compareContacts((Contact)g.elementAt(j), c, false) == 0)
            {
              if (compareContacts(c, (Contact)g.elementAt(j), true) < 0)
                break;
            }
            else break;
          else break;
        }
      }
      
      return j;
    }
    
    private int compareContacts(Contact c1, Contact c2, boolean name)
    {
      if (name)
        return (c1.screenName() == null ? c1.userID() : c1.screenName()).toLowerCase().compareTo(
            (c2.screenName() == null ? c2.userID() : c2.screenName()).toLowerCase());
      return c1.status() - c2.status();
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
	while(j<currentGroup.size()-1 && !(currentContact.userID().equals(c.userID()) && currentContact.protocol().getType() == c.protocol().getType())){
	    j++;
	    currentContact = (Contact)currentGroup.elementAt(j);
	    
	    /*if( currentContact.userID().equals(c.userID()) && currentContact.protocol().getType() != c.protocol().getType() ){
		j++;
		currentContact = (Contact)currentGroup.elementAt(j);
	    }*/
	}
        if(currentContact.userID().equals(c.userID())){
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
            
            /* ****** UsAJ ***********/
            
            currentGroup.removeElementAt(j);
            this.delete(screenIndex);
            int p = findPosition(currentContact, currentGroup);
            currentGroup.insertElementAt(currentContact, p);
            this.insert(screenIndex - j + p, Contact.insertSpaces(name), chooseImage(currentContact));
            
            /* ****** UsAJ ***********/

//            this.set(screenIndex,Contact.insertSpaces(name),chooseImage(currentContact));	
            System.out.println("[DEBUG] User "+currentContact.userID()+" has status: "+currentContact.status());
//            rebuildContactList();
        }
        else{
            System.out.println("[DEBUG] No user found!");            
        }
    }
    
    /**
     *	This method is called when new chat is started
     */
    public void startChat(){
	int selected = this.getSelectedIndex(), i=0, j=0, selectedIndex;
        if(selected > -1){
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

                    Screen chat = new ChatWindow(Contact.insertSpaces(name), cs);
                    chatWindows_.put(cs,chat);
                    currentChats_.addElement(currentContact);
                    ui_.setView(JimmyUI.SCR_CHAT,cs);
                }
            }	
        }
        else
            System.out.println("[DEBUG] None selected.");
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
                
                if(current.protocol().getType() == Protocol.MSN){
                    if(current.screenName() != null && Contact.insertSpaces(current.screenName()).equals(name) || current.userID().equals(name)){
                        candidates.addElement(current);
                        index.addElement(new Integer(indexCount+j));
                    }                    
                }
                else{
                    if(current.screenName() != null && current.screenName().equals(name) || current.userID().equals(name)){
                        candidates.addElement(current);
                        index.addElement(new Integer(indexCount+j));
                    }
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
    
    public void removeContact(Contact currContact,int sel, boolean remove){
	this.delete(sel);
	int i=1, index=0;
	Contact test;
        Vector group;
	
        if(currContact.groupName() == null){
            group = ((Vector)contacts_.firstElement());
            i=0;
        }
        else{
            do{
                test = (Contact)((Vector)contacts_.elementAt(i)).firstElement();		    
                i++;
            }while( !test.groupName().equals(currContact.groupName()) );

            i--;
            group = ((Vector)contacts_.elementAt(i));
        }
        if(remove)
            currContact.protocol().removeContact(currContact);
        
	group.removeElement(currContact);
	if(group.isEmpty() && i!=0){
	    ui_.removeGroup(Contact.insertSpaces(currContact.groupName()));
            contacts_.removeElementAt(i);
            for(int j=0; j<i; j++){
                if(j>0)
                    index++;
                index += ((Vector)contacts_.elementAt(j)).size();
            }
	    this.delete(index);
	}	
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command c, Displayable d) {
        if(c == (Command)commands_.get(new Integer(ui_.CMD_DELCONT))){
            int sel = this.getSelectedIndex();
            if(sel > -1){
        	    Contact currContact = findContact(sel,this.getString(sel));
	    
                if(currContact != null){
                    removeContact(currContact,sel,true);
                    currContact.protocol().removeContact(currContact);
                }
            }
            else
                System.out.println("[DEBUG] None selected.");            
        }
	else if(c == (Command)commands_.get(new Integer(ui_.CMD_EDIT))){
            int sel = this.getSelectedIndex();
            if(sel > -1){
                Contact currContact = findContact(sel,this.getString(sel));

                if(currContact !=  null){
                    ui_.editContact(currContact,sel);
                }
            }
            else
                System.out.println("[DEBUG] None selected.");            
	}
        JimmyUI.jimmyCommand(c,d);
    }
    
    private Image chooseImage(Contact c){
	Image image = null;
	
	try{
	    switch(c.protocol().getType()){
		case Protocol.JABBER:
		    if(c.status() == Contact.ST_OFFLINE)
			image = Image.createImage("/jimmy/media/jabber-offline.png");
		    else
			image = Image.createImage("/jimmy/media/jabber-online.png");
		    break;
		case Protocol.ICQ:
		    if(c.status() == Contact.ST_OFFLINE)
			image = Image.createImage("/jimmy/media/icq-offline.png");
		    else
			image = Image.createImage("/jimmy/media/icq-online.png");		
		    break;
		case Protocol.MSN:
		    if(c.status() == Contact.ST_OFFLINE)
			image = Image.createImage("/jimmy/media/msn-offline.png");
		    else
			image = Image.createImage("/jimmy/media/msn-online.png");		
		    break;
		case Protocol.YAHOO:
		    if(c.status() == Contact.ST_OFFLINE)
			image = Image.createImage("/jimmy/media/yahoo-offline.png");
		    else
			image = Image.createImage("/jimmy/media/yahoo-online.png");		
		    break;	
	    }   
	}catch(Exception e){System.out.println("[ERROR] Failed loading contact icon:"+e.getMessage());};
	return image;
    }
    
    public void removeContacts(Protocol p){
	Contact currContact = null;
	Vector currGroup = null;
	String groupName = null;
	int index = 0, oldIndex;
	
	for(int i=0; i<this.contacts_.size(); i++){
	    currGroup = (Vector)this.contacts_.elementAt(i);
	    if(!currGroup.isEmpty())
		groupName = ((Contact)currGroup.elementAt(0)).groupName();
	    oldIndex = index-1;
	    for(int j=0;j<currGroup.size(); j++){
		currContact = (Contact)currGroup.elementAt(j);
		if(currContact.protocol().equals(p)){
		    this.delete(index);
		    currGroup.removeElementAt(j);
		    index--;
		    j--;
		}
		index++;
	    }
	    if(currGroup.isEmpty() && i>0){
		ui_.removeGroup(Contact.insertSpaces(groupName));
		contacts_.removeElementAt(i);
		i--;
		index--;
		this.delete(oldIndex);
	    }
	    index++;
	}
    }
    
    public Vector getChatContactList(){return this.currentChats_;}
}//class ContactsMenu
