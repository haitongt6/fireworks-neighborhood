package com.fireworks.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 后台管理员实体，对应数据库表 {@code ums_admin}。
 */
@Data
@TableName("ums_admin")
public class UmsAdmin {

    /** 主键，自增 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录用户名，全局唯一 */
    private String username;

    /** BCrypt 加密后的密码，禁止明文存储 */
    private String password;

    /** 显示昵称 */
    private String nickname;

    /** 联系邮箱 */
    private String email;

    /** 账号状态：1-启用，0-禁用 */
    private Integer status;

    /** 账号创建时间 */
    private Date createTime;
}
