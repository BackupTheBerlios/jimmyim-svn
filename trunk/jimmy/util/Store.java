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
 * Class RMS handles persistent data storage.
 */
public class Store 
{
    private RecordStore acc;
    private RecordStore settings;
    /**
     * Opens the existing record stores "Jimmy" and "Accounts"
     */
    public Store(){
                    try{
                            this.acc = RecordStore.openRecordStore("Accounts",true);
                            this.settings = RecordStore.openRecordStore("Jimmy",true);
                    }catch(RecordStoreException e){
                            e.printStackTrace();
                    }
    }

    /**
     * Returns all record store names in a String[]
     * @return Names of record stores in a string array (String[]).
     */
    public String[] getStores(){
        return RecordStore.listRecordStores();
    }
    
    /**
     * This method deletes a store from persistent data storage.
     * @param RSName Name of the Record Store to be deleted.
     * @deprecated
     */
    public void deleteStore(String RSName){
        try {
            RecordStore.deleteRecordStore(RSName);
        } catch (RecordStoreException ex) {
            //ex.printStackTrace();
        }
    }
    
    /**
     * Opens a store with the name given with parameter RSName. Returns true for a successful opening
     * or false if store with that name already exists.
     * @param RSName The name of the store to be open.
     * @return True for a successful opening
     * or false if store with that name already exists.
     * @deprecated
     */
    public boolean openStore(String RSName){
        try{
            this.acc = RecordStore.openRecordStore(RSName,true);        
            return true;
        } catch (RecordStoreException ex){
            return false;
        }        
    }
    
    /**
     * Adds a byte array to the private Record Store of this class(instance). The record data is supplied as a parameter.
     * @param record byte array is going to be created from this string and inserted into a private Record Store 
     * of this class(instance).
     */
    public int addRecord(String record){
        try{
            return this.acc.addRecord(record.getBytes(), 0, record.length());
        } catch (RecordStoreNotOpenException ex)
        {
            //ex.printStackTrace();
        } catch (RecordStoreException ex) 
        {
            //ex.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * 
     * @param a
     * @return
     */
    public boolean addAccount(Account a) {
            System.out.println("----- Begin data to be saved -----");
            System.out.println("User: "+a.getUser()+"\nPass: "+a.getPassword()+"\nProtocol: "+a.getProtocolType());
            System.out.println("----- Begin data to be saved -----");

            String out = new String();
            String nl = "\n";
            out = out.concat(String.valueOf(a.getProtocolType()) + nl);
            out = out.concat(a.getUser() + nl);
            out = out.concat(a.getPassword() + nl);
            out = out.concat(a.getServer() + nl);
            out = out.concat(String.valueOf(a.getPort()) + nl);
            out = out.concat((a.getAutoLogin()?"1":"0") + nl);	//1 - True, 0 - False
    	
            System.out.println("---------Begin data saved in RS---------");
            System.out.println(out);
            System.out.println("---------End data saved in RS---------");
            
            try {
            	this.acc.addRecord(out.getBytes(), 0, out.length());
            } catch (RecordStoreNotOpenException e) {
            	e.printStackTrace();
            	return false;
            } catch (RecordStoreException e) {
            	e.printStackTrace();
            	return false;
            }
            
            return true;
    }
    
    /**
     * Deletes a record from the private Record Store of this class (instance). Index 
     * of the record to be deleted is supplied with parameter. Note that, when you 
     * delete a record, this record will remain empty forever, despite new insertions of records.
     * @param ind Index of the record to be deleted.
     */
    public void deleteRecord(int ind){
        try {
            this.acc.deleteRecord(ind);
        } catch (InvalidRecordIDException ex) {
            ex.printStackTrace();
        } catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }
        
    /**
     * This method returns a list of Accounts(see jimmy.Account) from the private recordstore 
     * of this class (instance) or null if error occured.
     * 
     * @return Account Vector (see jimmy.Account) or null if error occured.
     * @see jimmy.Account
     */
    public Vector getAccounts() {
            try {
                    RecordEnumeration re = this.acc.enumerateRecords(null, null, true);
                    if (re.hasNextElement()) { //first record contains the general program settings, second and on are the accounts records
    			re.nextRecord();
                    }

                    Vector accList = new Vector();
                    String curRecord;
                    while (re.hasNextElement()) {
                            curRecord = new String(re.nextRecord());
                            
                            int idx = 0;
                            //read Account type
    			byte type = Byte.parseByte(curRecord.substring(idx, ++idx));
                            idx++;	//newline
    			//read username
    			String userName = curRecord.substring(idx, idx = curRecord.indexOf("\n", idx));
                        System.out.println(userName);
                            idx++;	//newline
    			//read password
    			String password = curRecord.substring(idx, idx = curRecord.indexOf("\n", idx));
                            idx++;	//newline
    			//read server name (optional)
    			String server = curRecord.substring(idx, idx = curRecord.indexOf("\n", idx));
                            idx++;	//newline
    			//read server port (optional)
    			int port = Integer.parseInt(curRecord.substring(idx, idx = curRecord.indexOf("\n", idx)));
                            idx++;	//newline
    			boolean autoLogin = (curRecord.substring(idx, idx = curRecord.indexOf("\n", idx)).compareTo("0")==0?false:true);
                            
                            accList.addElement(new Account(userName, password, type, server, port, autoLogin));
                    }

                    return accList;
                    
            } catch (RecordStoreNotOpenException ex) {
                    ex.printStackTrace();
                return null;
        } catch(RecordStoreException ex){
                ex.printStackTrace();
                return null;
        }
    }
    
    
    public boolean close(){
        try{
            this.acc.closeRecordStore();
            this.settings.closeRecordStore();
        }catch(RecordStoreException ex){
            ex.printStackTrace();
            return false;
        }
        return true;
    }
}