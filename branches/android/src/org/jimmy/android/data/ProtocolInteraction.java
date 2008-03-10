/**
 * 
 */
package org.jimmy.android.data;

import java.util.Vector;

/**
 * @author Dejan Sakelsak
 * @author Matevz Jekovec
 */
public interface ProtocolInteraction {
  public void addContact(Contact c);
  public void addContacts(Vector c);
  public void changeContactStatus(Contact c);
  public void stopProtocol(Protocol p);
  public void setProtocolStatus(Protocol p, byte status);
  public void msgRecieved(ChatSession cs, Contact c, String msg);
}
