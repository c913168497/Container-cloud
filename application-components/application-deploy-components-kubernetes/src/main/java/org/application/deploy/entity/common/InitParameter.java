package org.application.deploy.entity.common;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import lombok.Data;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.listener.EventListener;
import org.application.deploy.entity.base.KBResource;
import org.application.deploy.entity.base.KBVolumeInfo;
import org.application.deploy.service.KubernetesService;

import java.util.List;
import java.util.Map;

@Data
public class InitParameter<T,O> {

    private String namespaceName;

    private String deploymentName;

    private String nodeName;

    private String protocol;

    private String clusterName;

    private String containerName;
    /**
     * 环境变量
     */
    private List<EnvVar> envVars;

    private List<ContainerPort> containerPorts;

    private List<KBVolumeInfo> kbVolumeInfos;

    private KBResource kbResource;

    private String instId;

    private String imgUrl;

    //操作id
    private String operateId ;

    private String logPath;

    private String port;

    private T obj;

    private EventListener<T,O> eventListener;

    private KubernetesService kubernetesService;

    private Map<Boolean, Pod> podMap;

    private Pod pod;

    private Service service;

    private FlowParameterResponse flowParameterResponse;

    private Deployment deployment;

    @Override
    public String toString() {
        return "InitParameter{" +
                "namespaceName='" + namespaceName + '\'' +
                ", deploymentName='" + deploymentName + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", protocol='" + protocol + '\'' +
                ", clusterName='" + clusterName + '\'' +
                ", containerName='" + containerName + '\'' +
                ", envVars=" + envVars +
                ", containerPorts=" + containerPorts +
                ", kbVolumeInfos=" + kbVolumeInfos +
                ", kbResource=" + kbResource +
                ", instId='" + instId + '\'' +
                ", imgUrl='" + imgUrl + '\'' +
                ", operateId='" + operateId + '\'' +
                ", logPath='" + logPath + '\'' +
                ", port='" + port + '\'' +
                '}';
    }
}
