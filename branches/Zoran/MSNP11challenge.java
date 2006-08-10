StringBuffer[] hashes = new StringBuffer[4];
            hashes[0] = new StringBuffer(hash.substring(0,8));
            hashes[1] = new StringBuffer(hash.substring(8,16));
            hashes[2] = new StringBuffer(hash.substring(16,24));
            hashes[3] = new StringBuffer(hash.substring(24,hash.length()));
            
            int[] md5hash = new int[4];
            char c;
            for(int i=0; i<hashes.length; i++)
            {
                //System.out.println("bEFORE:"+hashes[i]);
                c = hashes[i].charAt(6);
                hashes[i].setCharAt(6, hashes[i].charAt(0));
                hashes[i].setCharAt(0, c);
                
                c = hashes[i].charAt(7);
                hashes[i].setCharAt(7, hashes[i].charAt(1));
                hashes[i].setCharAt(1, c);  
                
                c = hashes[i].charAt(2);
                hashes[i].setCharAt(2, hashes[i].charAt(4));
                hashes[i].setCharAt(4, c);
                
                c = hashes[i].charAt(3);
                hashes[i].setCharAt(3, hashes[i].charAt(5));
                hashes[i].setCharAt(5, c);                

                //System.out.println("aFTER:"+hashes[i].toString());
                //md5hash[i] = hexToInt(hashes[i].toString()) &  0x7FFFFFF;
		
                //md5hash[i] = hexToInt(andHex(hashes[i].toString()));
                //md5hash[i] = Integer.parseInt(hashes[i].toString(), 16) & 0x7FFFFFFF;
                System.out.println(andHex(hashes[i].toString()) + " -> "+md5hash[i]);
                
                
		
	    }
		md5hash[0]=0x2a1749ec;
		md5hash[1]=0x1517305b;

		md5hash[2]=0x7847399e;
		md5hash[3]=0x041698ff;


            for(int i=0; i<md5hash.length; i++)
            {
                
            }
            
            // second step
            StringBuffer chlString;
            /*chlString = new StringBuffer(data.substring(6,data.length()-2));
            chlString.append(this.ProductID);*/
            chlString = new StringBuffer("22210219642164014968");
            chlString.append(this.ProductID);
            System.out.println(chlString);
            int r = (int)Math.ceil((double) chlString.length() / 8) * 8 - chlString.length();
            for(int i=0; i< r; i++)
            {
                chlString.append('0');
            }
            System.out.println(chlString);
            
            String tempS = chlString.toString();
            
            String[] hashInts = new String[5];
            hashInts[0] = tempS.substring(0,4);
            hashInts[1] = tempS.substring(4,8);
            hashInts[2] = tempS.substring(8,12);
            hashInts[3] = tempS.substring(12,16);      
            hashInts[4] = tempS.substring(16,20);
            //hashInts[5] = hash.substring(20,24);
            /*hashInts[6] = hash.substring(24,28);
            hashInts[7] = hash.substring(28,32);         
            hashInts[8] = hash.substring(32,36);  
            hashInts[9] = hash.substring(36,hash.length());  */            
            
            
            int[] chlStringArray = new int[10];
            chlStringArray[5] = 1146049104;
            chlStringArray[6] = 809054256;
            chlStringArray[7] = 1430345049;
            chlStringArray[8] = 1110604630;
            chlStringArray[9] = 808464432;   
            
            StringBuffer piece;
            for(int i = 0; i<hashInts.length;i++)
            {
                //System.out.println("TEST:" + hashInts[i]);
                piece = new StringBuffer();
                for(int j=3;j>=0;j--)
                {
                    piece.append(this.intToHex((int)hashInts[i].charAt(j)));
                }                
                //System.out.println("Piece:"+piece.toString());
                chlStringArray[i] = hexToInt(piece.toString());
                //System.out.println("Hex:" + chlStringArray[i]);
            }
            for(int i=0; i<chlStringArray.length; i++)
            {
                System.out.println("Hex:" + chlStringArray[i]);
            }
            
            // third step
            
            /*int high = 0;
            int low = 0;

            for (int i = 0; i < chlStringArray.length; i = i + 2) 
            {
              int temp = chlStringArray[i];
              temp = (0x0E79A9C1 * temp) % 0x7FFFFFFF + high;
              temp = (md5hash[0] * temp + md5hash[1]) % 0x7FFFFFFF;

              high = chlStringArray[i + 1];
              high = (high + temp) % 0x7FFFFFFF;
              high = (md5hash[2] * high + md5hash[3]) % 0x7FFFFFFF;

              low = low + high + temp;
            }
            high = (high + md5hash[1]) % 0x7FFFFFFF;
            low = (low + md5hash[3]) % 0x7FFFFFFF;
*/
        long high = 0;
        long low = 0;
        for (int i = 0; i < chlStringArray.length; i = i + 2) {
            long temp = (int)(((chlStringArray[i] * MSNP11_MAGIC_NUM) % 0x7FFFFFFF) + high);
            temp = ((temp * md5hash[0]) + md5hash[1]) % 0x7FFFFFFF;

            high = (chlStringArray[i + 1] + temp) % 0x7FFFFFFF;
            high = (md5hash[2] * high + md5hash[3]) % 0x7FFFFFFF;

            low = low + high + temp;
        }
        high = (high + md5hash[1]) % 0x7FFFFFFF;
        low = (low + md5hash[3]) % 0x7FFFFFFF;        
           
           
            /*long high = 0;
            long low = 0;
            for (int i = 0; i < chlStringArray.length; i = i + 2) {
                long temp = (((chlStringArray[i] * MSNP11_MAGIC_NUM) % 0x7FFFFFFF) + high);
                temp = ((temp * md5hash[0]) + md5hash[1]) % 0x7FFFFFFF;

                high = (chlStringArray[i + 1] + temp) % 0x7FFFFFFF;
                high = (md5hash[2] * high + md5hash[3]) % 0x7FFFFFFF;

                low = low + high + temp;
            }
            high = (high + md5hash[1]) % 0x7FFFFFFF;
            low = (low + md5hash[3]) % 0x7FFFFFFF; */       
            
            
            
            //System.out.println("low:" + low + " "+Long.parseLong(hash.substring(0,16).toUpperCase(),16));
            System.out.println("high:" + Integer.toHexString((int)high));
	    System.out.println("low:" + Integer.toHexString((int)low));
            // Gives high = 0x69A5A771 (1772463985 decimal) and low = 0xD4020628 (3556902440 decimal)

            long key = (high << 32) + low;
            // Gives 0x69a5a771d4020628 (7612674852469737000 decimal)            
            
            //long challenge = Long.parseLong(hash.substring(0,16).toUpperCase(),16)^key + Long.parseLong(hash.substring(16),16)^key;
            System.out.println("Key:" + (0xec4917aa^0x69A5A771)); 
	    System.out.println("Key:" + (0x5b301715^0xD4020628)); 
	    System.out.println("Key:" + (0x9e3947f8^0x69A5A771)); 
	    System.out.println("Key:" + (0xff981604^0xD4020628)); 
	    String result = Integer.toHexString(0xec4917aa^0x69A5A771)+Integer.toHexString(0x5b301715^0xD4020628)+Integer.toHexString(0x9e3947f8^0x69A5A771)+Integer.toHexString(0xff981604^0xD4020628);
	    System.out.println("Result:" + result); 
	    //System.out.println("Challenge:" + Integer.toHexString((int)challenge));
	    //hash = Long.parseLong(hash.substring(0,8))^high + Long.parseLong(hash.substring(8,16))^low +Long.parseLong(hash.substring(16,24))^high + Long.parseLong(hash.substring(24,32))^low;
            // Gives 0x69a5a771d4020628 (7612674852469737000 decimal)            
            
            this.tr.newTransaction();
            this.tr.setType("QRY");
            this.tr.addArgument(this.ProductID);
            //this.tr.addArgument("32\r\n"+Integer.toHexString((int)challenge));
            System.out.println(this.tr.toStringNN());
            
            this.sh.sendRequest(this.tr.toStringNN());        