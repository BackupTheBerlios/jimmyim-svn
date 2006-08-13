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

import jimmy.net.ServerHandler;
import java.util.Vector;
import jimmy.util.Utils;

/**
 * Used to feed the ICQProtocol instance with packages arriving from the server.
 * The implementation is now just a step between ServerHandler and ICQProtocol.
 * 
 * @author dejan
 *
 */
public class ICQConnector extends ServerHandler {
	private Vector pkgs = null;
	
	/**
	 * constructs a new instance of this class using the supplide url and port
	 * @param url the url/IP of the server to connect to
	 * @param port the port on the server to connect to
	 */
	public ICQConnector(String url, int port) {
		super(url, port);
		this.pkgs = new Vector();
	}
	
	/**
	 * returns the next awaiting package in the buffer
	 * @return package
	 */
	public byte[] getNextPackage(){
		
		if(this.pkgs.size() > 0){
			Vector pk = (Vector)this.pkgs.firstElement();
			byte[] p = new byte[pk.size()];
			for(int i = 0; i < pk.size(); i++){
				p[i] = ((Byte)pk.elementAt(i)).byteValue();
			}
			this.pkgs.removeElementAt(0);
			//System.out.println(Utils.byteArrayToHexString(p));
			return p;
		}else{
			byte[] b = super.getReplyBytes();
//			int h = 0;
//			if(b == null){
//				while(b==null ){
//					b = super.getReplyBytes();
//					
//				}
//			}
			if(b == null){
				b = super.getReplyBytes();
				if(b == null)
					return b;
			}
			
//			if(b != null){
				for(int i = 0; i < b.length;){
					byte[] bplen = new byte[2];
					bplen[0] = b[i+4];
					bplen[1] = b[i+5];
					int plen = Utils.bytesToInt(bplen,true)+6;
					Vector p = new Vector();
					int ind = i;
					for(	int j = i; j < ind+plen; j++){
						p.addElement(new Byte(b[j]));
						i++;
					}
					this	.pkgs.addElement(p);
				}
				return this.getNextPackage();
//			}
		}
	}
	
	/**
	 * sends a package
	 * @param p the NetPackage -> byte[] version of the package
	 */
	public void sendPackage(byte[] p){
		super.sendRequest(p);
	}
	
	/**
	 * returns the number of packages in the package buffer
	 * @return number of packages waiting
	 */
	public int getNumPackages(){
		return this.pkgs.size();
	}
}
