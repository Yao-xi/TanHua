package com.yx.tanhua.sso.service;


import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwsHeader;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class TestJWT {
    /**
     * 密钥
     */
    String secret = "tanhua";
    
    @Test
    public void testCreateToken() {
        
        Map<String, Object> header = new HashMap<>();
        header.put(JwsHeader.TYPE, JwsHeader.JWT_TYPE);
        header.put(JwsHeader.ALGORITHM, "HS256");
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", "1333333333");
        claims.put("id", "2");
        
        // 生成token
        String jwt = Jwts.builder()
            .setHeader(header)  //header，可省略
            .setClaims(claims) //payload，存放数据的位置，不能放置敏感数据，如：密码等
            .signWith(SignatureAlgorithm.HS256, secret) //设置加密方法和加密密钥
            .setExpiration(new Date(System.currentTimeMillis() + 3000)) //设置过期时间，3秒后过期
            .compact();
        
        System.out.println(jwt);
        
    }
    
    @Test
    public void testDecodeToken() {
        String token =
            "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJtb2JpbGUiOiIxMzMzMzMzMzMzIiwiaWQiOiIyIiwiZXhwIjoxNjEwNzEzMTIyfQ.feQcJVcYL5CP5jEcNH0h6onoKIK--mHuAm6_UgO8RqI";
        try {
            // 通过token解析数据
            Map<String, Object> body = Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
            System.out.println(body); //{mobile=1333333333, id=2, exp=1605513392}
        } catch (ExpiredJwtException e) {
            System.out.println("token已经过期！");
        } catch (Exception e) {
            System.out.println("token不合法！");
        }
    }
    
}