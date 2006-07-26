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
import java.util.Vector;

/**
 * @author Dejan Sakelsak
 * 
 */
public class ICQPackage {

	public static final byte FLAP_id = 0x2A;

	public static final short FLAP_HEADER_SIZE = 6;

	public static final short SNAC_HEADER_SIZE = 10;

	public static final short SNACK_PKG_HEADER_SIZE = 16;

	private byte ch; // The FLAP channel

	private int flap_size = 0;

	private byte[] pkg = null; // The package in byte array

	private Vector tlvs = null;
	
	private byte[] s = null;

	/**
	 * Creates a new instance of this class
	 * 
	 */
	public ICQPackage() {
		// TODO alter it
	}

	/**
	 * Creates a new instance of this class and sets the size of the package in
	 * advance
	 * 
	 * @param data_size
	 *            The size of the data you will send
	 */
	public ICQPackage(int data_size,byte c) {
		this.ch = c;
		if (this.pkg == null)
			if (this.ch == 0x02)
				this.pkg = new byte[data_size
						+ ICQPackage.SNACK_PKG_HEADER_SIZE];
			else
				this.pkg = new byte[data_size + ICQPackage.FLAP_HEADER_SIZE];
		System.out.print("datasize ");System.out.println(data_size);
	}

	/**
	 * Creates a new instance of this class and sets the package to p
	 * 
	 * @param p
	 *            The entire package in byte[]
	 */
	public ICQPackage(byte[] p) {
		this.pkg = p;
		this.ch = this.pkg[1];
		byte[] b = new byte[2];
		b[0] = this.pkg[4];
		b[1] = this.pkg[5];
		this.flap_size = Utils.bytesToUShort(b, true);
		System.out.print("channel");
		System.out.println(this.ch);
		if(this.ch != 0x01){
			System.out.println("dismanlte");
			this.tlvs = new Vector();
			dismantle();
		}
	}

	
	public void dismantle(){
		int header = ICQPackage.FLAP_HEADER_SIZE;
		if(this.ch == 0x02){
			header = ICQPackage.SNACK_PKG_HEADER_SIZE;
		}
		System.out.println("dismanlte");
		if (this.ch != 0x02) {
			for (int i = header; i < this.pkg.length; i++) {
				ICQTlv tlv = new ICQTlv();
				byte[] b1 = new byte[2];
				byte[] b2 = new byte[2];
				b1[0] = this.pkg[i];
				b1[1] = this.pkg[i + 1];
				b2[0] = this.pkg[i + 2];
				b2[1] = this.pkg[i + 3];
				tlv.setHeader(Utils.bytesToShort(b1, true), Utils.bytesToShort(b2, true));
				i = i + 4;
				byte[] bla = new byte[Utils.bytesToInt(b2, true)];
				for (int j = 0; j < Utils.bytesToInt(b2, true); j++) {
					bla[j] = this.pkg[i];
					if (j != bla.length - 1)
						i++;
				}
				tlv.setContent(bla);
				this.tlvs.addElement(tlv);
			}
			/*
			 * byte[] seq = new byte[2]; seq[0] = this.pkg[2]; seq[1] =
			 * this.pkg[3]; this.pkg = new byte[6]; this.pkg[0] = 0x2a;
			 * this.pkg[1] = this.ch; this.pkg[2] = seq[0]; this.pkg[3] =
			 * seq[1]; byte[] s = Utils.intToBytes(this.flap_size,true);
			 * this.pkg[4] = s[0]; this.pkg[5] = s[1];
			 */
			byte[] b = new byte[header];
			for (int i = 0; i < header; i++) {
				b[i] = this.pkg[i];
			}
			this.pkg = b;
			b = null;
		}else{
			//dismantle as SNAC type
			int type = this.getSnackType();
			int subtype = this.getSnackSubType();
			//DISMANTLE SNAC BY SUBTYPE
			System.out.println("dismantling SNAC");
			switch(type){
			case 0x0017:
				switch(subtype){
				case 0x0007:
					break;
				}
				break;
			case 0x0001:
				switch(subtype){
				//supported services
				case 0x0003:
					this.s = new byte[this.flap_size - ICQPackage.SNAC_HEADER_SIZE];
					for(int i = ICQPackage.SNACK_PKG_HEADER_SIZE; i < pkg.length; i++)
						s[i-ICQPackage.SNACK_PKG_HEADER_SIZE] = pkg[i];
					System.out.println("reading services...");
					break;
				case 0x0018:
					
					break;
				}
				break;
			}
		}
	}
	
	/**
	 * Sets the content we want to send.
	 * 
	 * @param content
	 *            the "message" we send (data)
	 */
	public void setContent(byte[] content) {

		if (this.pkg == null) {
			if (this.ch == 0x02) {
				this.pkg = new byte[content.length
						+ ICQPackage.SNACK_PKG_HEADER_SIZE];
				for (int i = 0; i < content.length; i++) {
					this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE + i] = content[i];
				}
			} else {
				this.pkg = new byte[content.length
						+ ICQPackage.FLAP_HEADER_SIZE];
				for (int i = 0; i < content.length; i++) {
					this.pkg[ICQPackage.FLAP_HEADER_SIZE + i] = content[i];
				}
			}
		} else {
			System.out.print("PKGlen: ");System.out.println(this.pkg.length);
			if (this.ch == 0x02) {
				for (int i = 0; i < content.length; i++) {
					this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE + i] = content[i];
				}
			} else {
				for (int i = 0; i < content.length; i++) {
					this.pkg[ICQPackage.FLAP_HEADER_SIZE + i] = content[i];
				}
			}
		}
	}

	public byte[] getContent(){
		int header = ICQPackage.FLAP_HEADER_SIZE;
		if(this.ch == 0x02)
			header = ICQPackage.SNACK_PKG_HEADER_SIZE;
		byte[] b = new byte[this.pkg.length - header];
		for (int i = 0; i<b.length; i++){
			b[i] = this.pkg[header+i];
		}
		return b;
	}
	
	/**
	 * Return the package that we want to send or just interpret.
	 * 
	 * @return the package in byte array
	 */
	public byte[] getPackage() {
		Vector v = new Vector();
			for (int i = 0; i < this.pkg.length; i++) {
				v.addElement(new Byte(this.pkg[i]));
			}
			if (this.tlvs.size() != 0) {
				for (int i = 0; i < this.tlvs.size(); i++) {
					byte[] b = ((ICQTlv) this.tlvs.elementAt(i)).getBytes();
					for (int j = 0; j < b.length; j++) {
						v.addElement(new Byte(b[j]));
					}
				}
			}

			byte[] b = new byte[v.size()];
			for (int i = 0; i < v.size(); i++) {
				b[i] = ((Byte) v.elementAt(i)).byteValue();
				System.out.println(((Byte) v.elementAt(i)).toString());
				// v.removeElementAt(0);
			}
			v = null;
			System.out.println(b.length);
			return b;
	}
	
	public byte[] getNetPackage() {
		Vector v = new Vector();
			for (int i = 0; i < this.pkg.length; i++) {
				v.addElement(new Byte(this.pkg[i]));
			}
			if(this.tlvs != null)
				if (this.tlvs.size()	 != 0) {
					for (int i = 0; i < this.tlvs.size(); i++) {
						byte[] b = ((ICQTlv) this.tlvs.elementAt(i)).getBytes();
						for (int j = 0; j < b.length; j++) {
							v.addElement(new Byte(b[j]));
						}
					}
				}
			/*if (v.size()%16 != 0){
				int mod = 16-v.size()%16;
				for (int i = 0; i < mod; i++){
					v.addElement(new Byte((byte)0x00));
				}
			}*/
			byte[] b = new byte[v.size()];
			for (int i = 0; i < v.size(); i++) {
				b[i] = ((Byte) v.elementAt(i)).byteValue();
				//System.out.println(((Byte) v.elementAt(i)).toString());
				// v.removeElementAt(0);
			}
			v = null;
			return b;
	}

	/**
	 * Sets the FLAP package header.
	 * 
	 * @param seq
	 *            The sequence number (0x0000 - 0x8000, normally in increases
	 *            from package to package)
	 */
	public void setFlap(short seq) {

		byte[] a;
			this.pkg[0] = ICQPackage.FLAP_id;
			this.pkg[1] = this.ch;
			a = Utils.shortToBytes(seq, true);
			this.pkg[2] = a[0];
			this.pkg[3] = a[1];
			a = Utils.shortToBytes((short)(this.flap_size = this.pkg.length	- ICQPackage.FLAP_HEADER_SIZE+this.getTlvSize()), true);
			this.pkg[4] = a[0];
			this.pkg[5] = a[1];
	}

	/**
	 * Sets the SNAC package header.
	 * 
	 * @param family
	 *            Service family ID.
	 * @param subtype
	 *            Service subtype ID-
	 * @param flags
	 *            Sets the SNAC flags header attribute. bit1 should be enabled
	 *            if there are more packages of the same reqid
	 * @param reqid
	 *            Snac reqid -> you should save some information on sent SNACs
	 *            for some time, you need back information when you recieve a
	 *            package.
	 */
	public void setSnac(int family, int subtype, int flags, int reqid) {
		this.ch = 0x02;
		if(this.pkg == null)
			this.pkg = new byte[ICQPackage.SNACK_PKG_HEADER_SIZE];
		byte[] a = Utils.shortToBytes((short)family, true);
		this.pkg[6] = a[0];
		this.pkg[7] = a[1];
		a = Utils.shortToBytes((short)subtype, true);
		this.pkg[8] = a[0];
		this.pkg[9] = a[1];
		a = Utils.shortToBytes((short)flags, true);
		this.pkg[10] = a[0];
		this.pkg[11] = a[1];
		a = Utils.intToBytes(reqid, true);
		this.pkg[12] = a[0];
		this.pkg[13] = a[1];
		this.pkg[14] = a[2];
		this.pkg[15] = a[3];
	}

	/**
	 * Adds a TLV to the tlv list in the package
	 * 
	 * @param t
	 *            the TLV
	 */
	public void addTlv(ICQTlv t) {
		if (this.tlvs == null)
			this.tlvs = new Vector();
		this.tlvs.addElement(t);
	}

	/**
	 * Tells if a package is or is set to be of SNAC type -> FLAP channel
	 * property is 0x02
	 * 
	 * @return true if is or is set to be SNAC
	 */
	public boolean isSnack() {
		if (this.ch == 0x02)
			return true;
		else
			return false;
	}

	/**
	 * Sets the FLAP channel to c.
	 * 
	 * @param c
	 *            Desired FLAP channel
	 */
	public void setChannel(byte c) {
		this.ch = c;
	}

	/**
	 * Returns the size of the FLAP content
	 * 
	 * @return FLAP content size
	 */
	public int getFlapSize() {
		return this.flap_size;
	}

	public void setFlapSize(int s) {
		this.flap_size = s;
	}

	public int getTlvSize() {
		int l = 0;
		if(this.tlvs != null){
			if(this.tlvs.size() == 0)
				return 0;
		
			for (int i = 0; i < this.tlvs.size(); i++) {
				l = l + ((ICQTlv) this.tlvs.elementAt(i)).getLen();
			}
		}
		return l;
	}

	public ICQTlv getTlv(int ind){
		return (ICQTlv)this.tlvs.elementAt(ind);
	}
	
	public byte[] getHeader(){
		byte[] b = new byte[6];
		b[0] = 0x2a;
		b[1] = this.ch;
		b[2] = this.pkg[2];
		b[3] = this.pkg[3];
		b[4] = this.pkg[4];
		b[5] = this.pkg[5];
		return b;
	}
	
	public int getSize() {
		int l = 0;
		l = this.getTlvSize() + this.pkg.length;
		return l;
	}
	
	public byte[] getServices(){
		return this.s;
	}
	
	public byte getChannel(){return this.ch;}
	
	public int getSnackType(){
		byte[] b = new byte[2];
		b[0] = this.pkg[ICQPackage.FLAP_HEADER_SIZE];
		b[1] = this.pkg[ICQPackage.FLAP_HEADER_SIZE+1];
		return Utils.bytesToInt(b,true);
	}
	
	public int getSnackSubType(){
		byte[] b = new byte[2];
		b[0] = this.pkg[ICQPackage.FLAP_HEADER_SIZE+2];
		b[1] = this.pkg[ICQPackage.FLAP_HEADER_SIZE+3];
		return Utils.bytesToInt(b,true);
	}
	
	public int getSnackReqID(){
		byte[] b = new byte[4];
		b[3] = this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE-1];
		b[2] = this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE-2];
		b[1] = this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE-3];
		b[0] = this.pkg[ICQPackage.SNACK_PKG_HEADER_SIZE-4];
		return Utils.bytesToInt(b,true);
	}
}
