package com.fireworks.service.cart;



import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

import com.fireworks.model.pojo.OmsCartItem;

import com.fireworks.service.mapper.OmsCartItemMapper;

import org.apache.rocketmq.spring.annotation.ConsumeMode;

import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

import org.apache.rocketmq.spring.core.RocketMQListener;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.context.annotation.Profile;

import org.springframework.stereotype.Component;



/**

 * 购物车落库消息消费者。

 * <p>

 * consumeMode = ORDERLY：同一 MessageQueue 内消息串行消费，

 * 保证同一用户的 add / update / remove / clear 操作按发送顺序落库 MySQL，

 * 解决 Redis 与 MySQL 的弱一致性问题。

 * 消费失败时抛出异常，RocketMQ 自动重试；超过最大重试次数后消息进入死信队列。

 * </p>

 * <p>

 * {@code @Profile("api")}：仅在 api 模块激活该 Consumer，防止 admin 模块因

 * scanBasePackages 扫描到本类而重复注册同一 Consumer Group，

 * 导致 MessageQueue 被 admin/api 两个进程瓜分（非预期消费）。

 * 后续新增的 MQ Consumer（如订单支付）同样遵循此规范，按所属模块打上对应 Profile。

 * </p>

 */

@Profile("api")

@Component

@RocketMQMessageListener(

        topic = CartMqProducer.TOPIC,

        consumerGroup = "cart-consumer-group",

        consumeMode = ConsumeMode.ORDERLY

)

public class CartPersistConsumer implements RocketMQListener<CartPersistMessage> {



    private static final Logger log = LoggerFactory.getLogger(CartPersistConsumer.class);



    private final OmsCartItemMapper cartItemMapper;



    public CartPersistConsumer(OmsCartItemMapper cartItemMapper) {

        this.cartItemMapper = cartItemMapper;

    }



    @Override

    public void onMessage(CartPersistMessage msg) {

        log.debug("购物车落库消费开始, type={}, userId={}, productId={}",

                msg.getType(), msg.getUserId(), msg.getProductId());

        try {

            switch (msg.getType()) {

                case UPSERT:

                    handleUpsert(msg);

                    break;

                case DELETE:

                    handleDelete(msg);

                    break;

                case CLEAR:

                    handleClear(msg);

                    break;

                default:

                    log.warn("未知消息类型: {}", msg.getType());

            }

            log.info("购物车落库消费成功, type={}, userId={}, productId={}",

                    msg.getType(), msg.getUserId(), msg.getProductId());

        } catch (Exception e) {

            log.error("购物车落库消费失败，等待重试, type={}, userId={}, productId={}",

                    msg.getType(), msg.getUserId(), msg.getProductId(), e);

            throw new RuntimeException("购物车落库失败，触发重试", e);

        }

    }



    private void handleUpsert(CartPersistMessage msg) {

        OmsCartItem existing = cartItemMapper.selectOne(

                new LambdaQueryWrapper<OmsCartItem>()

                        .eq(OmsCartItem::getUserId, msg.getUserId())

                        .eq(OmsCartItem::getProductId, msg.getProductId()));

        if (existing == null) {

            OmsCartItem item = new OmsCartItem();

            item.setUserId(msg.getUserId());

            item.setProductId(msg.getProductId());

            item.setQuantity(msg.getQuantity());

            item.setPriceSnapshot(msg.getPriceSnapshot());

            item.setTitleSnapshot(msg.getTitleSnapshot());

            item.setImageSnapshot(msg.getImageSnapshot());

            cartItemMapper.insert(item);

        } else {

            existing.setQuantity(msg.getQuantity());

            existing.setPriceSnapshot(msg.getPriceSnapshot());

            existing.setTitleSnapshot(msg.getTitleSnapshot());

            existing.setImageSnapshot(msg.getImageSnapshot());

            cartItemMapper.updateById(existing);

        }

    }



    private void handleDelete(CartPersistMessage msg) {

        cartItemMapper.delete(

                new LambdaQueryWrapper<OmsCartItem>()

                        .eq(OmsCartItem::getUserId, msg.getUserId())

                        .eq(OmsCartItem::getProductId, msg.getProductId()));

    }



    private void handleClear(CartPersistMessage msg) {

        cartItemMapper.delete(

                new LambdaQueryWrapper<OmsCartItem>()

                        .eq(OmsCartItem::getUserId, msg.getUserId()));

    }

}

