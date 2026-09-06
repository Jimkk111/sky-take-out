package com.sky.interceptor;

import com.sky.context.BaseContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 用户端登录校验拦截器
 * 本项目用户端通过JSESSIONID会话维持登录态：登录接口将userId写入会话，此拦截器校验会话中的登录标记
 */
@Component
@Slf4j
public class UserLoginInterceptor implements HandlerInterceptor {

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //拦截到的不是Controller方法（如静态资源），直接放行
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HttpSession session = request.getSession();
        Object userId = session.getAttribute("userId");
        if (userId == null) {
            //未登录，响应401状态码
            response.setStatus(401);
            return false;
        }

        BaseContext.setCurrentId((Long) userId);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        BaseContext.removeCurrentId();
    }
}
