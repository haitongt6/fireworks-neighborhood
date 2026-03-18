package com.fireworks.model.vo;

import com.fireworks.model.pojo.UmsMember;
import lombok.Data;

/**
 * C 端登录成功返回值：JWT Token + 会员基本信息。
 */
@Data
public class MemberLoginVO {

    private String token;

    private String tokenHead;

    private UmsMember member;
}
