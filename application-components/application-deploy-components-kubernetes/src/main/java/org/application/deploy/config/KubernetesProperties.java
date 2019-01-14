package org.application.deploy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kubernetes")
@Data
public class KubernetesProperties {

    private String namespaceName;

    // TCP /
    private String agreementType;

    // 日志卷挂载路径
    private String logPath;

    private String port;

}
