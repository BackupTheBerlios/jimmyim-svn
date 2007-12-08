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
 * File: jimmy/ui/Localization.java
 * Version: pre-alpha  Date: 2006/09/25
 * Author(s): Matevz Jekovec
 * @author Matevz Jekovec
 * @version pre-alpha
 */

package jimmy.ui;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

public class Localization {
	public static String tr(String in) {
		if (hTable != null) {
			if (hTable.containsKey(getHash(in)))
				return (String)hTable.get(getHash(in));
		}

		return in;
	}

	public static String tr(String in, String comment) {
		if (hTable != null) {
			if (hTable.containsKey(getHash(in, comment)))
				return (String)hTable.get(getHash(in, comment));
		}
		
		return in;
	}


	/**
	 * Opens a translation code's .po file. The first two characters of the translation code tell the file name (eg. sl.po for sl_SI).
	 * Initializes the class to be used by calling tr() functions then.
	 * 
	 * @param trCode Translation code: sl_SI for Slovenian, en_US for USA's English, it_IT for Italian.
	 */
	private final static int COMMENT = 3;
	private final static int MSGSTR = 2;
	private final static int MSGID = 1;
	private final static int IDLE = 0;
	
	public static void setTranslation(String trCode, MIDlet midlet) { 
            InputStream is = midlet.getClass().getResourceAsStream("po/" + trCode.substring(0,2) + ".po");	//open a file named the first two characters of the trCode and append a suffix .po
            hTable = new Hashtable();
            try {
                is.available();
                InputStreamReader stream = new InputStreamReader(is, "UTF-8");
                String key = new String();
                String value = new String();
                StringBuffer line;
                String tmpString;
                int status;
                while(true) {
                    line = new StringBuffer();
                    readLine(line,stream);  //throws exception
                    tmpString = line.toString();

                    if(tmpString.startsWith("#")) status=COMMENT;
                    else if(tmpString.startsWith("msgid")) status=MSGID;
                    else if(tmpString.startsWith("msgstr")) status=MSGSTR;
                    else status=IDLE;  
                                             
                    switch (status) {
                        case MSGID: //message id
                            key=parseMsgIdString(tmpString);
                            break;
                        case MSGSTR: //message string
                            value=parseMsgStrString(tmpString);
                            hTable.put(getHash(key.toString()), value.toString());
                            break;
                        case COMMENT: //comment
                            
                            break;
                        case IDLE: //empty line
                            
                            break;
                    }

                }
            } catch (Exception e) {
                   return;
            }
	}
        
        public static void readLine(StringBuffer line, InputStreamReader stream) throws Exception
        {
            char c;
            int ch;
            try {
                while (true) {
                      //c = (char) stream.read();
                      ch=stream.read();
                      //Read key until end of the line is reached
                      if(ch==-1) throw new Exception();
                      c=(char)ch;
                      if (c == '\n'){
                             break;
                      }
                      line.append(c);
                }
            } catch (Exception e) {
                    //System.out.println("THROW EXCEPTION!!!");
                   throw new Exception();
                //return -1;
                }
            
            //System.out.println("LINE:"+line);
            
 
            

            /*while (true) {
                try {
                  c = (char) stream.read();
                  if (c == '\n'){
                         break;
                  }

                  //Read value until end of line
                  value.append(c);
                } catch (Exception e) {
                    return 0;
                }
            }*/
           
        }
	
        private static String parseMsgIdString(String key) {
            return key.substring(7,key.length()-1);
        }
        
        private static String parseMsgStrString(String value) {
            return value.substring(8,value.length()-1);
        }
        
	private static Integer getHash(String in) {
		return new Integer(in.hashCode());
	}

	private static Integer getHash(String in, String comment) {
		return new Integer((in+comment).hashCode());
	}
	
	static private Hashtable hTable;
}
