package com.kmbbj.backend.games.util;

import com.kmbbj.backend.global.config.exception.ApiException;
import com.kmbbj.backend.global.config.exception.ExceptionEnum;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;

@Component
public class GameEncryptionUtil {

    @Value("${ENCRYPTION_KEY}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;
    private SecretKeySpec secretKey;

    @PostConstruct
    public void init() {
        if (encryptionKey == null || encryptionKey.isEmpty()) {
            throw new IllegalStateException("암호화 키가 설정되지 않았습니다.");
        }
        byte[] key = encryptionKey.getBytes(StandardCharsets.UTF_8);
        key = Arrays.copyOf(key, 16); // AES-128을 위해 16바이트 키 사용
        this.secretKey = new SecretKeySpec(key, "AES");
    }

    public String encryptUUID(UUID uuid) {
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);

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

            byte[] iv = new byte[GCM_IV_LENGTH];
            System.arraycopy(decoded, 0, iv, 0, iv.length);
            byte[] cipherText = new byte[decoded.length - GCM_IV_LENGTH];
            System.arraycopy(decoded, GCM_IV_LENGTH, cipherText, 0, cipherText.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedValue = cipher.doFinal(cipherText);
            return UUID.fromString(new String(decryptedValue, StandardCharsets.UTF_8));
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ApiException(ExceptionEnum.INVALID_ENCRYPTED_ID);
        } catch (Exception e) {
            throw new ApiException(ExceptionEnum.DECRYPTION_FAILED);
        }
    }
}
