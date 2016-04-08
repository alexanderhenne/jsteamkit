package com.jsteamkit.util;

import com.google.common.base.Throwables;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.*;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.zip.CRC32;

public class CryptoUtil {

    public static byte[] shaHash(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-1").digest(data);
        } catch (NoSuchAlgorithmException e) {
            Throwables.propagate(e);
        }
        return null;
    }

    public static byte[] encryptWithRsa(byte[] data, BigInteger modulus, BigInteger exponent)
            throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException,
            InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {

        RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);

        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        RSAPublicKey rsaPublicKey = (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);

        Security.addProvider(new BouncyCastleProvider());
        Cipher cipher = Cipher.getInstance("RSA/None/OAEPWithSHA1AndMGF1Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);

        return cipher.doFinal(data);
    }

    public static byte[] encryptSymmetrically(byte[] data, byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException,
            InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {

        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));

        byte[] iv = getRandomBytes(16);
        byte[] encryptedIv = cipher.doFinal(iv);

        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        byte[] encryptedText = cipher.doFinal(data);

        byte[] output = new byte[encryptedIv.length + encryptedText.length];
        System.arraycopy(encryptedIv, 0, output, 0, encryptedIv.length);
        System.arraycopy(encryptedText, 0, output, encryptedIv.length, encryptedText.length);

        return output;
    }

    public static byte[] decryptSymmetrically(byte[] data, byte[] key)
            throws NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException,
            InvalidKeySpecException, InvalidKeyException, IllegalBlockSizeException,
            BadPaddingException, InvalidAlgorithmParameterException {

        byte[] encryptedIv = Arrays.copyOfRange(data, 0, 16);
        byte[] encryptedText = Arrays.copyOfRange(data,
                encryptedIv.length,
                encryptedIv.length + (data.length - encryptedIv.length));

        Cipher cipher = Cipher.getInstance("AES/ECB/NoPadding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        byte[] iv = cipher.doFinal(encryptedIv);

        cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BC");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));

        return cipher.doFinal(encryptedText);
    }

    public static byte[] getCrcHash(byte[] data) {
        CRC32 crc32 = new CRC32();
        crc32.update(data);
        long crcHash = crc32.getValue();

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt((int) crcHash);

        return byteBuffer.array();
    }

    public static byte[] getRandomBytes(int len) {
        byte[] block = new byte[len];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(block);
        return block;
    }
}
