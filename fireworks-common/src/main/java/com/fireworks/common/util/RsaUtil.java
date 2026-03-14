package com.fireworks.common.util;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Base64;

/**
 * RSA 工具类，用于前端密码加密传输。
 * <p>前端使用公钥加密密码，后端使用私钥解密，避免明文传输。</p>
 * <p>通用加解密工具，与具体业务解耦。</p>
 */
public final class RsaUtil {

    private static final String ALGORITHM = "RSA";
    private static final int KEY_SIZE = 2048;

    private RsaUtil() {
    }

    /**
     * 生成 RSA 密钥对。
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator gen = KeyPairGenerator.getInstance(ALGORITHM);
            gen.initialize(KEY_SIZE);
            return gen.generateKeyPair();
        } catch (Exception e) {
            throw new IllegalStateException("生成 RSA 密钥对失败", e);
        }
    }

    /**
     * 将公钥转为 Base64 字符串（供前端使用）。
     */
    public static String getPublicKeyBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * 使用私钥解密 Base64 编码的密文。
     *
     * @param base64Encrypted 前端用公钥加密后再 Base64 编码的密文
     * @param privateKey      私钥
     * @return 解密后的明文
     */
    public static String decrypt(String base64Encrypted, PrivateKey privateKey) {
        if (base64Encrypted == null || base64Encrypted.trim().isEmpty()) {
            throw new IllegalArgumentException("密文不能为空");
        }
        try {
            byte[] encrypted = Base64.getDecoder().decode(base64Encrypted.trim());
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("RSA 解密失败，请检查公钥是否有效", e);
        }
    }
}
