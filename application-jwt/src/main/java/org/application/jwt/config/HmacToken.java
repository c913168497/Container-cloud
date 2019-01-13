package org.application.jwt.config;

import lombok.Data;
import org.apache.shiro.authc.AuthenticationToken;

import java.util.Map;

@Data
public class HmacToken implements AuthenticationToken {
    private String clientKey; //客户标识 （ 可以是 用户名 appid 等）
    private String digest; // 消息摘要
    private String timeStamp; // 时间戳
    private Map<String, String[]> parameters;// 访问参数


    public HmacToken(String clientKey, String digest, String timeStamp, Map<String, String[]> parameters) {
        this.clientKey = clientKey;
        this.digest = digest;
        this.timeStamp = timeStamp;
        this.parameters = parameters;
    }

    @Override
    public Object getPrincipal() {
        return clientKey;
    }

    @Override
    public Object getCredentials() {
        return Boolean.TRUE;
    }
}
