package org.application.jwt.core;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JWTTokenUtils {
 private static final String AUTHORITIES_KEY = "auth";
 private String secretKey;   //签名密钥
 private long tokenValidityInMilliseconds;  //失效日期
 private long tokenValidityInMillisecondsForRememberMe;  //（记住我）失效日期


 @PostConstruct
 public void init() {
  this.secretKey = "Linyuanmima";
  int secondIn1day = 1000 * 60 * 60 * 24;
  this.tokenValidityInMilliseconds = secondIn1day * 2L;  this.tokenValidityInMillisecondsForRememberMe = secondIn1day * 7L;
 }

 private final static long EXPIRATIONTIME = 432_000_000;
 //创建Token
 public String createToken(Authentication authentication, Boolean rememberMe){
  String authorities = authentication.getAuthorities().stream()  //获取用户的权限字符串，如 USER,ADMIN
    .map(GrantedAuthority::getAuthority)
    .collect(Collectors.joining(","));
  long now = (new Date()).getTime();    //获取当前时间戳
  Date validity;           //存放过期时间
  if (rememberMe){
   validity = new Date(now + this.tokenValidityInMilliseconds);
  }else {
   validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
  }
  return Jwts.builder()         //创建Token令牌
    .setSubject(authentication.getName())   //设置面向用户
    .claim(AUTHORITIES_KEY,authorities)    //添加权限属性
    .setExpiration(validity)      //设置失效时间
    .signWith(SignatureAlgorithm.HS512,secretKey) //生成签名
    .compact();
 }
 //获取用户权限
 public Authentication getAuthentication(String token){
  System.out.println("token:"+token);
  Claims claims = Jwts.parser()       //解析Token的payload
    .setSigningKey(secretKey)
    .parseClaimsJws(token)
    .getBody();
  Collection<? extends GrantedAuthority> authorities =
    Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))   //获取用户权限字符串
    .map(SimpleGrantedAuthority::new)
    .collect(Collectors.toList());             //将元素转换为GrantedAuthority接口集合
  User principal = new User(claims.getSubject(), "", authorities);
  return new UsernamePasswordAuthenticationToken(principal, "", authorities);
 }
 //验证Token是否正确
 public boolean validateToken(String token){
  try {
   Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token); //通过密钥验证Token
   return true;
  } catch (MalformedJwtException e) {         //JWT格式错误
   log.info("Invalid JWT token.");
   log.trace("Invalid JWT token trace: {}", e);
  } catch (ExpiredJwtException e) {         //JWT过期
   log.info("Expired JWT token.");
   log.trace("Expired JWT token trace: {}", e);
  } catch (UnsupportedJwtException e) {        //不支持该JWT
   log.info("Unsupported JWT token.");
   log.trace("Unsupported JWT token trace: {}", e);
  } catch (IllegalArgumentException e) {        //参数错误异常
   log.info("JWT token compact of handler are invalid.");
   log.trace("JWT token compact of handler are invalid trace: {}", e);
  }
  return false;
 }
}