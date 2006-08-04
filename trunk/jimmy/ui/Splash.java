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
        private Image image_;
        private String mess_;
    
        /**
         * This constructor creates instance of Splash. It loades Jimmy logo and shows
         * it at the splash screen. It also has string for messaga wich can be changed 
         * any time.
         */
        public Splash(){
            try{
                double screenWidth = this.getWidth()*0.9;
                double screenHeight = this.getHeight();
                
                image_ = Image.createImage("/jimmy/JimmyIM_splash_small.png");
                mess_ = "Loading...";
                
                double imageWidth = image_.getWidth();
                double imageHeight = image_.getHeight();
                double size = (screenWidth/imageWidth < screenHeight/imageHeight) ? screenWidth/imageWidth:screenHeight/imageHeight;
                
                if(size < 1.0)
                    image_ = this.resizeImage(image_,size);
            } catch(Exception e){
                System.out.println(e.getMessage());
                //System.out.println("Ex thrown");                
            };
            //this.append(splash_);
        } 
        
        public void paint(Graphics g){
            //Draw rectangle for background
            g.setColor(77,80,92);
            g.fillRect(0,0,getWidth(),getHeight());

            //Draw splash image
            g.drawImage(image_,getWidth()/2,5*getHeight()/12,Graphics.HCENTER|Graphics.VCENTER);

            //Draw status msg
            g.setColor(192,208,44);
            g.drawString(mess_,getWidth()/2,5*getHeight()/6,Graphics.HCENTER|Graphics.TOP);
        }
        
        /**
         * Changes message and repaints splash screen.
         * @param m new message for splash screen
         */
        public void setMess(String m){
            this.mess_ = m;
            repaint();
        }
        
        private Image resizeImage(Image img, double size){
            int coord1 = 0, coord2 = 0;
            //double skip = 1/size;
            int inWidth = img.getWidth();
            int inHeight = img.getHeight();
            int outWidth = (int)(inWidth * size);
            int outHeight = (int)(inHeight * size);
            int[] in = new int[inWidth*inHeight];
            int[] out = new int[outWidth*outHeight];
            img.getRGB(in,0,inWidth,0,0,inWidth,inHeight); 
            
            for(int i=0; i<outHeight; i++){
                    for(int j=0; j<outWidth; j++){
                        coord1 = i*outWidth+j;
                        coord2 = (int)(i/size)*inWidth+(int)(j/size);
                        out[coord1] = in[coord2];
                    }
            }
            
            return Image.createRGBImage(out,outWidth,outHeight,true);
        }
}
