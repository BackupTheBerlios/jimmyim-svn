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
 File: jimmy/JimmyUI.java

 Author(s): Matevz Jekovec
*/

package jimmy;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import java.util.*;


public class JimmyUI implements CommandListener {
	//Commands codes:
	final public static int CMD_OK     = 1;
	final public static int CMD_CANCEL = 2;
	final public static int CMD_YES    = 3;
	final public static int CMD_NO     = 4;
	final public static int CMD_FIND   = 5;
	final public static int CMD_BACK   = 6;

	//Commands:
	final private static Command cmdOk     = new Command("OK",     Command.OK,     1);
	final private static Command cmdCancel = new Command("Cancel", Command.BACK,   2);
	final private static Command cmdYes    = new Command("Yes",    Command.OK,     1);
	final private static Command cmdNo     = new Command("No",     Command.CANCEL, 2);
	final private static Command cmdFind   = new Command("Find",   Command.OK,     1);
	final private static Command cmdBack   = new Command("Back",   Command.BACK,   2);
	
	static private Hashtable commands = new Hashtable(); //commands list
	static private Displayable lastDisplayable; //displayable object
	static private JimmyUI jimmyUI; //JimmyUI object
	
//	 Associate commands and commands codes
	static
	{
		commands.put(cmdOk,     new Integer(CMD_OK)    );
		commands.put(cmdCancel, new Integer(CMD_CANCEL));
		commands.put(cmdYes,    new Integer(CMD_YES)   );
		commands.put(cmdNo,     new Integer(CMD_NO)    );
		commands.put(cmdFind,   new Integer(CMD_FIND)  );
		commands.put(cmdBack,   new Integer(CMD_BACK)  );
	}
	
	JimmyUI() {
		jimmyUI = this;
	}
	
	public void commandAction(Command arg0, Displayable arg1) {
			
	}
}
