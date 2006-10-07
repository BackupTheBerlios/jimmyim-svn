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
 * File: jimmy/ui/About.java
 * Version: pre-alpha  Date: 2006/06/10
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */

package jimmy.ui;

import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;

import java.util.Hashtable;

public class About extends TextBox implements CommandListener{
    private JimmyUI ui_;
    private Hashtable commands_;
    
    /** Creates a new instance of About
     *  @param text text to be shown in about window
     */
    public About(String text) {
        super(Localization.tr("About:"),text,300,TextField.UNEDITABLE);//[LOCALE]
        ui_ = JimmyUI.getInstance();
        commands_ = ui_.getCommands();
          
        //add command
        try {
            setCommandListener(this);
        } catch(Exception e) {e.printStackTrace();}
        addCommand((Command)commands_.get(new Integer(JimmyUI.CMD_BACK)));
    }
    
    public void commandAction(Command c, Displayable d){
        JimmyUI.jimmyCommand(c,d);
    }
}
