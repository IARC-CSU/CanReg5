package canreg.common;

import canreg.exceptions.SystemUnavailableException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
// Removed proprietory imports
// import sun.misc.BASE64Encoder;
// import sun.misc.CharacterEncoder;

public final class PasswordService
{
  private static PasswordService instance;

  private PasswordService()
  {
  }

  public synchronized String encrypt(String plaintext) throws SystemUnavailableException
  {
    MessageDigest md = null;
    try
    {
      md = MessageDigest.getInstance("SHA"); //step 2
    }
    catch(NoSuchAlgorithmException e)
    {
      throw new SystemUnavailableException(e.getMessage());
    }
    try
    {
      md.update(plaintext.getBytes("UTF-8")); //step 3
    }
    catch(UnsupportedEncodingException e)
    {
      throw new SystemUnavailableException(e.getMessage());
    }

    byte raw[] = md.digest(); //step 4
    // Old way:
    // String hash = (new BASE64Encoder()).encode(raw); //step 5
    // new way:
    String hash = hexEncode(raw);    
    return hash; //step 6
  }
  
  public static synchronized PasswordService getInstance() //step 1
  {
    if(instance == null)
    {
       instance = new PasswordService(); 
    } 
    return instance;
  }
  
    /** This array is used to convert from bytes to hexadecimal numbers */
  static final char[] digits = { '0', '1', '2', '3', '4', '5', '6', '7',
                                 '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
  
    /**
   * A convenience method to convert an array of bytes to a String.  We do
   * this simply by converting each byte to two hexadecimal digits.  Something
   * like Base 64 encoding is more compact, but harder to encode.
   **/
  public static String hexEncode(byte[] bytes) {
    StringBuffer s = new StringBuffer(bytes.length * 2);
    for(int i = 0; i < bytes.length; i++) {
      byte b = bytes[i];
      s.append(digits[(b & 0xf0) >> 4]);
      s.append(digits[b & 0x0f]);
    }
    return s.toString();
  }

  /**
   * A convenience method to convert in the other direction, from a string
   * of hexadecimal digits to an array of bytes.
   **/
  public static byte[] hexDecode(String s) throws IllegalArgumentException {
    try {
      int len = s.length();
      byte[] r = new byte[len/2];
      for(int i = 0; i < r.length; i++) {
        int digit1 = s.charAt(i*2), digit2 = s.charAt(i*2 + 1);
        if ((digit1 >= '0') && (digit1 <= '9')) digit1 -= '0';
        else if ((digit1 >= 'a') && (digit1 <= 'f')) digit1 -= 'a' - 10;
        if ((digit2 >= '0') && (digit2 <= '9')) digit2 -= '0';
        else if ((digit2 >= 'a') && (digit2 <= 'f')) digit2 -= 'a' - 10;
        r[i] = (byte)((digit1 << 4) + digit2);
      }
      return r;
    }
    catch (Exception e) {
      throw new IllegalArgumentException("hexDecode(): invalid input");
    }
  }
}