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
import java.util.Hashtable;

import javax.microedition.midlet.MIDlet;

public class Localization {
	public static String tr(String in) {
		if (hTable != null) {
			if (hTable.contains(getHash(in)))
				return (String)hTable.get(getHash(in));
		}

		return in;
	}

	public static String tr(String in, String comment) {
		if (hTable != null) {
			if (hTable.contains(getHash(in, comment)))
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
		InputStream is = midlet.getClass().getResourceAsStream(trCode.substring(0,2) + ".po");	//open a file named the first two characters of the trCode and append a suffix .po
		
		try {	//try, if the file existed and was open
			is.available();
		} catch (Exception e) {
			return;	//otherwise, leave and don't create a hashtable
		}
		
		//file was successfully open, read the file and fill the hashtable
		hTable = new Hashtable();
		
		try {
			int curChar;
			String curMsgId = new String();
			String curMsgComment = new String();
			String curMsgStr = new String();
			String buffer = new String();
			boolean reading = false;
			boolean usedEscapeSeq = false;
			int curStat = IDLE;
			
			while ((curChar=is.read()) != -1) {
				String curStr = String.valueOf((char)curChar);
				if (((curStat != MSGSTR) || (curStat != MSGID)) && (curStr=="#"))
					curStat = COMMENT;
				
				if (curStat == COMMENT && curStr=="\n")
					curStat = IDLE;

				if (curStr=="\\") {	//escape sequence used
					curChar = is.read();
					curStr = curStr + String.valueOf(curChar);
					if (curStr == "\\n")	//newline
						curStr = "\n";
					else if (curStr == "\\\"")	//double quotes
						curStr = "\"";
					else if (curStr == "\\t")	//tab
						curStr = "\t";
					else if (curStr == "\\\\")	//backslash
						curStr = "\\";
					
					usedEscapeSeq = true;
				}
				
				if (curStat == IDLE && buffer=="msgid") {
					curStat = MSGID;
					
					if (curMsgStr!="") {
						if (curMsgStr.indexOf("_:") != -1) {	//expression has comment
							int idx1 = curMsgStr.indexOf("_:");
							int idx2 = curMsgStr.indexOf("\n", idx1+2);
							curMsgComment = curMsgStr.substring(idx1, idx2);
							curMsgStr = curMsgStr.substring(idx2+1);
						}
							
						hTable.put(getHash(curMsgId, curMsgComment), curMsgStr);
					}
					curMsgId = ""; curMsgComment = ""; curMsgStr = ""; buffer="";
				}

				if (curStat == IDLE && buffer=="msgstr")
					curStat = MSGSTR;
				
				if ((curStat== MSGID || curStat==MSGSTR) && curStr=="\"") {	//reading msgid or msgstr value
					if (reading && !usedEscapeSeq) {
						if (curStat == MSGID)
							curMsgId += buffer.substring(1);
						else if (curStat == MSGSTR)
							curMsgStr += buffer.substring(1);
						
						reading = false;
					} else if (!reading) {
						buffer = "";
						reading = true;
					}
					
				}
				
				if (!reading && curStr != "\n" && curStr != " " && curStr != "\t")
					buffer.concat(curStr);
				
				usedEscapeSeq = false;
			}
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private static Integer getHash(String in) {
		return new Integer(in.hashCode());
	}

	private static Integer getHash(String in, String comment) {
		return new Integer((in+comment).hashCode());
	}
	
	static private Hashtable hTable;
}
