package com.fireworks.service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 阿里云短信服务配置属性。
 * <p>
 * 配置前缀：aliyun.sms
 * </p>
 */
@Component
@ConfigurationProperties(prefix = "aliyun.sms")
public class AliyunSmsProperties {

    /** AccessKey ID，建议通过环境变量或配置中心注入 */
    private String accessKeyId;

    /** AccessKey Secret */
    private String accessKeySecret;

    /** 短信签名名称，需在阿里云短信控制台申请 */
    private String signName;

    /** 验证码短信模板编号，如 SMS_123456789 */
    private String templateCode;

    /** 端点，默认 dypnsapi.aliyuncs.com（号码认证服务） */
    private String endpoint = "dypnsapi.aliyuncs.com";

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
