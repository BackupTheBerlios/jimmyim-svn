/**
 * 
 */
package jimmy;

/**
 * @author Dejan Sakel?ak
 *
 */
public class Utils {

	/**
	 * Returns a short converted to a byte array.
	 * 
	 * @param l a short number
	 * @param bigendian true if big-endian output
	 * @return endian byte array
	 */
	
	public static byte[] shortToBytes(short l, boolean bigendian){
		byte[] by = new byte[2];
		if(bigendian){
			by[0] = (byte)(l >> 8);
            by[1] = (byte)(l & 0xff);
		}else{
            by[0] = (byte)(l & 0xff);
			by[1] = (byte)(l >> 8);
		}
		return by;
	}
}
