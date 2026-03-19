package com.fireworks.service.cart;

import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 购物车落库消息生产者。
 * <p>
 * 使用顺序消息：hashKey = userId，保证同一用户的所有写操作消息
 * 路由到同一个 MessageQueue，Consumer 串行消费，MySQL 落库顺序
 * 与 Redis 操作顺序严格一致。
 * </p>
 */
@Component
public class CartMqProducer {

    private static final Logger log = LoggerFactory.getLogger(CartMqProducer.class);
    static final String TOPIC = "cart-persist";

    private final RocketMQTemplate rocketMQTemplate;

    public CartMqProducer(RocketMQTemplate rocketMQTemplate) {
        this.rocketMQTemplate = rocketMQTemplate;
    }

    /**
     * 发送 upsert 顺序消息（加购 / 改数量）。
     */
    public void sendUpsert(Long userId, Long productId, Integer quantity,
                           BigDecimal priceSnapshot, String titleSnapshot, String imageSnapshot) {
        CartPersistMessage msg = new CartPersistMessage();
        msg.setType(CartPersistMessage.Type.UPSERT);
        msg.setUserId(userId);
        msg.setProductId(productId);
        msg.setQuantity(quantity);
        msg.setPriceSnapshot(priceSnapshot);
        msg.setTitleSnapshot(titleSnapshot);
        msg.setImageSnapshot(imageSnapshot);
        sendOrderly(msg);
    }

    /**
     * 发送 delete 顺序消息（删除单项）。
     */
    public void sendDelete(Long userId, Long productId) {
        CartPersistMessage msg = new CartPersistMessage();
        msg.setType(CartPersistMessage.Type.DELETE);
        msg.setUserId(userId);
        msg.setProductId(productId);
        sendOrderly(msg);
    }

    /**
     * 发送 clear 顺序消息（清空购物车）。
     */
    public void sendClear(Long userId) {
        CartPersistMessage msg = new CartPersistMessage();
        msg.setType(CartPersistMessage.Type.CLEAR);
        msg.setUserId(userId);
        sendOrderly(msg);
    }

    /**
     * 顺序发送核心方法。
     * hashKey 使用 userId，保证同一用户消息路由到同一 MessageQueue。
     */
    private void sendOrderly(CartPersistMessage msg) {
        try {
            rocketMQTemplate.sendOneWayOrderly(
                    TOPIC,
                    MessageBuilder.withPayload(msg).build(),
                    String.valueOf(msg.getUserId())
            );
            log.info("购物车 MQ 顺序消息已发送, type={}, userId={}, productId={}",
                    msg.getType(), msg.getUserId(), msg.getProductId());
        } catch (Exception e) {
            log.error("购物车 MQ 消息发送失败, type={}, userId={}, productId={}",
                    msg.getType(), msg.getUserId(), msg.getProductId(), e);
            throw new RuntimeException("购物车消息发送失败，请重试", e);
        }
    }
}
