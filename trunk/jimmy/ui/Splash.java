/*
 * Created on Apr 8, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package jimmy.ui;

import javax.microedition.lcdui.*;

public class Splash extends Canvas {
        private Image splash_;
    
        public Splash(){
            try{
                splash_ = Image.createImage("/home/slashrsm/JimmyIM/src/splash.png");
            } catch(Exception e){
                System.out.println(e.getMessage());
            };
        
        }
    
	protected void paint(Graphics g) {
            g.drawImage(splash_,10,10,Graphics.TOP);
	}

}
