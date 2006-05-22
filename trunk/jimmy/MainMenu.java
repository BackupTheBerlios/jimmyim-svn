/*
 * MainMenu.java
 *
 * Created on May 20, 2006, 11:33 AM
 */
package jimmy;

import javax.microedition.lcdui.*;
import java.util.*;

/**
 *
 * @author  slashrsm
 * @version
 */
public class MainMenu extends List implements CommandListener {
    private Account[] al_;   //list of accounts
    private Hashtable commands_;
    private JimmyUI ui_;

    /**
     * constructor
     */
    public MainMenu(Account[] a){
        super("Accounts:",List.IMPLICIT);
        ui_ = JimmyUI.getInstance();
        
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}
        
        commands_ = ui_.getCommands();
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_EXIT) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_NEW)  ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_LOGIN)));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_ABOUT)));
        
        this.al_ = a;
        addAccountsToMenu();        
    }
    
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
