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
 * File: jimmy/ui/NewAccount.java
 * Version: pre-alpha  Date: 2006/06/05
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */

package jimmy.ui;

import jimmy.Account;

import javax.microedition.lcdui.*;
import java.util.*;


/**
 *
 * @author  slashrsm
 * @version
 */
public class NewAccount extends Form implements CommandListener {
    final private TextField user_     = new TextField("Username:",    "", 50, TextField.ANY     );
    final private TextField pass_     = new TextField("Password:",    "", 30, TextField.PASSWORD);
    final private TextField server_   = new TextField("Server:",      "", 30, TextField.ANY     );
    final private TextField port_     = new TextField("Port:",        "", 10, TextField.DECIMAL );
    
    final private String[] protocols_ = {"Jabber","ICQ","MSN","Yahoo"};
    final private String[] auto_ = {"Yes","No"};
    final private ChoiceGroup protocol_ = new ChoiceGroup("Protocol:",Choice.EXCLUSIVE,protocols_, null);
    final private ChoiceGroup autoLogin_ = new ChoiceGroup("AutoLogin:",Choice.EXCLUSIVE,auto_,null);
    
    private Hashtable commands_;
    private JimmyUI ui_;
    
    private boolean edit_;
    private Account editAcc_;
    private int editIndex_;
    
    /**
     * constructor
     */
    public NewAccount() {
        super("Add new account:");
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
        
        edit_ = false;
        editAcc_ = null;
        editIndex_ = -1;
        
        try {
            setCommandListener(this);
        } catch(Exception e) { e.printStackTrace(); }
        
        protocol_.setLayout(Item.LAYOUT_NEWLINE_AFTER);   append(protocol_);        
        user_.setLayout(Item.LAYOUT_NEWLINE_AFTER);     append(user_);
        pass_.setLayout(Item.LAYOUT_NEWLINE_AFTER);     append(pass_);
        server_.setLayout(Item.LAYOUT_NEWLINE_AFTER);   append(server_);
        port_.setLayout(Item.LAYOUT_NEWLINE_BEFORE);    append(port_);
        autoLogin_.setLayout(Item.LAYOUT_NEWLINE_BEFORE);    append(autoLogin_);
        
        
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_OK)   ));
    }
    
    public boolean isInEditMode(){return edit_;}
    public Account getEditAccount(){return editAcc_;}
    public int getEditIndex(){return editIndex_;}
    /**
     * Called when action should be handled
     */
    public void commandAction(Command c, Displayable d) {
        JimmyUI.jimmyCommand(c,d);        
    }
    
    public Vector getData(){
        Vector data = new Vector();
        
        data.addElement(user_.getString());
        data.addElement(pass_.getString());
        data.addElement(server_.getString());
        data.addElement(port_.getString());
        data.addElement(new Integer(protocol_.getSelectedIndex()));

        boolean auto = (autoLogin_.getSelectedIndex() == 0) ? true : false;
        data.addElement(new Boolean(auto));

        return data;
    }
    
    public void clearForm(){
        user_.setString("");
        pass_.setString("");
        server_.setString("");
        port_.setString("");
        protocol_.setSelectedIndex(0,true);
        autoLogin_.setSelectedIndex(0,true);
        edit_ = false;
        editIndex_ = -1;
        editAcc_ = null;
        this.insert(0,protocol_);
    }
    
    public void enableEdit(Account a, int index){
        this.edit_ = true;
        this.editIndex_ = index;
        this.editAcc_ = a;
        
        user_.setString(a.getUser());
        pass_.setString(a.getPassword());
        server_.setString(a.getServer());
        port_.setString(Integer.toString(a.getPort()));
        protocol_.setSelectedIndex((int)a.getProtocolType(),true);
        int auto = (a.getAutoLogin()) ? 0 : 1;
        autoLogin_.setSelectedIndex(auto,true);
        this.delete(0);
    }
    
}
