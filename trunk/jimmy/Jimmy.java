/* JIMMY - Instant Mobile Messenger
   Copyright (C) 2006  JIMMY Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **********************************************************************
 File: jimmy/Jimmy.java
 Version: pre-alpha  Date: 2006/04/08
 Author(s): Matev? Jekovec, Zoran Mesec, Dejan Sakel?ak, Janez Urevc
 */

package jimmy;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

public class Jimmy extends MIDlet {
	public static Jimmy jimmy; //Application main object
	public static String VERSION; //JIMMY version
	public static Display display; //Display object

	public static JimmyUI ui; //User Interface object
	public static Splash splash; //Splash screen object
	public static MainMenu mainMenu; //Main menu object
	
	protected void startApp() throws MIDletStateChangeException {
		Jimmy.VERSION = "pre-alpha";
		Jimmy.jimmy = this;
		
		Jimmy.ui = new JimmyUI();
	}

	protected void pauseApp() {
		// TODO Auto-generated method stub

	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException {
		// TODO Auto-generated method stub

	}
}
