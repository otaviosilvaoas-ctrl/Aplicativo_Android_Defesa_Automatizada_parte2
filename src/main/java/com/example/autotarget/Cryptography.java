package com.example.autotarget;

import android.util.Base64;
import android.util.Log;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

/**
 * Classe utilitária para criptografia AES (AV3 Letra B).
 */
public class Cryptography {

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final String AES_MODE = "AES";
    // Chave de 16 caracteres para AES-128
    private static final String FIXED_KEY = "AutoTarget_Key16"; 

    /**
     * Criptografa um texto usando AES e retorna em Base64.
     */
    public static String encrypt(String texto) {
        if (texto == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(FIXED_KEY.getBytes(StandardCharsets.UTF_8), AES_MODE);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(texto.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(encryptedBytes, Base64.NO_WRAP);
        } catch (Exception e) {
            Log.e("Cryptography", "Erro ao criptografar: " + e.getMessage());
            return null;
        }
    }

    /**
     * Descriptografa um texto em Base64 usando AES.
     */
    public static String decrypt(String textoCriptografado) {
        if (textoCriptografado == null) return null;
        try {
            SecretKeySpec secretKey = new SecretKeySpec(FIXED_KEY.getBytes(StandardCharsets.UTF_8), AES_MODE);
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.decode(textoCriptografado, Base64.NO_WRAP);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e("Cryptography", "Erro ao descriptografar: " + e.getMessage());
            return null;
        }
    }
}
