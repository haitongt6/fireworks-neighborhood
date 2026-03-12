package com.fireworks.common.api;

public class UserDetailsThreadLocal {

    private static ThreadLocal<AdminUserDetails> userDetailsThreadLocal = new ThreadLocal<>();


    public static void setUserDetails(AdminUserDetails userDetails) {
        userDetailsThreadLocal.set(userDetails);
    }

    public static AdminUserDetails getUserDetails() {
        return userDetailsThreadLocal.get();
    }

    public static void removeUserDetails() {
        userDetailsThreadLocal.remove();
    }
}
