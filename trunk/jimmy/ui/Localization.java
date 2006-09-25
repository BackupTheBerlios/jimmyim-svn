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

import java.util.Hashtable;

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
	public static void setTranslation(String trCode) {
		hTable = new Hashtable();
		
	}
	
	private static Integer getHash(String in) {
		return new Integer(in.hashCode());
	}

	private static Integer getHash(String in, String comment) {
		return new Integer((in+comment).hashCode());
	}
	
	static private Hashtable hTable;
}
