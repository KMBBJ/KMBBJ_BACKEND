package com.kmbbj.backend.games.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GameEncryptionUtil {

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey;

    // AES-GCM 모드 사용
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        try {
            if (encryptionKey == null || encryptionKey.isEmpty()) {
                throw new IllegalStateException("암호화 키가 설정되지 않았습니다.");
            }
            byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 32); // AES-256을 위해 32바이트 키 사용
            this.secretKey = new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("암호화 키 생성에 실패했습니다.", e);
        }
    }

    public String encryptUUID(UUID uuid) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] encryptedValue = cipher.doFinal(uuid.toString().getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + encryptedValue.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedValue, 0, combined, iv.length, encryptedValue.length);

            return Base64.getUrlEncoder().withoutPadding().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("게임 ID 암호화에 실패했습니다.", e);
        }
    }

    public UUID decryptToUUID(String encrypted) {
        try {
            byte[] decoded = Base64.getUrlDecoder().decode(encrypted);

            byte[] iv = Arrays.copyOfRange(decoded, 0, GCM_IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(decoded, GCM_IV_LENGTH, decoded.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedValue = cipher.doFinal(cipherText);
            String uuidString = new String(decryptedValue, StandardCharsets.UTF_8);

            // UUID 형식 검증
            if (!isValidUUID(uuidString)) {
                throw new IllegalArgumentException("복호화된 문자열이 유효한 UUID 형식이 아닙니다.");
            }

            return UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("유효하지 않은 UUID 형식입니다.", e);
        } catch (Exception e) {
            throw new RuntimeException("게임 ID 복호화에 실패했습니다.", e);
        }
    }

    private boolean isValidUUID(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}