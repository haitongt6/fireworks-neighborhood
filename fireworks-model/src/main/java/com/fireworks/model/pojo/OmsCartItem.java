package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 购物车明细实体，对应 {@code oms_cart_item}。
 */
@Data
@TableName("oms_cart_item")
public class OmsCartItem {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 ID */
    private Long userId;

    /** 商品 ID */
    private Long productId;

    /** 购买数量 */
    private Integer quantity;

    /** 加购时的价格快照 */
    private BigDecimal priceSnapshot;

    /** 加购时的标题快照 */
    private String titleSnapshot;

    /** 加购时的主图快照 */
    private String imageSnapshot;

    private Date createTime;

    private Date updateTime;
}
