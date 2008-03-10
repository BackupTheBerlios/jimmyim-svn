package org.jimmy.android.ui;

import org.jimmy.android.R;
import org.jimmy.android.data.Protocol;
import org.jimmy.android.jabber.JabberProtocol;
import org.jimmy.provider.JimmyIM;

import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ContentURI;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

/**
 * @author matej
 */
public class MainMenu extends ListActivity
{
  public static final int NEW_ID = Menu.FIRST;
  public static final int EDIT_ID = Menu.FIRST + 1;
  public static final int DELETE_ID = Menu.FIRST + 2;
  public static final int LOGIN_ID = Menu.FIRST + 3;
  public static final int LOGOUT_ID = Menu.FIRST + 4;
  public static final int ROSTER_ID = Menu.FIRST + 5;

  private static final String[] PROJECTION = new String[] {
    JimmyIM.Accounts._ID, 
    JimmyIM.Accounts.NAME,
    JimmyIM.Accounts.PROTOCOL,
    JimmyIM.Accounts.CONNECTED};
  
  private Cursor cursor;
  
  /** Called when the activity is first created. */
  public void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    setDefaultKeyMode(SHORTCUT_DEFAULT_KEYS);
    
    Intent intent = getIntent();
    if (intent.getData() == null) {
        intent.setData(JimmyIM.Accounts.CONTENT_URI);
    }

    cursor = managedQuery(getIntent().getData(), PROJECTION, null, null);

    Ada adapter = new Ada(this, 0, cursor, new String[] {}, new int[] {});

    setListAdapter(adapter);
    
    if (cursor.count() == 0) insertItem();
  }
  
//  OnItemClickListener itemClickListener = new OnItemClickListener(){
//    
//    public void onItemClick(AdapterView av, View v, int i, long l)
//    {
//      Account current = (Account)av.getAdapter().getItem(i);
//      
//      // which protocol to connect?
//      switch(current.getProtocolType()){
//        case Protocol.JABBER:
//          JabberProtocol jabber = new JabberProtocol(null);
//          current.setConnected(jabber.login(current));
//          current.setProtocol(jabber);
//          break;
//          
//        default:
//          break;
//      }
//      
//    }
//  };
  
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    menu.add(0, NEW_ID, R.string.menu_acc_new).setShortcut(
        KeyEvent.KEYCODE_1, 0, KeyEvent.KEYCODE_N);
    menu.add(0, DELETE_ID, R.string.menu_acc_delete).setShortcut(
        KeyEvent.KEYCODE_2, 0, KeyEvent.KEYCODE_D);
    menu.add(0, EDIT_ID, R.string.menu_acc_edit).setShortcut(
        KeyEvent.KEYCODE_3, 0, KeyEvent.KEYCODE_E);
    menu.addSeparator(0, 0);
    menu.add(0, LOGIN_ID, R.string.menu_acc_login).setShortcut(
        KeyEvent.KEYCODE_4, 0, KeyEvent.KEYCODE_I);
    menu.add(0, LOGOUT_ID, R.string.menu_acc_logout).setShortcut(
        KeyEvent.KEYCODE_5, 0, KeyEvent.KEYCODE_O);
    menu.addSeparator(0, 0);
    menu.add(0, ROSTER_ID, R.string.menu_acc_roster).setShortcut(
        KeyEvent.KEYCODE_6, 0, KeyEvent.KEYCODE_R);

    return true;
  }
  
  public boolean onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    final boolean haveItems = cursor.count() > 0;

    cursor.moveTo(getSelection());
    
    if (haveItems)
    {
      boolean connected = cursor.getInt(3) != 0;
      menu.setItemShown(LOGIN_ID, !connected);
      menu.setItemShown(LOGOUT_ID, connected);
    }
    else
    {
      menu.setItemShown(LOGIN_ID, false);
      menu.setItemShown(LOGOUT_ID, false);
    }
    
    menu.setItemShown(EDIT_ID, haveItems);
    menu.setItemShown(DELETE_ID, haveItems);
    return true;
  }
  
  public boolean onOptionsItemSelected(Menu.Item item) 
  {
    switch (item.getId()) {
    case DELETE_ID:
        deleteItem();
        return true;
    case NEW_ID:
        insertItem();
        return true;
    case EDIT_ID:
      insertItem();
      return true;
    case LOGIN_ID:
      login();
      return true;
    case LOGOUT_ID:
      logout();
      cursor.updateInt(3, 0);
      managedCommitUpdates(cursor);
      return true;
    case ROSTER_ID:
      openRoster();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
  
  protected void onListItemClick(ListView l, View v, int position, long id) {
    ContentURI url = getIntent().getData().addId(getSelectionRowID());
    
    String action = getIntent().getAction();
    if (Intent.PICK_ACTION.equals(action)
            || Intent.GET_CONTENT_ACTION.equals(action)) {
        // The caller is waiting for us to return a note selected by
        // the user.  The have clicked on one, so return it now.
        setResult(RESULT_OK, url.toString());
    } else {
        // Launch activity to view/edit the currently selected item
        startActivity(new Intent(Intent.EDIT_ACTION, url));
    }
  }
  
  private final void deleteItem() {
    cursor.moveTo(getSelection());
    cursor.deleteRow();
  }

  private final void insertItem() {
    // Launch activity to insert a new item
    startActivity(new Intent(Intent.INSERT_ACTION, getIntent().getData()));
  }
  
  private final void login()
  {
    bindService(
        new Intent(MainMenu.this, JabberProtocol.class), 
        "Protocol", 
        mConnection, 
        Context.BIND_AUTO_CREATE);
    openRoster();
  }
  
  private final void logout()
  {
    unbindService(mConnection);
  }
  
  private final void openRoster()
  {
    startActivity(new Intent(Intent.LOGIN_ACTION, getIntent().getData()));
  }
  
  private class Ada extends SimpleCursorAdapter
  {
    public Ada(Context context, int layout, Cursor c, String[] from, int[] to)
    {
      super(context, layout, c, from, to);
    }
    
    public View getView(int position, View convertView, ViewGroup parent)
    {
      mCursor.moveTo(position);
      int proto = mCursor.getInt(2);
      int imageResource;
      
      switch (proto)
      {
        case Protocol.ICQ:
          imageResource = R.drawable.icq_online;
          break;
        case Protocol.JABBER:
          imageResource = R.drawable.jabber_online;
          break;
        case Protocol.MSN:
          imageResource = R.drawable.msn_online;
          break;
        case Protocol.YAHOO:
          imageResource = R.drawable.yahoo_online;
          break;
        default:
          imageResource = R.drawable.jabber_online;
      }
      
      return new BulletedTextView(
          mContext, mCursor.getString(1), imageResource, mCursor.getInt(3) != 0);
    }
  }
  
  /**
   * Thanks to <a href="http://steven.bitsetters.com/"/>http://steven.bitsetters.com/</a>
   * @author matej
   *
   */
  public class BulletedTextView extends LinearLayout
  {
    private TextView mText;
    private ImageView mBullet;
    
    public BulletedTextView(Context context, String text, int imageResource, boolean loggedIn)
    {
      super(context);
      this.setOrientation(HORIZONTAL);
      mBullet = new ImageView(context);
      mBullet.setImageResource(imageResource);
      // left, top, right, bottom
      mBullet.setPadding(0, 2, 5, 0);
      addView(mBullet, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
      mText = new TextView(context);
      mText.setText(text);
      if (loggedIn)
        mText.setTextColor(Color.GREEN);
      addView(mText, new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
    }
    
    public void setText(String words)
    {
      mText.setText(words);
    }
    
    public void setBullet(Drawable bullet)
    {
      mBullet.setImageDrawable(bullet);
    }
  }
  
  private ServiceConnection mConnection = new ServiceConnection()
  {
      public void onServiceConnected(ComponentName className, IBinder service)
      {
          // This is called when the connection with the service has been
          // established, giving us the service object we can use to
          // interact with the service.
      }

      public void onServiceDisconnected(ComponentName className)
      {
          // This is called when the connection with the service has been
          // unexpectedly disconnected -- that is, its process crashed.
          // Because it is running in our same process, we should never
          // see this happen.
        
      }
  };
}