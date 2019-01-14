package org.application.deploy.entity;

import lombok.Data;
import org.application.deploy.K8enum.KBServiceType;
import org.application.deploy.entity.base.KBPort;

import java.util.List;

@Data
public class ServiceInfo {
    // 端口 （必须填写完整）
    private List<KBPort> kbPorts;
    // 绑定的集群IP
    private String clusterIp;
    // 绑定的选择器名称（由部署时， workload label（ workload.user.cattle.io/workloadselector ） 名称决定 注意，选择器会将 service 和 deployment 绑定）
    private String selectorName;
    // 服务名称 （必须小写）
    private String name;
    // 命名空间名称 （必须小写）
    private String namespaceName;

    public KBServiceType kbServiceType;
}
