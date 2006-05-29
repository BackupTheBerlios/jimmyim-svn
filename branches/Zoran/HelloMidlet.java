/*
 * HelloMidlet.java
 *
 * Created on Nedelja, 28 maj 2006, 23:59
 */

package hello;

import javax.microedition.midlet.*;
import javax.microedition.lcdui.*;

/**
 *
 * @author Zoran Mesec
 */
public class HelloMidlet extends MIDlet implements CommandListener {
    
    /** Creates a new instance of HelloMidlet */
    public HelloMidlet() {
    }
    
    private Form ContactsForm;//GEN-BEGIN:MVDFields
    private Command exitCommand;
    private Command OKCommand;
    private Image image1;
    private Ticker JIMMYTicker;
    private org.netbeans.microedition.lcdui.SplashScreen Splash;
    private Command CancelCommand;
    private Command BackCommand;
    private ChoiceGroup Group1;
    private List MainMenu;
    private Form SettingsForm;
    private Form AccountForm;
    private org.netbeans.microedition.lcdui.SplashScreen AboutScreen;
    private Font font1;
    private Gauge gauge1;
    private ChoiceGroup ProtocolSelect;
    private Form UPForm;
    private TextField username;
    private TextField password;
    private ChoiceGroup Group2;
    private org.netbeans.microedition.lcdui.WaitScreen Connecting;
    private Form ConversationForm;
    private Command ContactsCommand;
    private Command CloseSession;
    private TextField textField1;
    private StringItem stringItem1;
    private Gauge gauge2;
    private Alert Warning;
    private Image image2;//GEN-END:MVDFields
    
//GEN-LINE:MVDMethods

    /** This method initializes UI of the application.//GEN-BEGIN:MVDInitBegin
     */
    private void initialize() {//GEN-END:MVDInitBegin
        // Insert pre-init code here
        getDisplay().setCurrent(get_Splash());//GEN-LINE:MVDInitInit
        // Insert post-init code here
    }//GEN-LINE:MVDInitEnd
    
    /** Called by the system to indicate that a command has been invoked on a particular displayable.//GEN-BEGIN:MVDCABegin
     * @param command the Command that ws invoked
     * @param displayable the Displayable on which the command was invoked
     */
    public void commandAction(Command command, Displayable displayable) {//GEN-END:MVDCABegin
        // Insert global pre-action code here
        if (displayable == MainMenu) {//GEN-BEGIN:MVDCABody
            if (command == MainMenu.SELECT_COMMAND) {
                switch (get_MainMenu().getSelectedIndex()) {
                    case 0://GEN-END:MVDCABody
                        // Insert pre-action code here
                        getDisplay().setCurrent(get_Connecting());//GEN-LINE:MVDCAAction36
                        // Insert post-action code here
                        break;//GEN-BEGIN:MVDCACase36
                    case 2://GEN-END:MVDCACase36
                        // Insert pre-action code here
                        getDisplay().setCurrent(get_AccountForm());//GEN-LINE:MVDCAAction38
                        // Insert post-action code here
                        break;//GEN-BEGIN:MVDCACase38
                    case 1://GEN-END:MVDCACase38
                        // Insert pre-action code here
                        getDisplay().setCurrent(get_SettingsForm());//GEN-LINE:MVDCAAction40
                        // Insert post-action code here
                        break;//GEN-BEGIN:MVDCACase40
                    case 3://GEN-END:MVDCACase40
                        // Insert pre-action code here
                        getDisplay().setCurrent(get_AboutScreen());//GEN-LINE:MVDCAAction42
                        // Insert post-action code here
                        break;//GEN-BEGIN:MVDCACase42
                }
            } else if (command == exitCommand) {//GEN-END:MVDCACase42
                // Insert pre-action code here
                exitMIDlet();//GEN-LINE:MVDCAAction49
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase49
        } else if (displayable == SettingsForm) {
            if (command == OKCommand) {//GEN-END:MVDCACase49
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction48
                // Insert post-action code here
            } else if (command == CancelCommand) {//GEN-LINE:MVDCACase48
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction51
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase51
        } else if (displayable == AccountForm) {
            if (command == OKCommand) {//GEN-END:MVDCACase51
                // Insert pre-action code here
                getDisplay().setCurrent(get_UPForm());//GEN-LINE:MVDCAAction50
                // Insert post-action code here
            } else if (command == CancelCommand) {//GEN-LINE:MVDCACase50
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction59
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase59
        } else if (displayable == ContactsForm) {
            if (command == BackCommand) {//GEN-END:MVDCACase59
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction61
                // Insert post-action code here
            } else if (command == OKCommand) {//GEN-LINE:MVDCACase61
                // Insert pre-action code here
                getDisplay().setCurrent(get_ConversationForm());//GEN-LINE:MVDCAAction68
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase68
        } else if (displayable == UPForm) {
            if (command == exitCommand) {//GEN-END:MVDCACase68
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction63
                // Insert post-action code here
            } else if (command == BackCommand) {//GEN-LINE:MVDCACase63
                // Insert pre-action code here
                getDisplay().setCurrent(get_AccountForm());//GEN-LINE:MVDCAAction64
                // Insert post-action code here
            } else if (command == OKCommand) {//GEN-LINE:MVDCACase64
                // Insert pre-action code here
                getDisplay().setCurrent(get_MainMenu());//GEN-LINE:MVDCAAction65
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase65
        } else if (displayable == ConversationForm) {
            if (command == CloseSession) {//GEN-END:MVDCACase65
                // Insert pre-action code here
                getDisplay().setCurrent(get_ContactsForm());//GEN-LINE:MVDCAAction83
                // Insert post-action code here
            } else if (command == ContactsCommand) {//GEN-LINE:MVDCACase83
                // Insert pre-action code here
                getDisplay().setCurrent(get_ContactsForm());//GEN-LINE:MVDCAAction80
                // Insert post-action code here
            } else if (command == OKCommand) {//GEN-LINE:MVDCACase80
                // Insert pre-action code here
                getDisplay().setCurrent(get_ConversationForm());//GEN-LINE:MVDCAAction81
                // Insert post-action code here
            }//GEN-BEGIN:MVDCACase81
        }//GEN-END:MVDCACase81
        // Insert global post-action code here
}//GEN-LINE:MVDCAEnd
    
    /**
     * This method should return an instance of the display.
     */
    public Display getDisplay() {//GEN-FIRST:MVDGetDisplay
        return Display.getDisplay(this);
    }//GEN-LAST:MVDGetDisplay
    
    /**
     * This method should exit the midlet.
     */
    public void exitMIDlet() {//GEN-FIRST:MVDExitMidlet
        getDisplay().setCurrent(null);
        destroyApp(true);
        notifyDestroyed();
    }//GEN-LAST:MVDExitMidlet
    
    /** This method returns instance for ContactsForm component and should be called instead of accessing ContactsForm field directly.//GEN-BEGIN:MVDGetBegin2
     * @return Instance for ContactsForm component
     */
    public Form get_ContactsForm() {
        if (ContactsForm == null) {//GEN-END:MVDGetBegin2
            // Insert pre-init code here
            ContactsForm = new Form("Contacts", new Item[] {//GEN-BEGIN:MVDGetInit2
                get_Group1(),
                get_Group2()
            });
            ContactsForm.addCommand(get_BackCommand());
            ContactsForm.addCommand(get_OKCommand());
            ContactsForm.setCommandListener(this);//GEN-END:MVDGetInit2
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd2
        return ContactsForm;
    }//GEN-END:MVDGetEnd2
    
    
    /** This method returns instance for exitCommand component and should be called instead of accessing exitCommand field directly.//GEN-BEGIN:MVDGetBegin5
     * @return Instance for exitCommand component
     */
    public Command get_exitCommand() {
        if (exitCommand == null) {//GEN-END:MVDGetBegin5
            // Insert pre-init code here
            exitCommand = new Command("Exit", Command.EXIT, 1);//GEN-LINE:MVDGetInit5
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd5
        return exitCommand;
    }//GEN-END:MVDGetEnd5

    /** This method returns instance for OKCommand component and should be called instead of accessing OKCommand field directly.//GEN-BEGIN:MVDGetBegin6
     * @return Instance for OKCommand component
     */
    public Command get_OKCommand() {
        if (OKCommand == null) {//GEN-END:MVDGetBegin6
            // Insert pre-init code here
            OKCommand = new Command("Ok", Command.OK, 1);//GEN-LINE:MVDGetInit6
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd6
        return OKCommand;
    }//GEN-END:MVDGetEnd6

    /** This method returns instance for image1 component and should be called instead of accessing image1 field directly.//GEN-BEGIN:MVDGetBegin7
     * @return Instance for image1 component
     */
    public Image get_image1() {
        if (image1 == null) {//GEN-END:MVDGetBegin7
            // Insert pre-init code here
            try {//GEN-BEGIN:MVDGetInit7
                image1 = Image.createImage("<No Image>");
            } catch (java.io.IOException exception) {
            }//GEN-END:MVDGetInit7
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd7
        return image1;
    }//GEN-END:MVDGetEnd7

    /** This method returns instance for JIMMYTicker component and should be called instead of accessing JIMMYTicker field directly.//GEN-BEGIN:MVDGetBegin8
     * @return Instance for JIMMYTicker component
     */
    public Ticker get_JIMMYTicker() {
        if (JIMMYTicker == null) {//GEN-END:MVDGetBegin8
            // Insert pre-init code here
            JIMMYTicker = new Ticker("hehehe");//GEN-LINE:MVDGetInit8
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd8
        return JIMMYTicker;
    }//GEN-END:MVDGetEnd8

    /** This method returns instance for Splash component and should be called instead of accessing Splash field directly.//GEN-BEGIN:MVDGetBegin9
     * @return Instance for Splash component
     */
    public org.netbeans.microedition.lcdui.SplashScreen get_Splash() {
        if (Splash == null) {//GEN-END:MVDGetBegin9
            // Insert pre-init code here
            Splash = new org.netbeans.microedition.lcdui.SplashScreen(getDisplay());//GEN-BEGIN:MVDGetInit9
            Splash.setImage(get_image2());
            Splash.setNextDisplayable(get_MainMenu());//GEN-END:MVDGetInit9
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd9
        return Splash;
    }//GEN-END:MVDGetEnd9

    /** This method returns instance for CancelCommand component and should be called instead of accessing CancelCommand field directly.//GEN-BEGIN:MVDGetBegin11
     * @return Instance for CancelCommand component
     */
    public Command get_CancelCommand() {
        if (CancelCommand == null) {//GEN-END:MVDGetBegin11
            // Insert pre-init code here
            CancelCommand = new Command("Cancel", Command.CANCEL, 1);//GEN-LINE:MVDGetInit11
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd11
        return CancelCommand;
    }//GEN-END:MVDGetEnd11

    /** This method returns instance for BackCommand component and should be called instead of accessing BackCommand field directly.//GEN-BEGIN:MVDGetBegin13
     * @return Instance for BackCommand component
     */
    public Command get_BackCommand() {
        if (BackCommand == null) {//GEN-END:MVDGetBegin13
            // Insert pre-init code here
            BackCommand = new Command("Back", Command.BACK, 1);//GEN-LINE:MVDGetInit13
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd13
        return BackCommand;
    }//GEN-END:MVDGetEnd13
 
  
    /** This method returns instance for Group1 component and should be called instead of accessing Group1 field directly.//GEN-BEGIN:MVDGetBegin28
     * @return Instance for Group1 component
     */
    public ChoiceGroup get_Group1() {
        if (Group1 == null) {//GEN-END:MVDGetBegin28
            // Insert pre-init code here
            Group1 = new ChoiceGroup("Frends", Choice.POPUP, new String[] {//GEN-BEGIN:MVDGetInit28
                "Bill",
                "John",
                "Britney"
            }, new Image[] {
                null,
                null,
                null
            });
            Group1.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER | Item.LAYOUT_NEWLINE_AFTER);
            Group1.setSelectedFlags(new boolean[] {
                false,
                false,
                false
            });//GEN-END:MVDGetInit28
            
        // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd28
        return Group1;
    }//GEN-END:MVDGetEnd28

    /** This method returns instance for MainMenu component and should be called instead of accessing MainMenu field directly.//GEN-BEGIN:MVDGetBegin31
     * @return Instance for MainMenu component
     */
    public List get_MainMenu() {
        if (MainMenu == null) {//GEN-END:MVDGetBegin31
            // Insert pre-init code here
            MainMenu = new List("Main menu", Choice.IMPLICIT, new String[] {//GEN-BEGIN:MVDGetInit31
                "Connect",
                "Settings",
                "New account",
                "About JIMMY"
            }, new Image[] {
                get_image2(),
                null,
                null,
                null
            });
            MainMenu.addCommand(get_exitCommand());
            MainMenu.setCommandListener(this);
            MainMenu.setTicker(get_JIMMYTicker());
            MainMenu.setSelectedFlags(new boolean[] {
                false,
                false,
                false,
                false
            });//GEN-END:MVDGetInit31
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd31
        return MainMenu;
    }//GEN-END:MVDGetEnd31

    /** This method returns instance for SettingsForm component and should be called instead of accessing SettingsForm field directly.//GEN-BEGIN:MVDGetBegin43
     * @return Instance for SettingsForm component
     */
    public Form get_SettingsForm() {
        if (SettingsForm == null) {//GEN-END:MVDGetBegin43
            // Insert pre-init code here
            SettingsForm = new Form("Settings", new Item[0]);//GEN-BEGIN:MVDGetInit43
            SettingsForm.addCommand(get_OKCommand());
            SettingsForm.addCommand(get_CancelCommand());
            SettingsForm.setCommandListener(this);//GEN-END:MVDGetInit43
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd43
        return SettingsForm;
    }//GEN-END:MVDGetEnd43

    /** This method returns instance for AccountForm component and should be called instead of accessing AccountForm field directly.//GEN-BEGIN:MVDGetBegin44
     * @return Instance for AccountForm component
     */
    public Form get_AccountForm() {
        if (AccountForm == null) {//GEN-END:MVDGetBegin44
            // Insert pre-init code here
            AccountForm = new Form("New Account", new Item[] {//GEN-BEGIN:MVDGetInit44
                get_ProtocolSelect(),
                get_gauge1()
            });
            AccountForm.addCommand(get_OKCommand());
            AccountForm.addCommand(get_CancelCommand());
            AccountForm.setCommandListener(this);//GEN-END:MVDGetInit44
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd44
        return AccountForm;
    }//GEN-END:MVDGetEnd44

    /** This method returns instance for AboutScreen component and should be called instead of accessing AboutScreen field directly.//GEN-BEGIN:MVDGetBegin45
     * @return Instance for AboutScreen component
     */
    public org.netbeans.microedition.lcdui.SplashScreen get_AboutScreen() {
        if (AboutScreen == null) {//GEN-END:MVDGetBegin45
            // Insert pre-init code here
            AboutScreen = new org.netbeans.microedition.lcdui.SplashScreen(getDisplay());//GEN-BEGIN:MVDGetInit45
            AboutScreen.setTitle("About");
            AboutScreen.setText("About JIMMY IM");
            AboutScreen.setTextFont(get_font1());
            AboutScreen.setNextDisplayable(get_MainMenu());//GEN-END:MVDGetInit45
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd45
        return AboutScreen;
    }//GEN-END:MVDGetEnd45

    /** This method returns instance for font1 component and should be called instead of accessing font1 field directly.//GEN-BEGIN:MVDGetBegin47
     * @return Instance for font1 component
     */
    public Font get_font1() {
        if (font1 == null) {//GEN-END:MVDGetBegin47
            // Insert pre-init code here
            font1 = Font.getDefaultFont();//GEN-LINE:MVDGetInit47
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd47
        return font1;
    }//GEN-END:MVDGetEnd47

    /** This method returns instance for gauge1 component and should be called instead of accessing gauge1 field directly.//GEN-BEGIN:MVDGetBegin52
     * @return Instance for gauge1 component
     */
    public Gauge get_gauge1() {
        if (gauge1 == null) {//GEN-END:MVDGetBegin52
            // Insert pre-init code here
            gauge1 = new Gauge("Step 1/2", false, 2, 1);//GEN-BEGIN:MVDGetInit52
            gauge1.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_BOTTOM | Item.LAYOUT_NEWLINE_BEFORE | Item.LAYOUT_EXPAND);//GEN-END:MVDGetInit52
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd52
        return gauge1;
    }//GEN-END:MVDGetEnd52

    /** This method returns instance for ProtocolSelect component and should be called instead of accessing ProtocolSelect field directly.//GEN-BEGIN:MVDGetBegin53
     * @return Instance for ProtocolSelect component
     */
    public ChoiceGroup get_ProtocolSelect() {
        if (ProtocolSelect == null) {//GEN-END:MVDGetBegin53
            // Insert pre-init code here
            ProtocolSelect = new ChoiceGroup("Select protocols:", Choice.MULTIPLE, new String[] {//GEN-BEGIN:MVDGetInit53
                "MSN Messenger",
                "Jabber",
                "ICQ",
                "Yahoo Messenger"
            }, new Image[] {
                null,
                null,
                null,
                null
            });
            ProtocolSelect.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
            ProtocolSelect.setSelectedFlags(new boolean[] {
                false,
                false,
                false,
                false
            });//GEN-END:MVDGetInit53
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd53
        return ProtocolSelect;
    }//GEN-END:MVDGetEnd53

    /** This method returns instance for UPForm component and should be called instead of accessing UPForm field directly.//GEN-BEGIN:MVDGetBegin60
     * @return Instance for UPForm component
     */
    public Form get_UPForm() {
        if (UPForm == null) {//GEN-END:MVDGetBegin60
            // Insert pre-init code here
            UPForm = new Form("Username, password", new Item[] {//GEN-BEGIN:MVDGetInit60
                get_username(),
                get_password(),
                get_gauge2()
            });
            UPForm.addCommand(get_exitCommand());
            UPForm.addCommand(get_BackCommand());
            UPForm.addCommand(get_OKCommand());
            UPForm.setCommandListener(this);//GEN-END:MVDGetInit60
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd60
        return UPForm;
    }//GEN-END:MVDGetEnd60

    /** This method returns instance for username component and should be called instead of accessing username field directly.//GEN-BEGIN:MVDGetBegin66
     * @return Instance for username component
     */
    public TextField get_username() {
        if (username == null) {//GEN-END:MVDGetBegin66
            // Insert pre-init code here
            username = new TextField("Username", null, 40, TextField.ANY);//GEN-BEGIN:MVDGetInit66
            username.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);//GEN-END:MVDGetInit66
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd66
        return username;
    }//GEN-END:MVDGetEnd66

    /** This method returns instance for password component and should be called instead of accessing password field directly.//GEN-BEGIN:MVDGetBegin67
     * @return Instance for password component
     */
    public TextField get_password() {
        if (password == null) {//GEN-END:MVDGetBegin67
            // Insert pre-init code here
            password = new TextField("Password", null, 40, TextField.ANY | TextField.PASSWORD);//GEN-BEGIN:MVDGetInit67
            password.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER | Item.LAYOUT_NEWLINE_AFTER);//GEN-END:MVDGetInit67
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd67
        return password;
    }//GEN-END:MVDGetEnd67

    /** This method returns instance for Group2 component and should be called instead of accessing Group2 field directly.//GEN-BEGIN:MVDGetBegin70
     * @return Instance for Group2 component
     */
    public ChoiceGroup get_Group2() {
        if (Group2 == null) {//GEN-END:MVDGetBegin70
            // Insert pre-init code here
            Group2 = new ChoiceGroup("Family", Choice.EXCLUSIVE, new String[] {//GEN-BEGIN:MVDGetInit70
                "Mother",
                "Father",
                "Sister",
                "Brother"
            }, new Image[] {
                null,
                null,
                null,
                null
            });
            Group2.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER | Item.LAYOUT_NEWLINE_AFTER);
            Group2.setSelectedFlags(new boolean[] {
                false,
                false,
                false,
                false
            });//GEN-END:MVDGetInit70
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd70
        return Group2;
    }//GEN-END:MVDGetEnd70

    /** This method returns instance for Connecting component and should be called instead of accessing Connecting field directly.//GEN-BEGIN:MVDGetBegin75
     * @return Instance for Connecting component
     */
    public org.netbeans.microedition.lcdui.WaitScreen get_Connecting() {
        if (Connecting == null) {//GEN-END:MVDGetBegin75
            // Insert pre-init code here
            Connecting = new org.netbeans.microedition.lcdui.WaitScreen(getDisplay());//GEN-BEGIN:MVDGetInit75
            Connecting.setFailureDisplayable(get_Warning(), get_ContactsForm());
            Connecting.setTitle("Please wait. Connecting and Authenticating.");
            Connecting.setText("Connecting");
            Connecting.setTextFont(get_font1());
            Connecting.setNextDisplayable(get_ContactsForm());//GEN-END:MVDGetInit75
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd75
        return Connecting;
    }//GEN-END:MVDGetEnd75

    /** This method returns instance for ConversationForm component and should be called instead of accessing ConversationForm field directly.//GEN-BEGIN:MVDGetBegin78
     * @return Instance for ConversationForm component
     */
    public Form get_ConversationForm() {
        if (ConversationForm == null) {//GEN-END:MVDGetBegin78
            // Insert pre-init code here
            ConversationForm = new Form("Conversation to ...", new Item[] {//GEN-BEGIN:MVDGetInit78
                get_textField1(),
                get_stringItem1()
            });
            ConversationForm.addCommand(get_OKCommand());
            ConversationForm.addCommand(get_ContactsCommand());
            ConversationForm.addCommand(get_CloseSession());
            ConversationForm.setCommandListener(this);//GEN-END:MVDGetInit78
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd78
        return ConversationForm;
    }//GEN-END:MVDGetEnd78

    /** This method returns instance for ContactsCommand component and should be called instead of accessing ContactsCommand field directly.//GEN-BEGIN:MVDGetBegin79
     * @return Instance for ContactsCommand component
     */
    public Command get_ContactsCommand() {
        if (ContactsCommand == null) {//GEN-END:MVDGetBegin79
            // Insert pre-init code here
            ContactsCommand = new Command("View contacts", Command.BACK, 1);//GEN-LINE:MVDGetInit79
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd79
        return ContactsCommand;
    }//GEN-END:MVDGetEnd79

    /** This method returns instance for CloseSession component and should be called instead of accessing CloseSession field directly.//GEN-BEGIN:MVDGetBegin82
     * @return Instance for CloseSession component
     */
    public Command get_CloseSession() {
        if (CloseSession == null) {//GEN-END:MVDGetBegin82
            // Insert pre-init code here
            CloseSession = new Command("Close session", Command.EXIT, 1);//GEN-LINE:MVDGetInit82
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd82
        return CloseSession;
    }//GEN-END:MVDGetEnd82

    /** This method returns instance for textField1 component and should be called instead of accessing textField1 field directly.//GEN-BEGIN:MVDGetBegin84
     * @return Instance for textField1 component
     */
    public TextField get_textField1() {
        if (textField1 == null) {//GEN-END:MVDGetBegin84
            // Insert pre-init code here
            textField1 = new TextField("You say:", "Hi!", 120, TextField.ANY);//GEN-BEGIN:MVDGetInit84
            textField1.setLayout(Item.LAYOUT_LEFT | Item.LAYOUT_TOP | Item.LAYOUT_NEWLINE_AFTER | Item.LAYOUT_EXPAND);//GEN-END:MVDGetInit84
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd84
        return textField1;
    }//GEN-END:MVDGetEnd84

    /** This method returns instance for stringItem1 component and should be called instead of accessing stringItem1 field directly.//GEN-BEGIN:MVDGetBegin85
     * @return Instance for stringItem1 component
     */
    public StringItem get_stringItem1() {
        if (stringItem1 == null) {//GEN-END:MVDGetBegin85
            // Insert pre-init code here
            stringItem1 = new StringItem("Your Friend says:", "@ Hello!\n@ What are you doing?");//GEN-BEGIN:MVDGetInit85
            stringItem1.setFont(get_font1());//GEN-END:MVDGetInit85
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd85
        return stringItem1;
    }//GEN-END:MVDGetEnd85

    /** This method returns instance for gauge2 component and should be called instead of accessing gauge2 field directly.//GEN-BEGIN:MVDGetBegin87
     * @return Instance for gauge2 component
     */
    public Gauge get_gauge2() {
        if (gauge2 == null) {//GEN-END:MVDGetBegin87
            // Insert pre-init code here
            gauge2 = new Gauge("Step 2/2", false, 2, 2);//GEN-BEGIN:MVDGetInit87
            gauge2.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER | Item.LAYOUT_VEXPAND);//GEN-END:MVDGetInit87
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd87
        return gauge2;
    }//GEN-END:MVDGetEnd87

    /** This method returns instance for Warning component and should be called instead of accessing Warning field directly.//GEN-BEGIN:MVDGetBegin88
     * @return Instance for Warning component
     */
    public Alert get_Warning() {
        if (Warning == null) {//GEN-END:MVDGetBegin88
            // Insert pre-init code here
            Warning = new Alert("Authentification failed!", "Please check username and password.", null, AlertType.WARNING);//GEN-BEGIN:MVDGetInit88
            Warning.setTimeout(2000);//GEN-END:MVDGetInit88
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd88
        return Warning;
    }//GEN-END:MVDGetEnd88

    /** This method returns instance for image2 component and should be called instead of accessing image2 field directly.//GEN-BEGIN:MVDGetBegin92
     * @return Instance for image2 component
     */
    public Image get_image2() {
        if (image2 == null) {//GEN-END:MVDGetBegin92
            // Insert pre-init code here
            try {//GEN-BEGIN:MVDGetInit92
                image2 = Image.createImage("/JimmyIM_splash.png");
            } catch (java.io.IOException exception) {
            }//GEN-END:MVDGetInit92
            // Insert post-init code here
        }//GEN-BEGIN:MVDGetEnd92
        return image2;
    }//GEN-END:MVDGetEnd92
    
    public void startApp() {
        initialize();
    }
    
    public void pauseApp() {
    }
    
    public void destroyApp(boolean unconditional) {
    }
    
}
