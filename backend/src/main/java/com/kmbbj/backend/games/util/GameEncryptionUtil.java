package com.kmbbj.backend.games.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Component
public class GameEncryptionUtil {

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey;


    // AES 암호화 알고리즘을 사용 (ECB 모드 + PKCS5Padding)
    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        try {
            if (encryptionKey == null || encryptionKey.isEmpty()) {
                throw new RuntimeException("암호화 키가 설정되지 않았습니다.");
            }
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            this.secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new RuntimeException("암호화 키 생성에 실패했습니다.", e);
        }
    }

    public String encryptUUID(UUID uuid) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedValue = cipher.doFinal(uuid.toString().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedValue);
        } catch (Exception e) {
            throw new RuntimeException("게임 ID 암호화에 실패했습니다.", e);
        }
    }

    public UUID decryptToUUID(String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedValue = Base64.getDecoder().decode(encrypted);
            byte[] decryptedValue = cipher.doFinal(decodedValue);
            return UUID.fromString(new String(decryptedValue, StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("게임 ID 복호화에 실패했습니다.", e);
        }
    }
}
