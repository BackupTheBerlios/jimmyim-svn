/*
 * ChatWindow.java
 *
 * Created on Četrtek, 27 julij 2006, 2:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy.ui;
import jimmy.ChatSession;

import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.StringItem;

import java.util.Hashtable;
import java.util.Calendar;

/**
 *
 * @author slashrsm
 */
public class ChatWindow extends Form implements CommandListener {
    private JimmyUI	ui_;
    private Hashtable	commands_;
    private ChatSession	cs_;
    private TextField	tf_;
    
    /** Creates a new instance of ChatWindow */
    public ChatWindow(String user, ChatSession cs) {
	super("Chat with "+user+":");
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
	cs_ = cs;
	tf_ = new TextField("Message:","",200,TextField.ANY);
        
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}
        
        //add commands to menu
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK) ));
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_CHAT) ));
	
	//add text field
	this.append(tf_);
	
    }
    
    public void commandAction(Command c, Displayable d){
	if(c == (Command)commands_.get(new Integer(JimmyUI.CMD_CHAT) )){
	    String time = new String(Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND);
	    String msg = ((TextField)get(size()-1)).getString();
	    ((TextField)get(size()-1)).setString("");
	    
	    cs_.sendMsg(msg);
	    this.insert(size()-1,new StringItem("Me - "+time+":",null));
	    this.insert(size()-1,new StringItem(null,msg));
	}
	else
	    JimmyUI.jimmyCommand(c,d);
    }
}
