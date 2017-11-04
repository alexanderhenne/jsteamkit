package com.jsteamkit.steam.guard;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class SteamAuthenticator {

    private static final String HMAC_SHA1_ALGORITHM = "HmacSHA1";

    private static final byte[] STEAM_GUARD_CODE_TRANSLATIONS = new byte[] {
            50, 51, 52, 53, 54, 55, 56, 57, 66, 67, 68, 70, 71,
            72, 74, 75, 77, 78, 80, 81, 82, 84, 86, 87, 88, 89
    };

    private String sharedSecret;

    public SteamAuthenticator(String sharedSecret) {
        this.sharedSecret = sharedSecret;
    }

    public String generateCode() {
        return generateCode(TimeAligner.getSteamTime());
    }

    public String generateCode(long time) {
        if (sharedSecret == null || sharedSecret.length() == 0) {
            throw new IllegalStateException("You need to supply a sharedSecret");
        }

        byte[] sharedSecretArray = Base64.getDecoder().decode(sharedSecret);
        byte[] timeArray = new byte[8];

        time /= 30L;

        for (int i = 8; i > 0; i--) {
            timeArray[i - 1] = (byte) time;
            time >>= 8;
        }

        SecretKeySpec signingKey = new SecretKeySpec(sharedSecretArray, HMAC_SHA1_ALGORITHM);
        byte[] hashedData;
        try {
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
            mac.init(signingKey);
            hashedData = mac.doFinal(timeArray);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }

        byte[] codeArray = new byte[5];

        byte b = (byte)(hashedData[19] & 0xF);
        int codePoint = (hashedData[b] & 0x7F) << 24 | (hashedData[b + 1] & 0xFF) << 16 | (hashedData[b + 2] & 0xFF) << 8 | (hashedData[b + 3] & 0xFF);

        for (int i = 0; i < 5; i++) {
            codeArray[i] = STEAM_GUARD_CODE_TRANSLATIONS[codePoint % STEAM_GUARD_CODE_TRANSLATIONS.length];
            codePoint /= STEAM_GUARD_CODE_TRANSLATIONS.length;
        }

        return new String(codeArray);
    }
}
