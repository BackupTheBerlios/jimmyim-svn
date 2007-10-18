/*
 * ChatWindow.java
 *
 * Created on Četrtek, 27 julij 2006, 2:45
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy.ui;
import java.util.Hashtable;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

import jimmy.ChatSession;
import jimmy.Contact;
import jimmy.util.Utils;

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
	super(Localization.tr("Chat with ")+user+":");//[LOCALE]
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
	cs_ = cs;
	tf_ = new TextField(Localization.tr("Message:"),"",200,TextField.ANY);//[LOCALE]
        
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}
        
        //add commands to menu
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_SEND) ));        
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK) ));
        
	//add text field
	this.append(tf_);
	
    }
    
    public void commandAction(Command c, Displayable d){
	if(c == (Command)commands_.get(new Integer(JimmyUI.CMD_SEND) )){
//	    String time = new String(Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND);
    String time = Utils.getCurrentTimestamp();
	    String msg = ((TextField)get(size()-1)).getString();
	    ((TextField)get(size()-1)).setString("");
	    
            cs_.sendMsg(msg);
	    this.insert(size()-1,new StringItem(Localization.tr("Me - ")+time+":",null));//[LOCALE]
	    this.insert(size()-1,new StringItem(null,msg));
	}
	else
	    JimmyUI.jimmyCommand(c,d);
    }
    
    public void msgRecieved(Contact c, String msg){
	    String name;
	    if(c.screenName() != null)
		name = c.screenName();
	    else
		name = c.userID();
	    
//	    String time = new String(Calendar.HOUR_OF_DAY+":"+Calendar.MINUTE+":"+Calendar.SECOND);
      String time = Utils.getCurrentTimestamp();
	    this.insert(size()-1,new StringItem(name+" - "+time+":",null));
	    this.insert(size()-1,new StringItem(null,msg));	    
    }
}
