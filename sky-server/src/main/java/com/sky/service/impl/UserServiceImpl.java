package com.sky.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    //微信服务接口地址
    public static final String WX_LOGIN_URL = "https://api.weixin.qq.com/sns/jscode2session";

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private WeChatProperties weChatProperties;

    /**
     * 微信登录
     * @param user 前端提交的用户信息，phone字段携带微信登录code
     * @return
     */
    public User wxLogin(User user) {
        String openid = getOpenid(user.getPhone());

        //判断是否为新用户，是新用户则自动完成注册
        User dbUser = userMapper.getByOpenid(openid);
        if (dbUser == null) {
            dbUser = User.builder()
                    .openid(openid)
                    .name(user.getName())
                    .avatar(user.getAvatar())
                    .sex(user.getSex())
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(dbUser);
        }
        return dbUser;
    }

    /**
     * 通过微信code换取openid；本地未配置微信凭证时使用固定的模拟openid，保证本地登录的用户、
     * 购物车和历史订单在多次登录间保持连续
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        String appid = weChatProperties.getAppid();
        if (appid == null || appid.isEmpty() || appid.contains("请填写")) {
            log.warn("微信凭证未配置，使用模拟openid登录");
            return "mock_user_local";
        }

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("appid", appid);
        paramMap.put("secret", weChatProperties.getSecret());
        paramMap.put("js_code", code);
        paramMap.put("grant_type", "authorization_code");
        String response = HttpClientUtil.doGet(WX_LOGIN_URL, paramMap);

        JSONObject jsonObject = JSON.parseObject(response);
        String openid = jsonObject.getString("openid");
        if (openid == null) {
            log.error("微信登录失败：{}", response);
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }
        return openid;
    }
}
