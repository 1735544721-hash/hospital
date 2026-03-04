package org.example.springboot.util;


import cn.hutool.core.date.DateUtil;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.example.springboot.entity.User;
import org.example.springboot.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;

@Component
public class JwtTokenUtils {
    private static UserService staticUserService;
    
    // 使用固定的JWT密钥，而不是用户密码
    private static String jwtSecret;
    
    @Resource
    private UserService userService;
    
    @Value("${jwt.secret:hospital-management-system-secret-key-2024}")
    private String configJwtSecret;
    
    public static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenUtils.class);
    
    @PostConstruct
    public void setUserService() {
        staticUserService = userService;
        jwtSecret = configJwtSecret;
    }
    
    /**
     * 生成token - 使用固定密钥
     */
    public static String genToken(String userId, String sign) {
        return JWT.create()
                .withAudience(userId)
                .withExpiresAt(DateUtil.offsetHour(new Date(), 2))
                .sign(Algorithm.HMAC256(jwtSecret));
    }
    /**
     * 获取当前登录用户 - 验证token签名
     */
    public static User getCurrentUser() {
        String token = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");
            if (StringUtils.isBlank(token)) {
                token = request.getParameter("token");
            }
            if (StringUtils.isBlank(token)) {
                LOGGER.error("获取当前登录的token失败，token为空");
                return null;
            }

            // 验证token签名并解码
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT jwt = verifier.verify(token);
            
            String userId = jwt.getAudience().get(0);
            return staticUserService.getUserById(Long.valueOf(userId));
        } catch (Exception e) {
            LOGGER.error("获取当前用户信息失败，token: {}", token, e);
            return null;
        }
    }
    
    /**
     * 获取当前登录用户ID - 验证token签名
     */
    public static Long getCurrentUserId() {
        String token = null;
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            token = request.getHeader("token");
            if (StringUtils.isBlank(token)) {
                token = request.getParameter("token");
            }
            if (StringUtils.isBlank(token)) {
                LOGGER.error("获取当前登录的token失败，token为空");
                return null;
            }

            // 验证token签名并解码
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtSecret)).build();
            DecodedJWT jwt = verifier.verify(token);
            
            String userId = jwt.getAudience().get(0);
            return Long.valueOf(userId);
        } catch (Exception e) {
            LOGGER.error("获取当前用户ID失败，token: {}", token, e);
            return null;
        }
    }
}
