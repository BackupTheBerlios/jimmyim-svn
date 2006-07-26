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
	 * Default constructor. 
	 */
	public Store() {
	}
	
	/**
	 * Open the record store for writing.
	 * This method is called automatically on every write/read.
	 * It shouldn't be called from outside.
	 * 
	 * @return True, if the record store was successfully open, false otherwise.
	 */
	private static boolean openStore() {
		try {
			rs_ = RecordStore.openRecordStore(RECORDSTORENAME_, true);	//createIfNecessary = true
			if (rs_.getNumRecords()==0)
				rs_.addRecord("".getBytes(), 0, 0);

			return true;
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Close the record store.
	 * This method is called automatically on every write/read.
	 * It shouldn't be called from outside.
	 * 
	 * @return True, if the record store was successfully closed, false otherwise.
	 */
	private static boolean closeStore() {
		try {
			rs_.closeRecordStore();
			return true;
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return false;
		}    
	}
	
	/**
	 * Create a new record for the given Account in the record store.
	 *  
	 * @param acc Account to be added.
	 * @return True, if the record was successfully created, false otherwise.
	 */
	public static boolean addAccount(Account acc) {
		if (!openStore())
			return false;
		
		//write the String
		boolean success = addRecord(createString(acc));
		
		closeStore();
		return success;
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
	 * Create the String, which should be written in the store for the given Account.
	 * 
	 * @param acc Account which the properties should be written.
	 * @return String which should be written directly into the record.
	 */
	private static String createString(Account acc) {
		String out = new String();
		out = out.concat(String.valueOf(acc.getProtocolType()) + NEWLINE_);
		out = out.concat(acc.getUser() + NEWLINE_);
		out = out.concat(acc.getPassword() + NEWLINE_);
		out = out.concat(acc.getServer() + NEWLINE_);
		out = out.concat(String.valueOf(acc.getPort()) + NEWLINE_);
		out = out.concat((acc.getAutoLogin()?"1":"0") + NEWLINE_);	//1 - True, 0 - False
		
		return out;
	}
	
	/**
	 * Remove the account from the record store.
	 * WARNING! This method only marks the record as a type -1, which means the record should be deleted the next time record store gets rebuilt.
	 * It doesn't actually remove the record from the record store.
	 * 
	 * @param acc Account which should be removed.
	 */
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
	 * Change the record of an already written Account.
	 * 
	 * @param oldType Protocol of the old account in the record store.
	 * @param oldName Username of the old account in the record store.
	 * @param newAccount New Account, which should replace the one in the record store.
	 * @return True, if old account has been found and successfully replaced, false otherwise.
	 */
	public static boolean changeAccount(byte oldType, String oldName, Account newAccount) {
		if (!openStore())
			return false;
		
		int i=1;
		boolean success = false;
		try {
			while (i<rs_.getNumRecords()) {
				Account curr = createAccount(rs_.getRecord(i));
				if ( (curr.getProtocolType() == oldType) &&
				     (curr.getUser().compareTo(oldName)==0) ) {
					String out;
					rs_.setRecord(i, (out = createString(newAccount)).getBytes(), 0, out.length());
					success = true;
				}
			}
		} catch(RecordStoreException e) {
			e.printStackTrace();
		}
		
		closeStore();
		return success;
	}
	
	/**
	 * Write the general program settings.
	 * The settings are always stored in the first record of the record store.
	 * 
	 * @param config Array of string values to be written.
	 * @return True, if settings were successfully writtten, false otherwise.
	 */
	public static boolean writeSettings(String[] config) {
		if (!openStore())
			return false;
		
		//generate the output string
		String out = null;
		for (int i=0; i<config.length; i++)
			out = out.concat(config[i] + NEWLINE_);
		
		try {
			rs_.setRecord(0, out.getBytes(), 0, out.length());
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return false;
		}
		
		closeStore();
		return true;
	}
	
	/**
	 * Read general program settings from the first record in the record store.
	 * Every value has to finish with \n character.
	 * 
	 * @return String array of values, null, if error occured.
	 */
	public static String[] readSettings() {
		if (!openStore())
			return null;
		
		String in;
		try {
			in = new String(rs_.getRecord(0));
		} catch(RecordStoreException e) {
			e.printStackTrace();
			return null;
		}
		
		//scan the record string for the number of values
		int num = 0;
		for (int i=0; i<in.length(); i++)
			if (in.charAt(i) == '\n')
				num++;

		int idx = 0;
		int i=0;
		String[] out = new String[num];
		while (in.length()!=0) {
			out[i] = in.substring(idx, idx = (in.indexOf("\n")+1));
			in = in.substring(idx);
			i++;
		}
		
		closeStore();
		return out;
	}
	
	/**
	 * Add the given record to the record store.
	 * 
	 * @param record Record in String format going to be added.
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
	 * Return the list of Accounts (see jimmy.Account) found in the record store.
	 * 
	 * @return Vector of Accounts or null, if error occured.
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
