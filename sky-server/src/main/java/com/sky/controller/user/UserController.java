package com.sky.controller.user;

import com.sky.entity.User;
import com.sky.result.Result;
import com.sky.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户相关接口
 * 本项目用户端通过JSESSIONID会话维持登录态：登录成功后返回sessionId，前端后续请求携带该会话
 */
@RestController
@RequestMapping("/user")
@Api(tags = "用户相关接口")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 微信登录
     * 前端提交：{phone字段携带微信登录code, avatar, name, sex}
     * @param user
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("微信登录")
    public Result<Map<String, String>> login(@RequestBody User user, HttpServletRequest request) {
        log.info("微信用户登录：{}", user);

        User dbUser = userService.wxLogin(user);

        //将当前登录用户id存入会话，供后续请求鉴权及BaseContext使用
        HttpSession session = request.getSession();
        session.setAttribute("userId", dbUser.getId());

        Map<String, String> data = new HashMap<>();
        data.put("sessionId", session.getId());
        return Result.success(data);
    }
}
