/**
 * 
 */
package org.jimmy.android.data;

import org.jimmy.android.jabber.JabberProtocol;
import org.jimmy.android.ui.NewAccount;
import org.jimmy.provider.JimmyIM;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;

/**
 * @author matej
 *
 */
public class ProtocolManager extends Activity
{
  private Cursor cursor;
  
  private static final String[] PROJECTION = new String[] {
    JimmyIM.Accounts._ID, 
    JimmyIM.Accounts.NAME, // 1
    JimmyIM.Accounts.USER_NAME, // 2
    JimmyIM.Accounts.PASSWORD, // 3
    JimmyIM.Accounts.PROTOCOL, // 4
    JimmyIM.Accounts.SERVER, // 5
    JimmyIM.Accounts.PORT, // 6
    JimmyIM.Accounts.AUTO_LOGIN, // 7
    JimmyIM.Accounts.SSL, // 8
    JimmyIM.Accounts.CONNECTED // 9
    };

  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);

    Intent intent = getIntent();
    if (intent.getData() == null) {
        intent.setData(JimmyIM.Accounts.CONTENT_URI);
    }

    cursor = managedQuery(getIntent().getData(), PROJECTION, null, null);
    cursor.first();
    
    if (cursor.count() == 0)
    {
      setResult(RESULT_CANCELED);
      finish();
    }
    
    switch (cursor.getInt(4))
    {
      case Protocol.JABBER:
        JabberProtocol jabber = new JabberProtocol(null);
        Account a = NewAccount.restoreState(cursor);
        cursor.updateInt(9, jabber.login(a) ? 1 : 0);
        break;
      
      default:
        break;
    }
    
//    getApplication().
  }

}
