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
 File: jimmy/ChatSession.java

 Author(s): Matevz Jekovec
 */

package jimmy;

/**
 * Chat session represents a room (channel) for a chat.
 * Multiple users can join this room (similar to IRC or a conference meeting).
 * 
 * @author matevz
 */
public class ChatSession {
	Protocol protocol_;
	
	public ChatSession(Protocol protocol) {
		this.protocol_ = protocol;
	}
	
	public void sendMsg(String msg) {
		protocol_.sendMsg(msg, this);
	}
	
	public void sendMsg(String msg, Contact contact) {
		protocol_.sendMsg(msg, contact, this)
	}
}
