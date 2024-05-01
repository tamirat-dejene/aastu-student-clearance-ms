package g3.scms.model;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import g3.scms.utils.Util;

public class SecurePassword {
  private String salt;
  private String hashedPassword;

  public SecurePassword(String salt, String hashedPassword) {
    this.salt = salt;
    this.hashedPassword = hashedPassword;
  }

  public String getSalt() {
    return salt;
  }

  public void setSalt(String salt) {
    this.salt = salt;
  }

  public String getHashedPassword() {
    return hashedPassword;
  }

  public void setHashedPassword(String hashedPassword) {
    this.hashedPassword = hashedPassword;
  }

  @Override
  public String toString() {
    return salt + ":" + hashedPassword;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    SecurePassword other = (SecurePassword) obj;
    if (salt == null) {
      if (other.salt != null)
        return false;
    } else if (!salt.equals(other.salt))
      return false;
    if (hashedPassword == null) {
      if (other.hashedPassword != null)
        return false;
    } else if (!hashedPassword.equals(other.hashedPassword))
      return false;
    return true;
  }

  static SecurePassword saltAndHashPassword(String password) {
    return saltAndHashPassword(16, password, "SHA-256");
  }

  static SecurePassword saltAndHashPassword(int saltByteSize, String password) {
    return saltAndHashPassword(saltByteSize, password, "SHA-256");
  }

  static SecurePassword saltAndHashPassword(int saltByteSize, String password, String algorithm) {
    String salt = Util.generateSalt(saltByteSize);
    String saltedPasword = salt + ":" + password;

    MessageDigest passwordDigest;
    try {
      passwordDigest = MessageDigest.getInstance(algorithm);
      byte[] hashedPasswordByteArray = passwordDigest.digest(saltedPasword.getBytes());
      return new SecurePassword(salt, Hex.encodeHexString(hashedPasswordByteArray));
    } catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  static SecurePassword saltAndHashPassword(String salt, String password) {
    return saltAndHashPassword(salt, password, "SHA-256");
  }

  static SecurePassword saltAndHashPassword(String salt, String password, String algorithm) {
    String saltedPasword = salt + ":" + password;
    MessageDigest passwordDigest;
    try {
      passwordDigest = MessageDigest.getInstance(algorithm);
      byte[] hashedPasswordByteArray = passwordDigest.digest(saltedPasword.getBytes());
      return new SecurePassword(salt, Hex.encodeHexString(hashedPasswordByteArray));
    } catch (NoSuchAlgorithmException e) {
      System.out.println(e.getMessage());
    }
    return null;
  }

  static boolean authenticatePassword(String inputPassword, String storedPassword) {
    return authenticatePassword(inputPassword, storedPassword, "SHA-256");
  }

  static boolean authenticatePassword(String inputPassword, String storedPassword, String algorithm) {
    var saltandHash = storedPassword.split(":");
    if (saltandHash.length <= 1)
      return false;
    var salt = saltandHash[0];
    return storedPassword.equals(saltAndHashPassword(salt, inputPassword, algorithm).toString());
  }

}