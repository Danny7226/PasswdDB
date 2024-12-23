package org.secretdb.cryptology;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.security.InvalidKeyException;

public class Crypto {
    private static final byte[] IV = { 8, 8, 8, 8, 4, 4, 4, 4, 2, 2, 2, 2, 1, 0, 1, 0 };
    private static final String SALT = "secret_salt_am_i";
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    private final IvParameterSpec ivParameterSpec;

    public Crypto() {
        this.ivParameterSpec = CryptoUtil.generateIv(IV);
    }

    public String encrypt(final String privateKey, final String input) {
        final SecretKey secretKey = CryptoUtil.getPBEKey(privateKey, SALT);
        return CryptoUtil.encrypt(ALGORITHM, input, secretKey, this.ivParameterSpec);
    }

    public String decrypt(final String privateKey, final String input) throws BadPaddingException {
        final SecretKey secretKey = CryptoUtil.getPBEKey(privateKey, SALT);
        return CryptoUtil.decrypt(ALGORITHM, input, secretKey, this.ivParameterSpec);
    }
}
