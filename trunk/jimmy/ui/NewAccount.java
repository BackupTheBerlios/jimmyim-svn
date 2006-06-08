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

import javax.microedition.lcdui.*;
import java.util.*;


/**
 *
 * @author  slashrsm
 * @version
 */
public class NewAccount extends Form implements CommandListener {
    final private TextField user_     = new TextField("Username:",    "", 20, TextField.ANY     );
    final private TextField pass_     = new TextField("Password:",    "", 20, TextField.PASSWORD);
    final private TextField server_   = new TextField("Server:",      "", 20, TextField.ANY     );
    final private TextField port_     = new TextField("Port:",        "", 20, TextField.DECIMAL );
    
    final private String[] protocols_ = {"Jabber","ICQ","MSN","Yahoo"};
    final private ChoiceGroup protocol_ = new ChoiceGroup("Protocol:",Choice.EXCLUSIVE,protocols_, null);
    
    private Hashtable commands_;
    private JimmyUI ui_;
    
    /**
     * constructor
     */
    public NewAccount() {
        super("Add new account:");
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
        
        try {
            setCommandListener(this);
        } catch(Exception e) { e.printStackTrace(); }
        
        user_.setLayout(Item.LAYOUT_NEWLINE_AFTER);     append(user_);
        pass_.setLayout(Item.LAYOUT_NEWLINE_AFTER);     append(pass_);
        server_.setLayout(Item.LAYOUT_NEWLINE_AFTER);   append(server_);
        port_.setLayout(Item.LAYOUT_NEWLINE_BEFORE);    append(port_);
        protocol_.setLayout(Item.LAYOUT_NEWLINE_AFTER);   append(protocol_);
        
        
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_OK)   ));
    }
    
    /**
     * paint
     */
    public void paint(Graphics g) {

    }
    
    /**
     * Called when a key is pressed.
     */
    protected  void keyPressed(int keyCode) {
    }
    
    /**
     * Called when a key is released.
     */
    protected  void keyReleased(int keyCode) {
    }
    
    /**
     * Called when a key is repeated (held down).
     */
    protected  void keyRepeated(int keyCode) {
    }
    
    /**
     * Called when the pointer is dragged.
     */
    protected  void pointerDragged(int x, int y) {
    }
    
    /**
     * Called when the pointer is pressed.
     */
    protected  void pointerPressed(int x, int y) {
    }
    
    /**
     * Called when the pointer is released.
     */
    protected  void pointerReleased(int x, int y) {
    }
    
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

        return data;
    }
    
    public void clearForm(){
        user_.setString("");
        pass_.setString("");
        server_.setString("");
        port_.setString("");
        protocol_.setSelectedIndex(0,true);        
    }
    
}
