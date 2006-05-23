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
	
	/**
	 * Returns a short converted from a byte array.
	 * @param by a byte array
	 * @param bigendian true if big-endian output
	 * @return 
	 */
	public static short bytesToShort(byte[] by, boolean bigendian){
		short s = 0;
		if (bigendian) {
           s = (short) ((by[0] << 8) | (0xff & by[1]));
        } else {
           s = (short) ((by[1] << 8) | (0xff & by[0]));
        }
		return s;
	}
		
	/**
	 * Returns a int converted from a byte array.
	 * We have to get an int because in java we can only so represent
	 * an unsigned short.
	 * 
	 * @param by a byte array
	 * @param bigendian true if big-endian output
	 * @return 
	 */
	public static int bytesToUShort(byte[] by, boolean bigendian){
		int i = 0;
		if (by.length == 1) {
            i = (int) (0xff & by[0]);
        } else if (bigendian) {
            i = (int) (((by[0] & 0xff) << 8) | (0xff & by[1]));
        } else {
            i = (int) (((by[1] & 0xff) << 8) | (0xff & by[0]));
        }
		return i;
	}
}
