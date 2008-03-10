package org.jimmy.android.ui;

import java.util.Vector;

import org.jimmy.android.data.ChatSession;
import org.jimmy.android.data.Contact;
import org.jimmy.android.data.Protocol;
import org.jimmy.android.data.ProtocolInteraction;
import org.jimmy.provider.JimmyIM;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;

public class Contacts extends ListActivity implements ProtocolInteraction
{
  public static ProtocolInteraction interaction;
  
  protected void onCreate(Bundle icicle)
  {
    super.onCreate(icicle);
    setDefaultKeyMode(SHORTCUT_DEFAULT_KEYS);
    
    Intent intent = getIntent();
    if (intent.getData() == null) {
        intent.setData(JimmyIM.Accounts.CONTENT_URI);
    }
    
    interaction = this;
  }

  public void addContact(Contact c)
  {
  }

  public void addContacts(Vector c)
  {
  }

  public void changeContactStatus(Contact c)
  {
  }

  public void msgRecieved(ChatSession cs, Contact c, String msg)
  {
  }

  public void setProtocolStatus(Protocol p, byte status)
  {
  }

  public void stopProtocol(Protocol p)
  {
  }
}
