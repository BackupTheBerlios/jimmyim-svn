/*
 * Jimmy.java
 *
 * Created on May 20, 2006, 2:26 AM
 */
package jimmy;

<<<<<<< .mine
import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;
=======
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import java.io.IOException;
import java.util.Vector;
>>>>>>> .r114

<<<<<<< .mine
/**
 *
 * @author  slashrsm
 * @version
 */
=======
import jimmy.jabber.JabberProtocol;
import jimmy.msn.MSNProtocol;
import jimmy.ui.JimmyUI;
import jimmy.ui.MainMenu;
import jimmy.ui.Splash;

>>>>>>> .r114
public class Jimmy extends MIDlet {
<<<<<<< .mine
    private JimmyUI gui;
    private Display d;
    private static Jimmy jimmy;
=======
	public static Jimmy jimmy_; //Application main object
	final public static String VERSION	=	"pre-alpha"; //JIMMY version
	public static Display display_; //Display object
	private static Vector protocolList_; //List of active protocols 

	//User interface stuff:
	public static JimmyUI ui_; //User Interface object
	public static Splash splash_; //Splash screen object
	public static MainMenu mainMenu_; //Main menu object
	
	protected void startApp() throws MIDletStateChangeException {
		jimmy_ = this;
		
		ui_ = new JimmyUI();
		
		protocolList_.addElement(new MSNProtocol());
		protocolList_.addElement(new JabberProtocol());
		
		((Protocol)protocolList_.elementAt(0)).login("avgustin.ocepek@yahoo.com.au","0c3p3k");
		((Protocol)protocolList_.elementAt(1)).login("jimmy@gristle.org","jimmy");
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}
>>>>>>> .r114
    
    public Jimmy(){
        jimmy = this;
        d = Display.getDisplay(this);
        Account[] bla = new Account[4];
        bla[0] = new Account("slashrsm","kldsjf","jabber.org");
        bla[1] = new Account("marusa","sdfkj","gristle.org");
        bla[2] = new Account("mitko","lsjkdf","msn.com");
        bla[3] = new Account("sahel","sdjkf","jabber.org");
        
        gui = new JimmyUI(bla); 
    }
        
    public void startApp() {
   
    }
    
<<<<<<< .mine
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
    public static Jimmy getInstance(){
        return jimmy;
    }
    
    public void exitJimmy(){
        d.setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }
    
    public void setDisplay(Displayable disp){
        this.d.setCurrent(disp);
    }
=======
        public void exitJimmy(){
            display_.setCurrent(null);
            
           	try {
				destroyApp(true);
			} catch (MIDletStateChangeException e) {
				e.printStackTrace();
			}
			
            notifyDestroyed();
        }        
>>>>>>> .r114
}
