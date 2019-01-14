package org.application.deploy.K8enum;

public enum KBServiceType {
    // 开启外部访问 service
    NodePort,
    // 只允许集群内访问
    ClusterIP
}
