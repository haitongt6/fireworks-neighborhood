package com.fireworks.service.config;

import com.fireworks.common.util.RsaUtil;
import org.springframework.stereotype.Component;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * RSA 密钥对持有者。
 * <p>应用启动时生成密钥对，供公钥接口与解密使用。作为安全基础设施，由 service 层提供，可被 admin/api 等 Web 模块复用。</p>
 */
@Component
public class RsaKeyHolder {

    private final KeyPair keyPair;

    public RsaKeyHolder() {
        this.keyPair = RsaUtil.generateKeyPair();
    }

    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }

    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
}
