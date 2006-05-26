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
 File: jimmy/Account.java
 Version: pre-alpha  Date: 2006/05/21
 Author(s): Dejan Sakelsak, Matevz Jekovec
 */

/**
 * 
 */
package jimmy.icq;


import jimmy.util.*;
/**
 * @author dejan
 *
 */
public class ICQTlv {

	private byte[] header = null;
	private byte[] content = null;
	
	public ICQTlv(){
		
	}
	
	public ICQTlv(byte[] p){
		for(int i = 0; i < p.length; i++){
			if (i < 4){
				this.header[i] = p[i];
			}else{
				this.content[i-4] = p[i];
			}
		}
	}
	
	public void setHeader(short type, short len){
		byte[] b;
		b = Utils.shortToBytes(type,true);
		this.header[0] = b[0];
		this.header[1] = b[1];
		b = Utils.shortToBytes(len,true);
		this.header[2] = b[0];
		this.header[3] = b[1];
	}
	
	public void setHeader(byte[] h){
		this.header = h;
	}
	
	public void setContent(String s){
		this.content = s.getBytes();
	}
	
	public void setContent(byte[] c){
		this.content = c;
	}
	
	public int getLen(){
		return this.header.length+this.content.length;
	}
	
	public int getCLen(){
		return this.content.length;
	}
	
	public byte[] getHeader(short i){
		
		return null;
	}
	
	public byte[] getContent(int i){
		return null;
	}
}
