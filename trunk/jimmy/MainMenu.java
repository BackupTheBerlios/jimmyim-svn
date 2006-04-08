/*
 * Created on Apr 8, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jimmy;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDletStateChangeException;

public class MainMenu implements CommandListener {
	public void commandAction(Command arg0, Displayable arg1) {
		// TODO Auto-generated method stub
		
	}

	private void doExit() {
		try {
			Jimmy.jimmy.destroyApp(true);
		} catch (MIDletStateChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
