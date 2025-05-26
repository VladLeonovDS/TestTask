package org.deathstrix.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class AuthUtils {

    public static boolean validateInitData(String initData, String botToken) {
        Map<String, String> params = parseInitData(initData);

        String receivedHash = params.remove("hash");
        if (receivedHash == null) return false;

        String dataCheckString = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String secretKey = bytesToHex(sha256(botToken.getBytes(StandardCharsets.UTF_8)));
        String computedHash = hmacSha256(dataCheckString, secretKey);

        return computedHash.equals(receivedHash) &&
                isRecent(Long.parseLong(params.get("auth_date")));
    }

    public static Map<String, String> parseInitData(String initData) {
        Map<String, String> map = new HashMap<>();
        for (String pair : initData.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(URLDecoder.decode(kv[0], StandardCharsets.UTF_8),
                        URLDecoder.decode(kv[1], StandardCharsets.UTF_8));
            }
        }
        return map;
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hexToBytes(key), "HmacSHA256");
            sha256Hmac.init(secretKey);
            return bytesToHex(sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private static byte[] sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(input);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 error", e);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] res = new byte[len / 2];
        for (int i = 0; i < len; i += 2)
            res[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        return res;
    }

    private static boolean isRecent(long authDate) {
        long now = System.currentTimeMillis() / 1000;
        return now - authDate < 86400;
    }
}
