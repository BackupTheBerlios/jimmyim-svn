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
 File: jimmy/Contact.java
 Version: pre-alpha  Date: 2006/05/12
 Author(s): Dejan Sakel?ak
 */

/**
 * 
 */
package jimmy.icq;

/**
 * Class ICQPackege represents the skeleton of the three possible types of
 * ICQ packages FLAP, SNAC and TLV
 * 
 * @author Dejan Sakel?ak
 *
 */
public abstract class ICQPackage {
	
	protected byte[] header;
	protected byte[] content;
	
	/**
	 * Returns the package header in byte array.
	 * 
	 * @return header
	 */
	public abstract byte[] getHeader();

	/**
	 * Sets the package header information.
	 * 
	 * @param head
	 */
	public abstract void setHeader(byte[] head);
	
	/**
	 * Returns the package in byte array.
	 * 
	 * @return package
	 */
	public abstract byte[] getBPackage();
	
	/**
	 * Returns the package in String.
	 * 
	 * @return package
	 */
	public abstract String getSPackage();
	
	/**
	 * Sets the content of the package data field.
	 * 
	 * @param cont
	 */
	public abstract void setContent(byte[] cont);
	
	/**
	 * Sets the content of the package data field.
	 * 
	 * @param cont
	 */
	public abstract void setContent(String cont);
}
