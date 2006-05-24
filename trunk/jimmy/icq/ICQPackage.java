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
 * @author Dejan Sakelsak
 *
 */
public class ICQPackage {
	
	public static final byte FLAP_id = 0x2A;
	public static final short FLAP_HEADER_SIZE = 6;
	public static final short SNAC_HEADER_SIZE = 10;
	public static final short TLV_HEADER_SIZE = 4;
	public static final short NO_SNAC_FLAP_INDEX = 10; //[0 SNACK+FLAP+TLV) = 20 -> 10 
	public static final short TLV_INDEX = 16;
	public static final short SNAC_INDEX = 6;
	
	private byte ch;			//The FLAP channel
	private int tlv_size = 0;
	private int flap_size = 0;
	private byte[] pkg = null;			//The package in byte array
	
	
	public ICQPackage(byte[] p){
		this.pkg = p;
		this.ch = this.pkg[1];
		byte[] b = new byte[2];
		b[0] = this.pkg[4];
		b[1] = this.pkg[5];
		this.flap_size = Utils.bytesToUShort(b,true);
	}
	
	/**
	 * 
	 * @param content
	 */
	public void setContent(byte[] content){
		
		this.tlv_size = content.length;
		
		if(this.pkg == null){
			this.pkg = new byte[content.length + ICQPackage.FLAP_HEADER_SIZE + ICQPackage.SNAC_HEADER_SIZE + ICQPackage.TLV_HEADER_SIZE];
			for(int i = 0; i<content.length; i++){
				this.pkg[ICQPackage.FLAP_HEADER_SIZE+ICQPackage.SNAC_HEADER_SIZE+ICQPackage.TLV_HEADER_SIZE+i] = content[i];
			}
		}
	}
	
	public byte[] getPackage(){
		
		return this.pkg;
	}
	
	public void setFlap(short seq){
		byte[] a;
		if(this.ch == 2){
			this.pkg[0] = ICQPackage.FLAP_id;
			this.pkg[1] = this.ch;
			 a = Utils.shortToBytes(seq,true);
			this.pkg[2] = a[0];
			this.pkg[3] = a[1];
			a = Utils.shortToBytes(Utils.unsignShort(this.flap_size = this.pkg.length-ICQPackage.FLAP_HEADER_SIZE), true);
			this.pkg[4] = a[0];
			this.pkg[5] = a[1];
		}else{
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX] = ICQPackage.FLAP_id;
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX+1] = this.ch;
			 a = Utils.shortToBytes(seq,true);
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX+2] = a[0];
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX+3] = a[1];
			a = Utils.shortToBytes(Utils.unsignShort(this.flap_size = this.pkg.length-ICQPackage.FLAP_HEADER_SIZE-ICQPackage.SNAC_HEADER_SIZE), true);
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX+4] = a[0];
			this.pkg[ICQPackage.NO_SNAC_FLAP_INDEX+5] = a[1];
		}
		
	}
	
	public void setSnac(int family, int subtype, int flags, int reqid){
		byte[] a = Utils.shortToBytes(Utils.unsignShort(family),true);
		this.pkg[6] = a[0];
		this.pkg[7] = a[1];
		a = Utils.shortToBytes(Utils.unsignShort(subtype),true);
		this.pkg[8] = a[0];
		this.pkg[9] = a[1];
		a = Utils.shortToBytes(Utils.unsignShort(flags),true);
		this.pkg[10] = a[0];
		this.pkg[11] = a[1];
		a = Utils.intToBytes(reqid, true);
	}
	
	public void setTlv(int type){
		byte[] b = Utils.shortToBytes(Utils.unsignShort(type), true);
		this.pkg[16] = b[0];
		this.pkg[17] = b[1];	
		b = Utils.shortToBytes(Utils.unsignShort(this.tlv_size),true);
		this.pkg[18] = b[0];
		this.pkg[19] = b[1];
	}
	
	public boolean isSnack(){
		if(this.ch == 2)
			return true;
		else
			return false;
	}
	
	public void isSnack(boolean b){
		this.ch = 2;
	}
	
	public int getFlapSize(){
		return this.flap_size;
	}
}
