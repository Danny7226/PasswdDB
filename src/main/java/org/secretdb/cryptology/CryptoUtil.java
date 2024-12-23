package org.secretdb.cryptology;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

public class CryptoUtil {
    public static String encrypt(String algorithm, String input, SecretKey key, IvParameterSpec iv) {
        try {
            final Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            final byte[] cipherText = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder()
                    .encodeToString(cipherText);
        } catch (final NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                InvalidKeyException | BadPaddingException | IllegalBlockSizeException e) {
           throw new RuntimeException("Exception encrypting an input", e);
        }
    }

    /**
     *
     * @param algorithm to use for crypto
     * @param cipherText
     * @param key
     * @param iv
     * @return
     * @throws BadPaddingException, indicates bad secret key
     */
    public static String decrypt(String algorithm, String cipherText, SecretKey key, IvParameterSpec iv) throws BadPaddingException {
        try {
            System.out.println("Decrypting encoded cipher " + cipherText);
            final Cipher cipher = Cipher.getInstance(algorithm);
            cipher.init(Cipher.DECRYPT_MODE, key, iv);
            final byte[] decoded = Base64.getDecoder().decode(cipherText);
            final byte[] plainText = cipher.doFinal(decoded);
            return new String(plainText, StandardCharsets.UTF_8);
        } catch (final NoSuchPaddingException | NoSuchAlgorithmException | InvalidAlgorithmParameterException | InvalidKeyException | IllegalBlockSizeException e) {
            throw new RuntimeException("Exception encrypting an input", e);
        }
    }

    public static SecretKey getPBEKey(final String privateKey, String salt) {
        try {
            final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            final KeySpec spec = new PBEKeySpec(privateKey.toCharArray(), salt.getBytes(), 65536, 256);
            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");
        } catch (final NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Exception generating PBE key", e);
        }
    }

    public static IvParameterSpec generateIv(final byte[] iv) {
        return new IvParameterSpec(iv);
    }
}
