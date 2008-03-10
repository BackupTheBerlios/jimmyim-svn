/**
 * 
 */
package org.jimmy.android;

import java.util.HashMap;

import org.jimmy.provider.JimmyIM;

import android.content.ContentProvider;
import android.content.ContentProviderDatabaseHelper;
import android.content.ContentURIParser;
import android.content.ContentValues;
import android.content.QueryBuilder;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.ContentURI;
import android.text.TextUtils;
import android.util.Log;

public class AccountProvider extends ContentProvider {
  
  private SQLiteDatabase db;

  private static final String TAG = "AccountTableProvider";
  private static final String DATABASE_NAME = "jimmy.db";
  private static final int DATABASE_VERSION = 2;

  private static HashMap<String, String> NOTES_LIST_PROJECTION_MAP;

  private static final int ACCOUNTS = 1;
  private static final int ACCOUNT_ID = 2;

  private static final ContentURIParser URL_MATCHER;

  private static class DatabaseHelper extends ContentProviderDatabaseHelper {

      @Override
      public void onCreate(SQLiteDatabase db) {
          db.execSQL(new StringBuilder()
              .append("CREATE TABLE accounts (")
              .append(  JimmyIM.Accounts._ID).append(" INTEGER PRIMARY KEY,")
              .append(  JimmyIM.Accounts.PROTOCOL).append(" INTEGER,")
              .append(  JimmyIM.Accounts.NAME).append(" TEXT,")
              .append(  JimmyIM.Accounts.USER_NAME).append(" TEXT,")
              .append(  JimmyIM.Accounts.PASSWORD).append(" TEXT,")
              .append(  JimmyIM.Accounts.SERVER).append(" TEXT,")
              .append(  JimmyIM.Accounts.PORT).append(" INTEGER,")
              .append(  JimmyIM.Accounts.AUTO_LOGIN).append(" BOOLEAN,")
              .append(  JimmyIM.Accounts.SSL).append(" BOOLEAN,")
              .append(  JimmyIM.Accounts.CONNECTED).append(" BOOLEAN")
              .append(");").toString());
      }

      @Override
      public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
          Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                  + newVersion + ", which will destroy all old data");
          db.execSQL("DROP TABLE IF EXISTS accounts");
          onCreate(db);
      }
  }
  
  @Override
  public boolean onCreate() {
      DatabaseHelper dbHelper = new DatabaseHelper();
      db = dbHelper.openDatabase(getContext(), DATABASE_NAME, null, DATABASE_VERSION);
      return (db == null) ? false : true;
  }

  @Override
  public Cursor query(ContentURI url, String[] projection, String selection,
          String[] selectionArgs, String groupBy, String having, String sort) {
      QueryBuilder qb = new QueryBuilder();

      switch (URL_MATCHER.match(url)) {
      case ACCOUNTS:
          qb.setTables("accounts");
          qb.setProjectionMap(NOTES_LIST_PROJECTION_MAP);
          break;

      case ACCOUNT_ID:
          qb.setTables("accounts");
          qb.appendWhere("_id=" + url.getPathSegment(1));
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      // If no sort order is specified use the default
      String orderBy;
      if (TextUtils.isEmpty(sort)) {
          orderBy = JimmyIM.Accounts.DEFAULT_SORT_ORDER;
      } else {
          orderBy = sort;
      }

      Cursor c = qb.query(db, projection, selection, selectionArgs, groupBy,
              having, orderBy);
      c.setNotificationUri(getContext().getContentResolver(), url);
      return c;
  }

  @Override
  public String getType(ContentURI url) {
      switch (URL_MATCHER.match(url)) {
      case ACCOUNTS:
          return "vnd.android.cursor.dir/vnd.jimmy.account";

      case ACCOUNT_ID:
          return "vnd.android.cursor.item/vnd.jimmy.account";

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }
  }

  @Override
  public ContentURI insert(ContentURI url, ContentValues initialValues) 
  {
      long rowID;
      ContentValues values;
      
      if (initialValues != null)
          values = new ContentValues(initialValues);
      else
          values = new ContentValues();

      if (URL_MATCHER.match(url) != ACCOUNTS)
          throw new IllegalArgumentException("Unknown URL " + url);

      // Make sure that the fields are all set
      if (values.containsKey(JimmyIM.Accounts.AUTO_LOGIN) == false)
          values.put(JimmyIM.Accounts.AUTO_LOGIN, false);

      if (values.containsKey(JimmyIM.Accounts.PORT) == false)
          values.put(JimmyIM.Accounts.PORT, 0);

      if (values.containsKey(JimmyIM.Accounts.SERVER) == false)
          values.put(JimmyIM.Accounts.SERVER, "");

      if (values.containsKey(JimmyIM.Accounts.SSL) == false)
          values.put(JimmyIM.Accounts.SSL, false);
      
      if (values.containsKey(JimmyIM.Accounts.CONNECTED) == false)
        values.put(JimmyIM.Accounts.CONNECTED, false);

      rowID = db.insert("accounts", "account", values);
      if (rowID > 0) {
          ContentURI uri = JimmyIM.Accounts.CONTENT_URI.addId(rowID);
          getContext().getContentResolver().notifyChange(uri, null);
          return uri;
      }

      throw new SQLException("Failed to insert row into " + url);
  }

  @Override
  public int delete(ContentURI url, String where, String[] whereArgs) {
      int count;
      switch (URL_MATCHER.match(url)) {
      case ACCOUNTS:
          count = db.delete("accounts", where, whereArgs);
          break;

      case ACCOUNT_ID:
          String segment = url.getPathSegment(1);
          count = db.delete("accounts", JimmyIM.Accounts._ID + "="
                          + segment
                          + (!TextUtils.isEmpty(where) ? " AND (" + where
                                  + ')' : ""), whereArgs);
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      getContext().getContentResolver().notifyChange(url, null);
      return count;
  }

  @Override
  public int update(ContentURI url, ContentValues values, String where, String[] whereArgs) {
      int count;
      switch (URL_MATCHER.match(url)) {
      case ACCOUNTS:
          count = db.update("accounts", values, where, whereArgs);
          break;

      case ACCOUNT_ID:
          String segment = url.getPathSegment(1);
          count = db
                  .update("accounts", values, JimmyIM.Accounts._ID + "="
                          + segment
                          + (!TextUtils.isEmpty(where) ? " AND (" + where
                                  + ')' : ""), whereArgs);
          break;

      default:
          throw new IllegalArgumentException("Unknown URL " + url);
      }

      getContext().getContentResolver().notifyChange(url, null);
      return count;
  }

  static {
      URL_MATCHER = new ContentURIParser(ContentURIParser.NO_MATCH);
      URL_MATCHER.addURI("org.jimmy.provider.JimmyIM", "accounts", ACCOUNTS);
      URL_MATCHER.addURI("org.jimmy.provider.JimmyIM", "accounts/#", ACCOUNT_ID);

      NOTES_LIST_PROJECTION_MAP = new HashMap<String, String>();
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts._ID, "_id");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.PROTOCOL, "protocol");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.NAME, "name");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.USER_NAME, "username");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.PASSWORD, "password");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.SERVER, "server");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.PORT, "port");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.AUTO_LOGIN, "autologin");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.SSL, "ssl");
      NOTES_LIST_PROJECTION_MAP.put(JimmyIM.Accounts.CONNECTED, "connected");
  }
}
