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
 * Author(s): Zoran Mesec
 * @author Zoran Mesec
 * @version pre-alpha
 */

package jimmy.util;

import javax.microedition.rms.*;
import jimmy.Account;
import java.lang.*;

/**
 * Class RMS handles persistent data storage.
 */
public class Store 
{
    private RecordStore rs;
    /**
     * Creates a new instance of RMS. However this constructor method does nothing. You 
     * need to set a private Record Store instance of this class with the method 
     * openStore(String RSName). It is advisable to call this method after the call of 
     * constructor method.
     * @see method openStore(String RSName)
     */
    public Store() 
    {

    }

    /**
     * The constructor method opens a record store according to the parameter and stores 
     * instance of this record store in a private variable rs (stands for RecordStore).
     * If you are not sure what are you doing, use the other constructor method.
     * @param RSname 
     */
    public Store(String RSname) 
    {
        try {
            this.rs = RecordStore.openRecordStore(RSname, true);
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }    
    /**
     * Retrieves all Record Stores (RS) and returns them in a String array. Please note that you have to filter 
     * RS to retrieve Jimmy Record stores.
     * @return Names of record stores in a string array (String[]).
     */
    public String[] getStores()
    {
        return RecordStore.listRecordStores();
    }
    /**
     * This method deletes a store from persistent data storage.
     * @param RSName Name of the Record Store to be deleted.
     */
    public void deleteStore(String RSName)
    {
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
     */
    public boolean openStore(String RSName)
    {
        try 
        {
            this.rs = RecordStore.openRecordStore(RSName,true);        
            return true;
        } catch (RecordStoreException ex) 
        {
            return false;
        }        
    }
    
    /**
     * Adds a byte array to the private Record Store of this class(instance). The record data is supplied as a parameter.
     * @param record byte array is going to be created from this string and inserted into a private Record Store 
     * of this class(instance).
     */
    public int addRecord(String record)
    {
        try 
        {
            return this.rs.addRecord(record.getBytes(), 0, record.length());
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
     * Deletes a record from the private Record Store of this class (instance). Index 
     * of the record to be deleted is supplied with parameter. Note that, when you 
     * delete a record, this record will remain empty forever, despite new insertions of records.
     * @param ind Index of the record to be deleted.
     */
    public void deleteRecord(int ind)
    {
        try {
            this.rs.deleteRecord(ind);
        } catch (InvalidRecordIDException ex) {
            ex.printStackTrace();
        } catch (RecordStoreNotOpenException ex) {
            ex.printStackTrace();
        } catch (RecordStoreException ex) {
            ex.printStackTrace();
        }
    }
        
    /**
     * This method returns an array of Accounts(see jimmy.Account) from the private recordstore 
     * of this class (instance) or null if error occured.
     * @return Account array(see jimmy.Account) or null if error occured.
     * @see jimmy.Account
     */
    public Account[] getAccounts()
    {
        try 
        {      
            Account[] accounts = new Account[this.rs.getNumRecords()];
            RecordEnumeration rEnum = this.rs.enumerateRecords(null,null,true);
            
            int count = 0, pos1 = 0, pos2 = 0;
            String data = null;
            
            //read a program version by which the configuration file was written
            String version = null;
            if (rEnum.hasNextElement()) {
                data = new String(rEnum.nextRecord());
                pos1 = data.indexOf("\n");
                version = data.substring(0,pos1); //version
            }
            
            if (version=="pre-alpha") {
            	while (rEnum.hasNextElement()) {
            		data = new String(rEnum.nextRecord());
            		pos1 = 0;
            		
            		pos2 = data.indexOf("\n");
            		boolean autoLogin = (Byte.parseByte(data.substring(0,pos2))==0)?false:true; //auto login
            		
            		pos1 = data.indexOf("\n",pos2+1);
            		byte protocol = Byte.parseByte(data.substring(pos2+1,pos1));  //protocol code
            		
            		pos2 = data.indexOf("\n",pos1+1);
            		String user = data.substring(pos1+1,pos2);  //username
            		
            		pos1 = data.indexOf("\n",pos2+1);
            		String password = data.substring(pos2+1,pos1); //password
            		
            		pos2 = data.indexOf("\n",pos1+1);
            		String server = data.substring(pos1+1,pos2); //server
            		
            		pos1 = data.indexOf("\n",pos2+1);
            		int port = Integer.parseInt(data.substring(pos2+1,pos1)); //port
            		
            		accounts[count] = new Account(user,password,protocol,server,port);
            		accounts[count].setAutoLogin(autoLogin);
            		
            		count++;
            	}
            	rEnum.destroy();
            	return accounts;
            }
        } catch (RecordStoreNotOpenException ex) 
        {
        	//ex.printStackTrace();
        	return null;
        } catch(RecordStoreException e)
        {
        	return null;
        }
    }
    /**
     * This method returns a number of records in the private Record Store of this class
     * (instance). Each record represents one account(e.q. MSN account with a username )
     * and a password).
     * @return The number of accounts. If there are no accounts, the return value is 0,
     */
    public int NumAccounts()
    {
        try 
        {
            return this.rs.getNumRecords();
        } catch (RecordStoreNotOpenException ex) 
        {
            ex.printStackTrace();
            return 0;
        }
    }    
}
