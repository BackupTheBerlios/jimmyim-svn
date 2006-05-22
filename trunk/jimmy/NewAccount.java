/*
 * NewAccount.java
 *
 * Created on May 21, 2006, 5:32 PM
 */

package jimmy;

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
        if(c == (Command)commands_.get(new Integer(JimmyUI.CMD_BACK))){
            user_.setString("");
            pass_.setString("");
            server_.setString("");
            port_.setString("");
        }
           
        JimmyUI.jimmyCommand(c,d);        
    }
    
}
