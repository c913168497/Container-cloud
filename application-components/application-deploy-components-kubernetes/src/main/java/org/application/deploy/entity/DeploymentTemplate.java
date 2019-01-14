package org.application.deploy.entity;


import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import lombok.Data;
import org.application.deploy.entity.base.KBResource;
import org.application.deploy.entity.base.KBVolumeInfo;

import java.util.List;

@Data
public class DeploymentTemplate {
    private String name;
    // pod 数量
    private Integer replicas;
    // 镜像地址
    private String imageAddress;
    // 环境变量
    private List<EnvVar> envVars;
    //容器端口绑定
    private List<ContainerPort> containerPort;
    // 发布机器资源控制
    private KBResource kbResource;
    // 选择器名称，作为 service 绑定时的标识 值
    private String selectorName;
    // 卷属性信息
    private List<KBVolumeInfo> volumeInfos;
    // 命令空间名称
    private String namespace;
    // 发布节点机器指定
    private String nodeName;
}
