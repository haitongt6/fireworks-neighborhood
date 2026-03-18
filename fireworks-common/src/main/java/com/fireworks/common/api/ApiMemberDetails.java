package com.fireworks.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fireworks.model.pojo.UmsMember;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

/**
 * Spring Security {@link UserDetails} 的 C 端会员实现。
 * <p>
 * 与 {@link AdminUserDetails} 平行，封装 {@link UmsMember}，
 * 供 fireworks-api 模块的 JWT 认证过滤器使用。
 * </p>
 * <p>
 * C 端无角色/权限体系，{@link #getAuthorities()} 始终返回空集合。
 * 该类会被序列化存入 Redis，保留无参构造器供 Jackson 使用。
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiMemberDetails implements UserDetails {

    private UmsMember member;

    /** Jackson 反序列化专用 */
    public ApiMemberDetails() {}

    public ApiMemberDetails(UmsMember member) {
        this.member = member;
    }

    public UmsMember getMember() {
        return member;
    }

    public void setMember(UmsMember member) {
        this.member = member;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.emptyList();
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return "";
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return member != null ? member.getPhone() : null;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return member != null && member.getStatus() != null && member.getStatus() == 1;
    }
}
