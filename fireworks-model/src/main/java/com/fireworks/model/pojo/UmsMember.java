package com.fireworks.model.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * C 端会员实体，对应数据库表 {@code ums_member}。
 * <p>
 * 手机号为唯一登录凭证，首次验证码登录时自动注册。
 * </p>
 */
@Data
@TableName("ums_member")
public class UmsMember implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 手机号（唯一登录凭证） */
    private String phone;

    private String nickname;

    private String avatar;

    /** 0-禁用 1-启用 */
    private Integer status;

    private Date createTime;

    private Date updateTime;
}
