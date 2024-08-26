package com.megadeploy.annotations.datatypes;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

public class SecureString {
    private byte[] encryptedValue;
    private SecretKey secretKey;

    public SecureString(String plaintext) throws Exception {
        this.secretKey = generateKey();
        this.encryptedValue = encrypt(plaintext);
    }

    private byte[] encrypt(String plaintext) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(plaintext.getBytes());
    }

    public String decrypt() throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(encryptedValue));
    }

    // Method to securely wipe the encrypted value from memory
    public void wipe() {
        Arrays.fill(encryptedValue, (byte) 0);
        encryptedValue = null;
        secretKey = null;
    }

    private SecretKey generateKey() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256, new SecureRandom());
        return keyGen.generateKey();
    }

    @Override
    public String toString() {
        return Base64.getEncoder().encodeToString(encryptedValue);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj instanceof SecureString) {
            SecureString other = (SecureString) obj;
            return Arrays.equals(this.encryptedValue, other.encryptedValue);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(encryptedValue);
    }
}
