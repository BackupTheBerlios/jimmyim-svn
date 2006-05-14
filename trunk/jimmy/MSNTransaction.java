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
 *
 * @author Zoran Mesec
 */
public class MSNTransaction 
{
    private String messageType; 
    private int trID;   //stands for transaction ID
    private Vector arguments;
    /**
     * Creates a new instance of MSNTransaction
     */
    public MSNTransaction() 
    {
        this.trID = 1;
        this.arguments = new Vector();
    }
    
    public void setType(String type)
    {
        this.messageType = type;
    }
    public void addArgument(String arg)
    {
     
        this.arguments.addElement(arg);
    }
    public void newTransaction()
    {
        this.trID++;
        this.arguments.removeAllElements();
        this.messageType = null;
    }
    public String toString()
    {
        StringBuffer rMessage = new StringBuffer();
        rMessage.append(this.messageType);
        rMessage.append(" " + this.trID);
        
        for(int i=0; i<this.arguments.size(); i++)
        {
            rMessage.append(" " + this.arguments.elementAt(i));
        }   
        rMessage.append("\r\n");
        return rMessage.toString();
    }
    
    public String getLogoutString()
    {
        return "OUT";
    }
    public int getTransactionID()
    {
        return this.trID;
    }   
    
    public static void main(String[] args)
    {
        MSNTransaction test = new MSNTransaction();
        test.setType("VER");
        test.addArgument("MSNP8");
        test.addArgument("CVR0");
        System.out.println(test.toString());
    }
}
