package com.yx.tanhua.server.utils;

import com.yx.tanhua.common.pojo.User;

/**
 * 用{@link ThreadLocal}解决多用户访问过程中
 * 可能会出现的{@code user}对象不一致问题
 * <p>
 * 这里把{@code user}对象和当前线程绑定 防止出现多线程造成的数据修改
 */
public class UserThreadLocal {
    private static final ThreadLocal<User> LOCAL = new ThreadLocal<>();
    
    private UserThreadLocal() { }
    
    /**
     * @param user
     *     要与当前线程绑定的user对象
     */
    public static void set(User user) {
        LOCAL.set(user);
    }
    
    /**
     * @return {@link User} 与当前线程绑定的user对象
     */
    public static User get() {
        return LOCAL.get();
    }
    
    public static void remove() {
        LOCAL.remove();
    }
}
