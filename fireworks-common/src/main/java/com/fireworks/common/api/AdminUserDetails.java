package com.fireworks.common.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fireworks.model.pojo.UmsAdmin;
import com.fireworks.model.pojo.UmsPermission;
import com.fireworks.model.pojo.UmsRole;
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
    private List<UmsRole> roleList;

    /** Jackson 反序列化专用 */
    public AdminUserDetails() {
        this.permissionList = new ArrayList<UmsPermission>();
        this.roleList = new ArrayList<UmsRole>();
    }

    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsPermission> permissionList) {
        this(umsAdmin, permissionList, roleListOrDefault(null));
    }

    private static List<UmsRole> roleListOrDefault(List<UmsRole> roleList) {
        return roleList == null ? new ArrayList<UmsRole>() : roleList;
    }

    public AdminUserDetails(UmsAdmin umsAdmin, List<UmsPermission> permissionList, List<UmsRole> roleList) {
        this.umsAdmin = umsAdmin;
        this.permissionList = permissionList == null ? new ArrayList<UmsPermission>() : permissionList;
        this.roleList = roleListOrDefault(roleList);
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

    public List<UmsRole> getRoleList() {
        return roleList;
    }

    public void setRoleList(List<UmsRole> roleList) {
        this.roleList = roleList;
    }

    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = new ArrayList<SimpleGrantedAuthority>();
        for (UmsPermission permission : permissionList) {
            // 仅将 type=2（按钮）加入权限，菜单节点不参与鉴权
            if (permission.getType() != null && permission.getType() == 2) {
                String value = permission.getValue();
                if (value != null && !value.trim().isEmpty()) {
                    authorities.add(new SimpleGrantedAuthority(value));
                }
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
