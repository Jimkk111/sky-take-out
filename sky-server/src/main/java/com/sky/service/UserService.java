package com.sky.service;

import com.sky.entity.User;

public interface UserService {

    /**
     * 微信登录（本地未配置微信凭证时使用模拟openid）
     * @param user 前端提交的用户信息，phone字段携带微信登录code
     * @return
     */
    User wxLogin(User user);
}
