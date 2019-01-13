package org.application.jwt.shiro;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.application.jwt.config.HmacToken;
import org.application.jwt.core.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;

public class MyRealm extends AuthorizingRealm {

    @Autowired
    private JwtUtil jwtUtil;


    /**
     * 默认使用此方法进行用户名正确与否验证， 错误抛出异常
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    /**
     * 重写 token 继承
     * @param token
     * @return
     */
    public boolean supports(AuthenticationToken token){
        return token instanceof HmacToken;
    }

    /**
     * 检测用户权限的 时候 才会调用此方法。 checkRole . checkPermission 之类
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        return null;
    }
}
