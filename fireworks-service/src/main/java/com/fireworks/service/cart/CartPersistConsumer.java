package com.fireworks.service.cart;

/**
 * 购物车落库消息消费者（已停用，不再注册为 Spring Bean）。
 * <p>
 * 购物车已改为 {@link com.fireworks.service.impl.OmsCartServiceImpl} 同步落库 MySQL，不再通过 RocketMQ 异步落库。
 * 原 {@code @Profile}、{@code @Component}、{@code @RocketMQMessageListener} 及 {@code RocketMQListener} 实现保留在下方注释块中，供后续恢复 MQ 方案时参考。
 * </p>
 */
public final class CartPersistConsumer {

    private CartPersistConsumer() {
    }
}

/*
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
*/
