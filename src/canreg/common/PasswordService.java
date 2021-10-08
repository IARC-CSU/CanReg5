/**
 * CanReg5 - a tool to input, store, check and analyse cancer registry data.
 * Copyright (C) 2008-2015  International Agency for Research on Cancer
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Morten Johannes Ervik, CSU/IARC, ervikm@iarc.fr
 */


package canreg.common;

import canreg.exceptions.SystemUnavailableException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
// Removed proprietory imports
// import sun.misc.BASE64Encoder;
// import sun.misc.CharacterEncoder;

/**
 * 
 * @author ervikm
 */
public final class PasswordService
{
  private static PasswordService instance;
  
  /**
   * Method to hash the password with a SHA-256 encrypting method 
   * 
   * @param plaintext :the password to be hashed
   * @return generatedPassword : the password hashed with SHA-256
   * @throws canreg.exceptions.SystemUnavailableException
   */
  public synchronized String encrypt(String plaintext, String method) throws SystemUnavailableException {
    MessageDigest md = null;
    try {
      if (method.equalsIgnoreCase("SHA-256")) {
        md = MessageDigest.getInstance(method); //step 2 for password hashes with SHA-256
      } else if (method.equalsIgnoreCase("SHA")) {
        md = MessageDigest.getInstance(method); //step 2 for password hashes with SHA
      }
      md.update(plaintext.getBytes(StandardCharsets.UTF_8)); //step 3 add the salt for the password hashing
    } catch (NoSuchAlgorithmException e) {
      throw new SystemUnavailableException(e.getMessage());
    }
    byte[] raw = md.digest(); //step 4
    return hexEncode(raw); //step 5
  }
  
  /** return an instance of the PasswordService 
   * 
   * @return instance 
   */
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
     *
     * @param bytes 
     * @return
     */
  public static String hexEncode(byte[] bytes) {
    StringBuilder s = new StringBuilder(bytes.length * 2);
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
   *
   * @param s 
   * @return
   * @throws IllegalArgumentException 
   */
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