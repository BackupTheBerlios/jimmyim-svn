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

public class MainMenu extends List implements CommandListener {
    private Account[] al_;          //list of accounts
    private Hashtable commands_;    //commands
    private JimmyUI ui_;

    /**
     * This constructor creates an instance of MainMenu. 
     * @param a array of accounts to be displayed in main menu
     */
    public MainMenu(Account[] a){
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
        
        //add accounts to menu
        this.al_ = a;
        addAccountsToMenu();        
    }
    
    /**
     * This method refreshes account list in main menu with data from a.
     * @param a accounts to be displayed in main menu
     */
    public void setAccountList(Account[] a){
        this.al_ = a;
        addAccountsToMenu();
    }
    
    private void addAccountsToMenu(){
        this.deleteAll();
        for(int i=0; i<al_.length; i++){
            this.append(al_[i].getUser()+"@"+al_[i].getServer(),null);
        }
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
}
