/**
 * 
 */
package jimmy.util;

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
	/**
	 * Returns a int converted from a hexadecimal number
         *
	 * @param s string representing a hexadecimal number
	 * @return 
	 */        
    public int hexToInt(String s)
    {
        int[] n = new int[s.length()];
        char c;
        int sum = 0;
        int koef = 1;
        for(int i=n.length-1; i>=0; i--)
        {
            c = s.charAt(i);
            //System.out.println(c);
            switch ((char)c)
            {
                case 48:
                    n[i] = 0;
                    break;
                case 49:
                    n[i] = 1;
                    break;
                case 50:
                    n[i] = 2;
                    break;
                case 51:
                    n[i] = 3;
                    break;
                case 52:
                    n[i] = 4;
                    break;
                case 53:
                    n[i] = 5;
                    break;
                case 54:
                    n[i] = 6;
                    break;
                case 55:
                    n[i] = 7;
                    break;
                case 56:
                    n[i] = 8;
                    break;
                case 57:
                    n[i] = 9;
                    break;                      
                case 97:
                    n[i] = 10;
                    break;
                case 98:
                    n[i] = 11;
                    break;
                case 99:
                    n[i] = 12;
                    break;
                case 100:
                    n[i] = 13;
                    break;
                case 101:
                    n[i] = 14;
                    break;
                case 102:
                    n[i] = 15;
                    break;
            }
            
            sum = sum + n[i]*koef;
            //System.out.println(sum);
            koef=koef*16;
        }
        return sum;
    }        
}
