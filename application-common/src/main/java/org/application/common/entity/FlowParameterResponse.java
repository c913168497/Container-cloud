package org.application.common.entity;

import lombok.Data;

import java.util.List;
/**
 * 发布，启动停止，销毁、升级、回滚整条流程链 结果集 公用参数
 */
@Data
public class FlowParameterResponse {
    private List<Host> hosts;
    //  容器ID
    private String containerId ;
    //  容器实例 名称
    private String containerName;
    // 集群 地址
    private String clusterAddress;
    // 命名空间
    private String namespaceName;
    // 日志 ID
    private String logId;
}
