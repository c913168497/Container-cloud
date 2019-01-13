package org.application.jwt.realm;

import org.apache.shiro.authc.*;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.crypto.hash.Sha256Hash;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.application.jwt.entity.User;
import org.application.jwt.service.UserService;

import java.util.List;
import java.util.Optional;

public class UrlShiroRealm extends AuthorizingRealm {

    private static final String encryptSalt = "deqweeq131231r1";
    private UserService userService;

    public UrlShiroRealm(UserService userService) {
        this.userService = userService;
        this.setCredentialsMatcher(new HashedCredentialsMatcher(Sha256Hash.ALGORITHM_NAME));
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        UsernamePasswordToken usernamePasswordToken = (UsernamePasswordToken) authenticationToken;
        String username = usernamePasswordToken.getUsername();
        User user = userService.getUserInfoByUserName();
        if (!Optional.ofNullable(user).isPresent()){
            throw new AuthenticationException("密码错误");
        }

        return new SimpleAuthenticationInfo(user, user.getPassword(),  ByteSource.Util.bytes(encryptSalt), this.getName());
    }



    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
        User user = (User) principals.getPrimaryPrincipal();
        List<String> roles = user.getRoleIds();
        if(roles == null) {
            roles = userService.getUserRoles(user.getUserId());
        }
        if (roles != null)
            simpleAuthorizationInfo.addRoles(roles);

        return simpleAuthorizationInfo;
    }
}
