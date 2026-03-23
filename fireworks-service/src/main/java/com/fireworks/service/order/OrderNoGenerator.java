package com.fireworks.service.order;

import com.fireworks.model.constant.RedisKeyConstant;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 订单号生成器。
 */
@Component
public class OrderNoGenerator {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");
    private static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

    private final StringRedisTemplate stringRedisTemplate;

    public OrderNoGenerator(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public String generateOrderNo() {
        return TIME_FORMAT.format(new Date()) + nextSeq(RedisKeyConstant.ORDER_SEQ_PREFIX);
    }

    public String generatePayOrderNo() {
        return "P" + TIME_FORMAT.format(new Date()) + nextSeq(RedisKeyConstant.PAY_SEQ_PREFIX);
    }

    private String nextSeq(String prefix) {
        String date = DATE_FORMAT.format(new Date());
        Long seq = stringRedisTemplate.opsForValue().increment(prefix + date);
        if (seq == null) {
            seq = 1L;
        }
        return String.format("%06d", seq);
    }
}
