/*
 * MSNTransaction.java
 *
 * Created on Petek, 2006, april 14, 13:49
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package jimmy;

import java.util.Vector;

/**
 * This class represents a single request (line) to be sent to the MSN server.
 * 
 * Because the statements are usually long and can get complicated, MSNTransaction is a bridge between MSNProtocol and raw ServerHandler.sendRequest().
 * MSNTransaction automatically stores the serial number of the transaction in trID_.
 * Typical use:
 * 1) MSNTransaction myTransaction = new MSNTransaction();
 * 2) myTransaction.newTransaction();
 * 3) myTransaction.setType("VER");
 * 4) myTransaction.addElement("MSNP11"); myTransaction.addElement("CVR0");
 * 5) serverHandler.sendRequest(myTransaction.toString());
 * 
 * This will send "VER 1 MSNP8 CVR0\r\n" to the server.
 *
 * @author Zoran Mesec
 * @author Matevz Jekovec
 */
public class MSNTransaction 
{
    private String messageType_;	//the first 3 characters which mark the message type
    private int trID_;   //serial number of the transaction
    private Vector arguments_;	//the arguments list
    private static final String NEWLINE_ = "\r\n";	//newline definition
    
    /**
     * Creates a new instance of MSNTransaction.
     * Transaction ID is by default 0! Call newTransaction() before making a transaction.
     */
    public MSNTransaction() 
    {
        this.trID_ = 0;
        this.arguments_ = new Vector();
    }
    
    /**
     * Sets the type - the first three characters of the transaction (eg. VER, USR, CHG etc.)
     * 
     * @param type 3 characters long message type
     */
    public void setType(String type)
    {
        this.messageType_ = type;
    }
    
    /**
     * Adds another argument to the message and automatically append " ".
     * 
     * @param arg New argument
     */
    public void addArgument(String arg)
    {
     
        this.arguments_.addElement(arg);
    }
    
    /**
     * Blanks the arguments list and message type. Increase the serial number of the transaction by 1.
     * Call this method when sending a new request to the server.
     */
    public void newTransaction()
    {
        this.trID_++;
        this.arguments_.removeAllElements();
        this.messageType_ = null;
    }
    
    /**
     * Concatenates the message type, serial number, a list of arguments separated by a blank space and adds new line characters.
     * @return Message in String ready to be sent directly to MSN server.
     */
    public String toString()
    {
        StringBuffer rMessage = new StringBuffer();
        rMessage.append(this.messageType_);
        rMessage.append(" " + this.trID_);
        
        for(int i=0; i<this.arguments_.size(); i++)
        {
            rMessage.append(" " + this.arguments_.elementAt(i));
        }   
        rMessage.append(NEWLINE_);

        return rMessage.toString();
    }
    
    /**
     * Logout is an exception among all the requests. It doesn't need the transaction ID beside.
     * @return Logout request ready to be sent directly to the MSN Server.
     */
    public String getLogoutString()
    {
        return "OUT" + NEWLINE_;
    }
    
    /**
     * MSNTransaction stores the current transaction ID locally. If you need it in any case externally, call this function.
     * @return Current transaction ID
     */
    public int getTransactionID()
    {
        return this.trID_;
    }   
}
