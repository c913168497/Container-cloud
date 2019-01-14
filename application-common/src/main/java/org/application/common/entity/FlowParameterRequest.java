package org.application.common.entity;

import lombok.Data;

import java.util.List;

/**
 * 发布，启动停止，销毁、升级、回滚整条流程链请求公用参数
 */
@Data
public class FlowParameterRequest {

    // 发布ip
    private String ip;
    // 镜像地址
    private String imageUrl;
    // 环境变量
    private List<InstVar> envs;
    // 集群地址
    private String clusterAddress;
    // 当前版本号
    private String version ;
    // 容器名称
    private String containerName;
    // 命名空间
    private String namespaceName;
    //实例id
    private String instId;
    //日志文件id
    private String logUuid;
}
