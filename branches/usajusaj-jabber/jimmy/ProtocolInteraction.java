/* JIMMY - Instant Mobile Messenger
   Copyright (C) 2006  JIMMY Project

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 **********************************************************************
 File: jimmy/ProtocolInteraction.java
 Version: pre-alpha  Date: 2006/06/21
 Author(s): Dejan Sakelsak, Matevz Jekovec
 */

package jimmy;

import jimmy.Contact;
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
