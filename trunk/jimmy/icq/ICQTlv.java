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
 Author(s): Dejan Sakelsak
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

	private byte[] header = new byte[4];
	private byte[] content = null;
	
	/**
	 * Creates a new instance of ICQTlv
	 *
	 */
	public ICQTlv(){
		
	}
	
	/**
	 * Creates a new instance of ICQTlv using the supplied package in byte[]
	 * 
	 * @param p package in byte array
	 */
	public ICQTlv(byte[] p){
		for(int i = 0; i < p.length; i++){
			if (i < 4){
				this.header[i] = p[i];
			}else{
				this.content[i-4] = p[i];
			}
		}
	}
	
	/**
	 * Sets the header of the TLV instance package
	 * 
	 * @param type The TLV service type
	 * @param len The data length in bytes
	 */
	public void setHeader(short type, short len){
		byte[] b;
		b = Utils.shortToBytes(type,true);
		this.header[0] = b[0];
		this.header[1] = b[1];
		b = Utils.shortToBytes(len,true);
		this.header[2] = b[0];
		this.header[3] = b[1];
	}
	
	/**
	 * Sets the header using the supplied byte[]
	 * 
	 * @param h The header in byte array
	 */
	public void setHeader(byte[] h){
		this.header = h;
	}
	
	/**
	 * Sets the content of the TLV supplied with content in String
	 * 
	 * @param s String content
	 */
	public void setContent(String s){
		this.content = s.getBytes();
	}
	
	/**
	 * Setst the content of the TLV supplied with content in byte[]
	 * 
	 * @param c Content byte array
	 */
	public void setContent(byte[] c){
		this.content = c;
	}
	
	/**
	 * Returns the length of the entire TLV
	 * 
	 * @return TLV length
	 */
	public int getLen(){
		if(this.content == null)
			return this.header.length;
		return this.header.length+this.content.length;
	}
	
	/**
	 * Returns the size of the content of the TLV
	 * 
	 * @return TLV content length
	 */
	public int getCLen(){
		if(this.content == null)
			return 0;
		return this.content.length;
	}
	
	/**
	 * Returns the TLV type
	 * 
	 * @return int defining the TLV type
	 */
	public int getType(){
		return (int)ByteOperator.bytesToShort(this.header[0],this.header[1]);
	}
	
	/**
	 * Returns the header of the TLV package
	 * 
	 * @return header in byte[]
	 */
	public byte[] getHeader(){
		
		return this.header;
	}
	
	/**
	 * Returns the content of the TLV package
	 * 
	 * @return content in byte[]
	 */
	public byte[] getContent(){
		return this.content;
	}
	
	/**
	 * Returns the package in byte[]
	 * 
	 * @return Package in byte[]
	 */
	public byte[] getBytes(){
		byte[] b = null;
		if(this.content != null){
			b = new byte[this.header.length+this.content.length];
			for(int i = 0; i < b.length; i++){
				if(i < this.header.length)
					b[i] = this.header[i];
				else
					b[i] = this.content[i-this.header.length];
			}
		}else{
			return this.header;
		}
	
		return b;
	}
}
