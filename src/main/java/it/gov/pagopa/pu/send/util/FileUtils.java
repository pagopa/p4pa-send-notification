package it.gov.pagopa.pu.send.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileUtils {

  private FileUtils() {
  }

  public static String calculateFileHash(File file)
    throws NoSuchAlgorithmException, IOException {
    // Create a MessageDigest instance for SHA-256
    MessageDigest digest = MessageDigest.getInstance("SHA-256");
    // Create an InputStream to read the file
    try (FileInputStream fis = new FileInputStream(file)) {
      byte[] buffer = new byte[1024];
      int bytesRead;

      while ((bytesRead = fis.read(buffer)) != -1) {
        digest.update(buffer, 0, bytesRead);
      }
    }
    // Convert byte array into a hexadecimal string
    byte[] hashBytes = digest.digest();
    StringBuilder hexString = new StringBuilder();
    for (byte b : hashBytes) {
      String hex = Integer.toHexString(0xff & b);
      if (hex.length() == 1) {
        hexString.append('0');
      }
      hexString.append(hex);
    }
    return hexString.toString();
  }
}
