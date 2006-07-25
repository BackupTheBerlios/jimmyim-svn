/**
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
 * File: jimmy/util/RMS.java
 * Version: pre-alpha  Date: 2006/06/05
 * Author(s): Zoran Mesec, Dejan Sakelsak, Matevz Jekovec
 * @version pre-alpha
 */

package jimmy.util;

import javax.microedition.rms.*;
import jimmy.Account;
import java.util.Vector;
import jimmy.util.Utils;

/**
 * Class Store handles persistent data storage in the mobile phone.
 * Record store is comprised of a number of records.
 * Jimmy uses the first record for general program properties and the next records for account properties (one account per record).
 * Values are separated by \n (newline) character.
 */
public class Store 
{
	private static final String RECORDSTORENAME_ = "JimmyIM";
	private static final String NEWLINE_ = "\n";
	private static RecordStore rs_;
	
	/**
	 * 
	 */
	public Store() {
	}
	
	private static boolean openStore() {
		try {
			rs_ = RecordStore.openRecordStore(RECORDSTORENAME_, true);	//createIfNecessary = true
			return true;
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	private static boolean closeStore() {
		try {
			rs_.closeRecordStore();
			return true;
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return false;
		}    
	}
	
	public static boolean addAccount(Account acc) {
		if (!openStore())
			return false;
		
		//build the String about to be written
		String out = new String();
		out = out.concat(String.valueOf(acc.getProtocolType()) + NEWLINE_);
		out = out.concat(acc.getUser() + NEWLINE_);
		out = out.concat(acc.getPassword() + NEWLINE_);
		out = out.concat(acc.getServer() + NEWLINE_);
		out = out.concat(String.valueOf(acc.getPort()) + NEWLINE_);
		out = out.concat((acc.getAutoLogin()?"1":"0") + NEWLINE_);	//1 - True, 0 - False
		
		//write the String
		boolean success = addRecord(out);
		
		closeStore();
		return success;
	}
	
	public static void removeAccount(Account acc) {
		if (!openStore())
			return;
		
		int i=1;
		try {
			while (i<rs_.getNumRecords()) {
				Account curr = createAccount(rs_.getRecord(i));
				if ( (curr.getProtocolType() == acc.getProtocolType()) &&
						(curr.getUser().compareTo(acc.getUser())==0) )
					rs_.setRecord(i, "-1".getBytes(), 0, 2);
			}
		} catch(RecordStoreException e) {
			e.printStackTrace();
		}
		
		closeStore();
	}
	
	/**
	 * Create an Account instance from the given byte[] record written in the record store.
	 * 
	 * @param byteRecord Byte array of the data written in Record store.
	 * @return Instance of the newly created Account from the given data. Returns null, if the account couldn't be generated.
	 */
	private static Account createAccount(byte[] byteRecord) {
		String record = new String(byteRecord);
		int idx = 0;
		
		//read Account type
		byte type = Byte.parseByte(record.substring(idx, (idx = record.indexOf("\n", idx)) + 1));
		if (type == -1)
			return null;
		idx++;	//newline
		
		//read username
		String userName = record.substring(idx, (idx = record.indexOf("\n", idx)) + 1);
		idx++;	//newline
		
		//read password
		String password = record.substring(idx, (idx = record.indexOf("\n", idx)) + 1);
		idx++;	//newline
		
		//read server name (optional)
		String server = record.substring(idx, (idx = record.indexOf("\n", idx)) + 1);
		idx++;	//newline
		
		//read server port (optional)
		int port = Integer.parseInt(record.substring(idx, (idx = record.indexOf("\n", idx)) + 1));
		idx++;	//newline
		boolean autoLogin = ((record.substring(idx, (idx = record.indexOf("\n", idx)) + 1).compareTo("0")==0)?false:true);
		
		return new Account(userName, password, type, server, port, autoLogin);
	}
	
	/**
	 * Add a byte array to the private Record Store of this class (instance).
	 * The record data is supplied as a parameter.
	 * 
	 * @param record byte array is going to be created from this string and inserted into a private Record Store 
	 * of this class(instance).
	 */
	private static boolean addRecord(String record){
		try {
			rs_.addRecord(record.getBytes(), 0, record.length());
			return true;
		} catch (RecordStoreException ex) {
			ex.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Return a list of Accounts (see jimmy.Account) from the private record store 
	 * of this class (instance) or null, if error occured.
	 * 
	 * @return Account Vector (see jimmy.Account) or null if error occured.
	 * @see jimmy.Account
	 */
	public static Vector getAccounts() {
		if (!openStore())
			return null;
		
		Vector accList = new Vector();
		
		try {
			for (int i=1; i<rs_.getNumRecords(); i++) {
				Account curr = createAccount(rs_.getRecord(i));
				if (curr!=null)
					accList.addElement(curr);
			}
		} catch (RecordStoreException ex) {
			ex.printStackTrace();
			return null;
		}
		
		closeStore();
		return accList;
	}
}