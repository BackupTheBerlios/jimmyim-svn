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
 * File: jimmy/ui/Splash.java
 * Version: pre-alpha  Date: 2006/06/05
 * Author(s): Janez Urevc
 * @author Janez Urevc
 * @version pre-alpha
 */

package jimmy.ui;

import javax.microedition.lcdui.*;

public class Splash extends Canvas {
        private Image splash_;
        private String mess_;
    
        /**
         * This constructor creates instance of Splash. It loades Jimmy logo and shows
         * it at the splash screen. It also has string for messaga wich can be changed 
         * any time.
         */
        public Splash(){
            try{
                splash_ = Image.createImage("/jimmy/JimmyIM_splash.png");
                mess_ = "Loading...";
            } catch(Exception e){
                System.out.println(e.getMessage());
                //System.out.println("Ex thrown");                
            };
            //this.append(splash_);
        } 
        
        public void paint(Graphics g){
            g.drawImage(splash_,0,0,Graphics.TOP|Graphics.LEFT);
            g.setColor(192,208,44);
            //g.setFont(Font.getFont(Font.FACE_PROPORTIONAL,Font.SIZE_SMALL,Font.STYLE_PLAIN));
            g.drawString(mess_,120,260,Graphics.TOP|Graphics.HCENTER);
        }
        
        /**
         * Changes message and repaints splash screen.
         * @param m new message for splash screen
         */
        public void setMess(String m){
            this.mess_ = m;
            repaint();
        }
}
