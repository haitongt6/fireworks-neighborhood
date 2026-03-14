package com.fireworks.common.api.util;

import com.fireworks.model.pojo.UmsRole;

import java.util.List;

public class UserRoleUtil {


    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    public static boolean isSuperAdminEnabled(List<UmsRole> roles) {
        if (roles == null) {
            return false;
        }
        for (UmsRole role : roles) {
            if (role != null && SUPER_ADMIN.equals(role.getName())
                    && role.getStatus() != null && role.getStatus() == 1) {
                return true;
            }
        }
        return false;
    }
}
