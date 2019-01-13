package org.application.jwt.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.application.jwt.config.HmacToken;

import java.util.ArrayList;
import java.util.List;

public class HmacRealm extends AuthorizingRealm {

    public Class<?> getAuthenticationTokenClass() {
        return HmacToken.class; // 表示此 Realm 只支持 HmacToken;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {


        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        HmacToken hmacToken  = (HmacToken) authenticationToken;
        List<String> keys = new ArrayList<>();
        for (String key : hmacToken.getParameters().keySet()) {
            if (!"digest".equals( key )){
                keys.add(key);
            }
        }
        return null;
    }
}
