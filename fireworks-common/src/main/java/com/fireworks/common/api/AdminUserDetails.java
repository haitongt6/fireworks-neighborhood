package com.fireworks.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fireworks.model.UmsAdmin;
import com.fireworks.model.UmsPermission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Spring Security {@link UserDetails} 的管理员实现。
 * <p>
 * 封装 {@link UmsAdmin} 实体及其关联的权限列表，
 * 供 Spring Security 认证框架在认证和鉴权环节使用。
 * </p>
 * <p>
 * 该类会被序列化存入 Redis，因此保留无参构造器供 Jackson 使用。
 * {@link UserDetails} 接口中的衍生字段用 {@link JsonIgnore} 排除，
 * 仅序列化 {@code umsAdmin} 和 {@code permissionList} 两个核心字段。
 * </p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AdminUserDetails implements UserDetails {

    private UmsAdmin umsAdmin;
    private List<UmsPermission> permissionList;

    /** Jackson 反序列化专用 */
    public AdminUserDetails() {
        this.permissionList = new ArrayList<UmsPermission>();
    }

    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsPermission> permissionList) {
        this.umsAdmin = umsAdmin;
        this.permissionList = permissionList == null ? new ArrayList<UmsPermission>() : permissionList;
    }

    public UmsAdmin getUmsAdmin() {
        return umsAdmin;
    }

    public void setUmsAdmin(UmsAdmin umsAdmin) {
        this.umsAdmin = umsAdmin;
    }

    public List<UmsPermission> getPermissionList() {
        return permissionList;
    }

    public void setPermissionList(List<UmsPermission> permissionList) {
        this.permissionList = permissionList;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        for (UmsPermission permission : permissionList) {
            String value = permission.getValue();
            if (value != null && !value.trim().isEmpty()) {
                authorities.add(new SimpleGrantedAuthority(value));
            }
        }
        return authorities;
    }

    @JsonIgnore
    @Override
    public String getPassword() {
        return umsAdmin.getPassword();
    }

    @JsonIgnore
    @Override
    public String getUsername() {
        return umsAdmin.getUsername();
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
        return umsAdmin.getStatus() != null && umsAdmin.getStatus() == 1;
    }
}
