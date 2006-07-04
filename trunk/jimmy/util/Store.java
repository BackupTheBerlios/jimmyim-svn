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
 * @author Dejan Sakelsak
 * @version pre-alpha
 */

package jimmy.util;

import javax.microedition.rms.*;
import jimmy.Account;
import java.lang.*;
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
    public int addAccount(Account a){
    		byte[] user = a.getUser().getBytes();
    		byte[] pass = a.getPassword().getBytes();
    		byte[] p = new byte[1];
                p[0] = a.getProtocolType();
    		byte[] s = a.getServer().getBytes();
    		byte[] port = Utils.intToBytes(a.getPort(),true);
    		boolean auto = a.getAutoLogin();
    		byte[] c = new byte[1];
                if(auto)
                    c[0] = 1;
                else
                    c[0] = 0;
                String n = "\n";
                int len = 5+user.length+pass.length+p.length+s.length+port.length+c.length;
                byte[] record = new byte[len];
                for(int i = 0; i<=user.length; i++){
                    if(i < user.length)
                        record[i] = user[i];
                    else if(i == user.length)
                        record[i] = n.getBytes()[0];
                }   
                for(int i = 0; i<=pass.length;i++){
                    if(i < pass.length)
                            record[i+user.length+1] = pass[i];
                    else if(i == user.length+pass.length)
                        record[i+user.length+1] = n.getBytes()[0];
                }   
                for(int i = 0; i<=p.length;i++){
                    if(i < p.length)
                        record[i+user.length+pass.length+1] = p[i];
                    else if(i == p.length)
                        record[i+user.length+pass.length+1] = n.getBytes()[0];
                }
                for(int i = 0; i<=s.length;i++){
                    if(i < s.length)
                        record[i+user.length+pass.length+p.length+1] = s[i];
                    else if(i == s.length)
                        record[i+user.length+pass.length+p.length+1] = n.getBytes()[0];
                }
                for(int i = 0; i<=port.length;i++){
                    if(i < port.length)
                        record[i+user.length+pass.length+p.length+s.length+1] = port[i];
                    else if(i == port.length)
                        record[i+user.length+pass.length+p.length+s.length+1] = n.getBytes()[0];
                }
                for(int i = 0; i<c.length;i++){
                    if(i < user.length+pass.length+p.length+s.length+port.length+c.length)
                        record[i+user.length+pass.length+p.length+port.length+1] = c[i];
                }
                System.out.println(new String(record));
    		try{
    		       this.acc.addRecord(record,0,record.length);
    		}catch(RecordStoreNotOpenException e){
    			e.printStackTrace();
    		}catch(RecordStoreException e){
    			e.printStackTrace();
    		}
    		return -1;
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
     * This method returns an array of Accounts(see jimmy.Account) from the private recordstore 
     * of this class (instance) or null if error occured.
     * @return Account array(see jimmy.Account) or null if error occured.
     * @see jimmy.Account
     */
    public Account[] getAccounts(){
    		
    		Account[] accounts = new Account[1];
                Account a =null;
         try{      
                RecordEnumeration re = this.acc.enumerateRecords(null,null,true);
                /*boolean b = false;
                if(this.acc.getRecord(5)[0] == (byte)1)
                    b = true;*/
                for(int i = 0; re.hasNextElement();i++){
                    String bla = new String(re.nextRecord());
                    int ind = bla.indexOf("\n");
                    String user = bla.substring(0,ind);
                    int ind2 = bla.indexOf("\n",ind+1);
                    String pass = bla.substring(ind+1,ind2);
                    ind = bla.indexOf("\n",ind2+1);
                    String type = bla.substring(ind2+1,ind);
                    ind2 = bla.indexOf("\n",ind+1);
                    String serv = bla.substring(ind+1,ind2);
                    ind = bla.indexOf("\n",ind2+1);
                    String port = bla.substring(ind2+1,ind);
                    ind2 = bla.indexOf("\n",ind+1);
                    String auto = bla.substring(ind+1,ind2);
                    
                    a = new Account(user,pass,type.getBytes()[0],serv,Integer.parseInt(port),Integer.parseInt(auto) == 1 ? true : false);
                    accounts[i] = a;
                }
        } catch (RecordStoreNotOpenException ex) {
        	ex.printStackTrace();
        	return null;
        } catch(RecordStoreException ex){
        	ex.printStackTrace();
                return null;
        }

        	return accounts;
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
    
    /**
     * This method returns a number of records in the private Record Store of this class
     * (instance). Each record represents one account(e.q. MSN account with a username )
     * and a password).
     * @return The number of accounts. If there are no accounts, the return value is 0,
     */
    public int NumAccounts(){
        try 
        {
            return this.acc.getNumRecords();
        } catch (RecordStoreNotOpenException ex) 
        {
            ex.printStackTrace();
            return 0;
        }
    }    
}
