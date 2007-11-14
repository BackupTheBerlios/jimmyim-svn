package jimmy.yahoo;

public class YahooConstants
{
  public final static int SERVICE_LOGON     = 0x01;
  public final static int SERVICE_LOGOFF      = 0x02;

  public final static int SERVICE_ISAWAY      = 0x03;
  public final static int SERVICE_ISBACK      = 0x04;
  public final static int SERVICE_IDLE      = 0x05;

  public final static int SERVICE_MESSAGE     = 0x06;

  public final static int SERVICE_IDACT     = 0x07;
  public final static int SERVICE_IDDEACT     = 0x08;

  public final static int SERVICE_MAILSTAT    = 0x09;
  public final static int SERVICE_USERSTAT    = 0x0a;
  public final static int SERVICE_NEWMAIL     = 0x0b;
  public final static int SERVICE_CHATINVITE    = 0x0c;
  public final static int SERVICE_CALENDAR    = 0x0d;
  public final static int SERVICE_NEWPERSONMAIL = 0x0e;
  public final static int SERVICE_CONTACTNEW    = 0x0f;

  public final static int SERVICE_ADDIDENT    = 0x10;
  public final static int SERVICE_ADDIGNORE   = 0x11;
  public final static int SERVICE_PING      = 0x12;
  public final static int SERVICE_GROUPRENAME   = 0x13;
  public final static int SERVICE_SYSMESSAGE    = 0x14;
  public final static int SERVICE_PASSTHROUGH2  = 0x16;

  public final static int SERVICE_CONFINVITE    = 0x18;
  public final static int SERVICE_CONFLOGON   = 0x19;
  public final static int SERVICE_CONFDECLINE   = 0x1a;
  public final static int SERVICE_CONFLOGOFF    = 0x1b;
  public final static int SERVICE_CONFADDINVITE = 0x1c;
  public final static int SERVICE_CONFMSG     = 0x1d;

  public final static int SERVICE_CHATPM      = 0x20;

  public final static int SERVICE_GAMELOGON   = 0x28;
  public final static int SERVICE_GAMELOGOFF    = 0x29;
  public final static int SERVICE_GAMEMSG     = 0x2a;

  public final static int SERVICE_FILETRANSFER  = 0x46;
  public final static int SERVICE_VOICECHAT   = 0x4a;
  public final static int SERVICE_NOTIFY      = 0x4b;
  public final static int SERVICE_P2PFILEXFER   = 0x4d;
  public final static int SERVICE_PEERTOPEER    = 0x4f;
  public final static int SERVICE_AUTHRESP    = 0x54;
  public final static int SERVICE_LIST      = 0x55;
  public final static int SERVICE_AUTH      = 0x57;

  public final static int SERVICE_FRIENDADD   = 0x83;
  public final static int SERVICE_FRIENDREMOVE  = 0x84;
  public final static int SERVICE_CONTACTIGNORE = 0x85;
  public final static int SERVICE_CONTACTREJECT = 0x86;

  public final static int SERVICE_CHATCONNECT   = 0x96;
  public final static int SERVICE_CHATGOTO    = 0x97;
  public final static int SERVICE_CHATLOGON   = 0x98;
  public final static int SERVICE_CHATLEAVE   = 0x99;
  public final static int SERVICE_CHATLOGOFF    = 0x9b;
  public final static int SERVICE_CHATDISCONNECT  = 0xa0;
  public final static int SERVICE_CHATPING    = 0xa1;
  public final static int SERVICE_CHATMSG     = 0xa8;

  /* *********** STATUS *****************/
  public final static int UNSTARTED = 0;      // Not logged in
  public final static int AUTH = 1;       // Logging in
  public final static int MESSAGING = 2;      // Logged in
  public final static int FAILED = 3;       // Dead
  public final static int CONNECT = 100;      // (Chat) Introduction
  public final static int LOGON = AUTH;     // (Chat) Alias for auth

  public static final long STATUS_AVAILABLE   = 0;
  public static final long STATUS_BRB       = 1;
  public static final long STATUS_BUSY      = 2;
  public static final long STATUS_NOTATHOME   = 3;
  public static final long STATUS_NOTATDESK   = 4;
  public static final long STATUS_NOTINOFFICE   = 5;
  public static final long STATUS_ONPHONE     = 6;
  public static final long STATUS_ONVACATION    = 7;
  public static final long STATUS_OUTTOLUNCH    = 8;
  public static final long STATUS_STEPPEDOUT    = 9;
  public static final long STATUS_INVISIBLE   = 12;
  public static final long STATUS_BAD       = 13; // Bad login?
  public static final long STATUS_LOCKED      = 14; // You've been naughty
  public static final long STATUS_CUSTOM      = 99;
  public static final long STATUS_IDLE      = 999;
  public static final long STATUS_OFFLINE     = 0x5a55aa56;
  public static final long STATUS_TYPING      = 0x16;

  public static final long STATUS_BADUSERNAME   = 3; // Account unknown?
  public static final long STATUS_INCOMPLETE    = 5; // Chat login etc.
  public static final long STATUS_COMPLETE    = 1; // Chat login etc.

  public static final String STATUS_AVAILABLE_STR   = "Available";
  public static final String STATUS_BRB_STR     = "Be right back";
  public static final String STATUS_BUSY_STR      = "Busy";
  public static final String STATUS_NOTATHOME_STR   = "Not at home";
  public static final String STATUS_NOTATDESK_STR   = "Not at desk";
  public static final String STATUS_NOTINOFFICE_STR = "Not in office";
  public static final String STATUS_ONPHONE_STR   = "On the phone";
  public static final String STATUS_ONVACATION_STR  = "On vacation";
  public static final String STATUS_OUTTOLUNCH_STR  = "Out to lunch";
  public static final String STATUS_STEPPEDOUT_STR  = "Stepped out";
  public static final String STATUS_INVISIBLE_STR   = "Invisible";
  public static final String STATUS_CUSTOM_STR    = "<custom>";
  public static final String STATUS_IDLE_STR      = "Zzz";

  public static final String NOTIFY_TYPING    = "TYPING";
  public static final String NOTIFY_GAME      = "GAME";

}
