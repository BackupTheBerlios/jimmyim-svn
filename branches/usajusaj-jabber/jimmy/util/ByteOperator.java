/**
 * JIMMY - Instant Mobile Messenger
 *   Copyright (C) 2006  JIMMY Project
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * **********************************************************************
 * File: jimmy/util/RMS.java
 * Version: pre-alpha  Date: 2006/06/05
 * Author(s): Dejan Sakelsak
 * @version pre-alpha
 */

package jimmy.util;

import java.util.Vector;

public class ByteOperator {

	Vector v = null;
	
	public ByteOperator() {
		this.v = new Vector();
	}
	
	public void append(byte b){
		this.v.addElement(new Byte(b));
	}
	
	public void append(byte a, byte b){
		this.append(a); this.append(b);
	}
	
	public void append(byte a, byte b, byte c){
		this.append(a,b); this.append(c);
	}
	
	public void append(byte a, byte b, byte c, byte d){
		this.append(a,b); this.append(c,d);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e){
		this.append(a,b,c,d); this.append(e);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e, byte f){
		this.append(a,b,c,d,e);this.append(f);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e, byte f, byte g){
		this.append(a,b,c,d,e,f);this.append(g);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e, byte f,byte g, byte h){
		this.append(a,b,c,d,e,f,g);this.append(h);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h, byte i){
		this.append(a,b,c,d,e,f,g,h);this.append(i);
	}
	
	public void append(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h, byte i, byte j){
		this.append(a,b,c,d,e,f,g,h,i);this.append(j);
	}
	
	public void append(byte[] b){
		for(int i = 0; i < b.length; i++){
			this.v.addElement(new Byte(b[i]));
		}
	}
	
	public static byte[] slice(int s, byte[] b, int e){
		byte[] a = new byte[e-s+1];
		for(int i = 0; i< a.length; i++)
			a[i] = b[s+i];
		return a;
	}
	
	public static byte[] slice(int s, byte[] b){
		byte[] a = new byte[b.length-s+1];
		for(int i = 0; i<a.length; i++)
			a[i] = b[s+i];
		return a;
	}
	
	public static byte[] slice(byte[] b, int e){
		byte[] a = new byte[e+1];
		for(int i = 0; i<e+1; i++)
			a[i] = b[i];
		return a;
	}
	
	public byte[] getBytes(){
		byte[] b = new byte[this.v.size()];
		for(int i = 0; i < this.v.size(); i++){
			b[i] = ((Byte)this.v.elementAt(i)).byteValue();
		}
		return b;
	}
	
	public static short bytesToShort(byte b1, byte b2){
		byte[] b = new byte[2];
		b[0] = b1;
		b[1] = b2;
		
		return Utils.bytesToShort(b,true);
	}
	
	public void clear(){
		this.v = new Vector();
	}
}
