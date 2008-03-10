/**
 * 
 */
package org.jimmy.android.ui;

import org.jimmy.android.R;
import org.jimmy.android.data.Account;
import org.jimmy.android.data.Protocol;
import org.jimmy.provider.JimmyIM;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.ContentURI;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * @author matej
 *
 */
public class NewAccount extends Activity
{
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
    };
  
  // The different distinct states the activity can be run in.
  private static final int STATE_EDIT = 0;
  private static final int STATE_NEW = 1;
  
  private Account originalAccount;

  private int mState;
  private ContentURI mURI;
  private Cursor cursor;
  
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    setContentView(R.layout.new_account);
    
    final Intent intent = getIntent();

    Button button = (Button)findViewById(R.id.new_acc_accept);
    button.setOnClickListener(acceptListener);
    button = (Button)findViewById(R.id.new_acc_discard);
    button.setOnClickListener(discardListener);
    
    final String action = intent.getAction();
    if (action.equals(Intent.EDIT_ACTION))
    {
      mState = STATE_EDIT;
      mURI = intent.getData();
    }
    else if (action.equals(Intent.INSERT_ACTION)) 
    {
      mState = STATE_NEW;
      mURI = getContentResolver().insert(intent.getData(), null);
      
      if (mURI == null) 
      {
        Log.e("Notes", "Failed to insert new note into "
            + getIntent().getData());
        finish();
        return;
      }
      
      setResult(RESULT_OK, mURI.toString());
    }
    else
    {
      finish();
      return;
    }
    
    cursor = managedQuery(mURI, PROJECTION, null, null);
    
    if (icicle != null)
      originalAccount = restoreState(icicle);
  }
  
  protected void onResume() 
  {
    super.onResume();
    
    // If we didn't have any trouble retrieving the data, it is now
    // time to get at the stuff.
    if (cursor != null) 
    {
      // Make sure we are at the one and only row in the cursor.
      cursor.first();
      
      // Modify our overall title depending on the mode we are running in.
      if (mState == STATE_EDIT)
        setTitle(getText(R.string.title_edit_account));
      else if (mState == STATE_NEW)
        setTitle(getText(R.string.title_create_account));
      
      Account a = restoreState(cursor);
      fillData(a);
      
      // If we hadn't previously retrieved the original text, do so
      // now.  This allows the user to revert their changes.
      if (originalAccount == null) {
        originalAccount = a;
      }
    }
  }
  
  protected void onFreeze(Bundle outState)
  {
    saveState(outState, originalAccount);
  }
  
  protected void onPause() 
  {
    super.onPause();
    
    originalAccount = null;
  }
  
  private OnClickListener acceptListener = new OnClickListener()
  {
    public void onClick(View v) 
    {
      Account a = getData();
      if (a.getName().equals("") ||
          a.getUser().equals("") ||
          a.getPassword().equals("") ||
          a.getProtocolType() < 0)
      {
        showAlert("Warning", "Name, protocol, username and password fields must not be empty", "OK", false);
        return;
      }
      
      cursor.updateString(1, a.getName());
      cursor.updateString(2, a.getUser());
      cursor.updateString(3, a.getPassword());
      cursor.updateInt(4, a.getProtocolType());
      cursor.updateString(5, a.getServer());
      cursor.updateInt(6, a.getPort());
      cursor.updateInt(7, a.getAutoLogin() ? 1 : 0);
      cursor.updateInt(8, a.getUseSSL() ? 1 : 0);
      managedCommitUpdates(cursor);
      clearFields();
      cursor.deactivate();
      cursor = null;
      finish();
    };
  };
  
  private OnClickListener discardListener = new OnClickListener()
  {
    public void onClick(View v) {
      setResult(RESULT_CANCELED);
      clearFields();
      if (mState == STATE_NEW)
        cursor.deleteRow();
      cursor.deactivate();
      cursor = null;
      finish();
    };
  };
  
  private Account restoreState(Bundle b)
  {
    return new Account(
        b.getString("name"),
        b.getString("username"),
        b.getString("pass"), 
        b.getByte("proto"),
        b.getString("server"),
        b.getInteger("port"),
        b.getBoolean("ssl"),
        b.getBoolean("autologin"));
  }
  
  public static final Account restoreState(Cursor c)
  {
    return new Account(
        c.getString(1),
        c.getString(2),
        c.getString(3), 
        (byte)c.getInt(4),
        c.getString(5),
        c.getInt(6),
        c.getShort(8) != 0,
        c.getShort(7) != 0);
  }

  private void saveState(Bundle b, Account a)
  {
    b.putString("name", a.getName());
    b.putString("username", a.getUser());
    b.putString("pass", a.getPassword());
    b.putByte("proto", a.getProtocolType());
    b.putString("server", a.getServer());
    b.putInteger("port", a.getPort());
    b.putBoolean("autologin", a.getAutoLogin());
    b.putBoolean("ssl", a.getUseSSL());
  }
  
  private void fillData(Account a)
  {
    ((RadioGroup)findViewById(R.id.protocols)).clearCheck();
    switch (a.getProtocolType())
    {
      case Protocol.ICQ:
        ((RadioButton)findViewById(R.id.icq)).setChecked(true);
        break;
      case Protocol.JABBER:
        ((RadioButton)findViewById(R.id.jabber)).setChecked(true);
        break;
      case Protocol.MSN:
        ((RadioButton)findViewById(R.id.msn)).setChecked(true);
        break;
      case Protocol.YAHOO:
        ((RadioButton)findViewById(R.id.yahoo)).setChecked(true);
        break;
      default:
        break;
    }
    
    ((EditText)findViewById(R.id.name)).setText(a.getName());
    ((EditText)findViewById(R.id.username)).setText(a.getUser());
    ((EditText)findViewById(R.id.password)).setText(a.getPassword());
    ((EditText)findViewById(R.id.server)).setText(a.getServer());
    ((EditText)findViewById(R.id.port)).setText(a.getPort() != 0 ?
        String.valueOf(a.getPort()) : "");
    ((CheckBox)findViewById(R.id.ssl)).setChecked(a.getUseSSL());
    ((CheckBox)findViewById(R.id.auto_connect)).setChecked(a.getAutoLogin());
  }

  private Account getData()
  {
    byte proto;
    switch (((RadioGroup)findViewById(R.id.protocols)).getCheckedRadioButtonId())
    {
      case R.id.icq:
        proto = Protocol.ICQ;
        break;
      case R.id.jabber:
        proto = Protocol.JABBER;
        break;
      case R.id.msn:
        proto = Protocol.MSN;
        break;
      case R.id.yahoo:
        proto = Protocol.YAHOO;
        break;
      default: proto = (byte)-1;
        break;
    }
    String port = ((EditText)findViewById(R.id.port)).getText().toString();
    
    return new Account(
        ((EditText)findViewById(R.id.name)).getText().toString(),
        ((EditText)findViewById(R.id.username)).getText().toString(),
        ((EditText)findViewById(R.id.password)).getText().toString(),
        proto,
        ((EditText)findViewById(R.id.server)).getText().toString(),
        Integer.parseInt(port.equals("") ? "0" : port),
        ((CheckBox)findViewById(R.id.ssl)).isChecked(),
        ((CheckBox)findViewById(R.id.auto_connect)).isChecked());
  }
  
  private void clearFields()
  {
    ((RadioGroup)findViewById(R.id.protocols)).clearCheck();
    ((EditText)findViewById(R.id.name)).setText("");
    ((EditText)findViewById(R.id.username)).setText("");
    ((EditText)findViewById(R.id.password)).setText("");
    ((EditText)findViewById(R.id.server)).setText("");
    ((EditText)findViewById(R.id.port)).setText("");
    ((CheckBox)findViewById(R.id.ssl)).setChecked(false);
    ((CheckBox)findViewById(R.id.auto_connect)).setChecked(false);
  }
}
