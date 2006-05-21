/*
 * MainMenu.java
 *
 * Created on May 20, 2006, 11:33 AM
 */
package jimmy;

import javax.microedition.lcdui.*;

/**
 *
 * @author  slashrsm
 * @version
 */
public class MainMenu extends List implements CommandListener {
    private Account[] al;   //list of accounts
    private Command exit;
    private Command login;
    private Command newAcc;
    private Command about;
    /**
     * constructor
     */
    public MainMenu(Account[] a){
        super("Accounts:",List.IMPLICIT);
        
        try {
            setCommandListener(this);
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        //Commands in Main menu
        exit    = new Command("Exit",      Command.EXIT,   1);
        login   = new Command("Login",     Command.ITEM,   1);
        newAcc  = new Command("New account",Command.ITEM,  1);
        about   = new Command("About",     Command.ITEM,   1);
        addCommand(exit);
        addCommand(login);
        addCommand(newAcc);
        addCommand(about);
        
        this.al = a;
        addAccountsToMenu();        
    }
    
    public void setAccountList(Account[] a){
        this.al = a;
        addAccountsToMenu();
    }
    
    private void addAccountsToMenu(){
        this.deleteAll();
        for(int i=0; i<al.length; i++){
            this.append(al[i].getUser()+"@"+al[i].getServer(),null);
        }
    }
    /**
     * paint
     */
    public void paint(Graphics g) {
        
    }
    
    /**
     * Called when a key is pressed.
     */
    protected  void keyPressed(int keyCode) {
    }
    
    /**
     * Called when a key is released.
     */
    protected  void keyReleased(int keyCode) {
    }
    
    /**
     * Called when a key is repeated (held down).
     */
    protected  void keyRepeated(int keyCode) {
    }
    
    /**
     * Called when the pointer is dragged.
     */
    protected  void pointerDragged(int x, int y) {
    }
    
    /**
     * Called when the pointer is pressed.
     */
    protected  void pointerPressed(int x, int y) {
    }
    
    /**
     * Called when the pointer is released.
     */
    protected  void pointerReleased(int x, int y) {
    }
    
    /**
     * Called when action should be handled
     */
    public void commandAction(Command c, Displayable d) {
        if(c==exit)
            Jimmy.getInstance().exitJimmy();
    }
}
