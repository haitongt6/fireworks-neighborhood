package com.fireworks.service.impl;

import com.aliyun.dypnsapi20170525.Client;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.teautil.models.RuntimeOptions;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fireworks.model.constant.RedisKeyConstant;
import com.fireworks.service.SmsService;
import com.fireworks.service.config.AliyunSmsProperties;
import com.fireworks.service.exception.SmsLimitException;
import com.fireworks.service.exception.SmsSendException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 短信服务实现类。
 * <p>
 * 使用 Redis Lua 脚本实现原子限流，接入阿里云短信 SDK 发送验证码，
 * 验证码存入 Redis，Key 为 auth:code:phone:{phone}，有效期 5 分钟。
 * </p>
 */
@Service
public class SmsServiceImpl implements SmsService {

    private static final Logger log = LoggerFactory.getLogger(SmsServiceImpl.class);

    private static final int CODE_LENGTH = 6;
    private static final int CODE_EXPIRE_MINUTES = 5;
    private static final int INTERVAL_SECONDS = 60;
    private static final int DAILY_EXPIRE_SECONDS = 86400;

    private final StringRedisTemplate stringRedisTemplate;
    private final Client aliyunSmsClient;
    private final AliyunSmsProperties smsProperties;
    private final ObjectMapper objectMapper;

    /** Lua 脚本（类加载时读取，避免重复编译） */
    private static final DefaultRedisScript<Long> RATE_LIMIT_SCRIPT = loadRateLimitScript();

    private static DefaultRedisScript<Long> loadRateLimitScript() {
        try {
            ClassPathResource resource = new ClassPathResource("scripts/sms-rate-limit.lua");
            String script = StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<Long>();
            redisScript.setScriptText(script);
            redisScript.setResultType(Long.class);
            return redisScript;
        } catch (Exception e) {
            throw new IllegalStateException("加载短信限流 Lua 脚本失败", e);
        }
    }

    public SmsServiceImpl(StringRedisTemplate stringRedisTemplate,
                          ObjectProvider<Client> clientProvider,
                          AliyunSmsProperties smsProperties,
                          ObjectMapper objectMapper) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.aliyunSmsClient = clientProvider.getIfAvailable();
        this.smsProperties = smsProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public void sendVerifyCode(String phone) {
        if (!StringUtils.hasText(phone) || !isValidPhone(phone)) {
            throw new IllegalArgumentException("手机号格式不正确");
        }

        String intervalKey = RedisKeyConstant.SMS_LIMIT_INTERVAL_PREFIX + phone;
        String dailyKey = RedisKeyConstant.SMS_LIMIT_DAILY_PREFIX + phone;

        Long result = stringRedisTemplate.execute(
                RATE_LIMIT_SCRIPT,
                Arrays.asList(intervalKey, dailyKey),
                String.valueOf(INTERVAL_SECONDS),
                String.valueOf(DAILY_EXPIRE_SECONDS)
        );

        if (result == null) {
            throw new SmsSendException("短信限流校验异常，请稍后再试");
        }

        if (result == -1) {
            throw new SmsLimitException(-1, "请稍后再试");
        }
        if (result == -2) {
            throw new SmsLimitException(-2, "今日发送次数已达上限");
        }

        String code = generateCode();
        doSend(phone, code);

        String codeKey = RedisKeyConstant.AUTH_CODE_PHONE_PREFIX + phone;
        stringRedisTemplate.opsForValue().set(codeKey, code, CODE_EXPIRE_MINUTES, TimeUnit.MINUTES);
        log.info("短信验证码已发送: phone={}", maskPhone(phone));
    }

    private void doSend(String phone, String code) {
        if (aliyunSmsClient == null) {
            throw new SmsSendException("阿里云短信未配置，请在配置文件中设置 aliyun.sms.*");
        }

        Map<String, String> templateParam = new HashMap<String, String>(2);
        templateParam.put("code", code);
        templateParam.put("min", "5");
        String templateParamJson;
        try {
            templateParamJson = objectMapper.writeValueAsString(templateParam);
        } catch (JsonProcessingException e) {
            throw new SmsSendException("模板参数序列化失败", e);
        }

        SendSmsVerifyCodeRequest request = new SendSmsVerifyCodeRequest()
                .setSignName(smsProperties.getSignName())
                .setTemplateCode(smsProperties.getTemplateCode())
                .setPhoneNumber(phone)
                .setTemplateParam(templateParamJson);

        RuntimeOptions runtime = new RuntimeOptions();
        try {
            SendSmsVerifyCodeResponse response = aliyunSmsClient.sendSmsVerifyCodeWithOptions(request, runtime);
            if (response == null || response.getBody() == null) {
                throw new SmsSendException("短信发送无响应");
            }
            String bizCode = response.getBody().getCode();
            if (bizCode != null && !"OK".equalsIgnoreCase(bizCode)) {
                String msg = response.getBody().getMessage() != null
                        ? response.getBody().getMessage()
                        : "短信发送失败";
                log.warn("阿里云短信发送失败: phone={}, code={}, message={}",
                        maskPhone(phone), bizCode, msg);
                throw new SmsSendException("短信发送失败：" + msg);
            }
        } catch (SmsSendException e) {
            throw e;
        } catch (Exception e) {
            log.error("短信发送异常: phone={}", maskPhone(phone), e);
            throw new SmsSendException("短信发送失败，请稍后再试", e);
        }
    }

    private static String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private static boolean isValidPhone(String phone) {
        return phone != null && phone.matches("^1[3-9]\\d{9}$");
    }

    private static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
