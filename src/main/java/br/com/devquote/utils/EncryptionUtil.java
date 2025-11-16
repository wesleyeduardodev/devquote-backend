package br.com.devquote.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
@Component
public class EncryptionUtil {

    private static final String SECRET_KEY = "DevQuote2025SecretKeyForParameterEncryption!";
    private static final String ALGORITHM = "AES";

    private SecretKeySpec getSecretKeySpec() {
        try {
            byte[] key = SECRET_KEY.getBytes(StandardCharsets.UTF_8);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            return new SecretKeySpec(key, ALGORITHM);
        } catch (Exception e) {
            log.error("Erro ao gerar chave de criptografia: {}", e.getMessage());
            throw new RuntimeException("Falha ao inicializar criptografia", e);
        }
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }

        try {
            SecretKeySpec secretKey = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            log.error("Erro ao criptografar valor: {}", e.getMessage());
            throw new RuntimeException("Falha ao criptografar valor", e);
        }
    }

    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }

        try {
            SecretKeySpec secretKey = getSecretKeySpec();
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Erro ao descriptografar valor: {}", e.getMessage());
            throw new RuntimeException("Falha ao descriptografar valor", e);
        }
    }
}
