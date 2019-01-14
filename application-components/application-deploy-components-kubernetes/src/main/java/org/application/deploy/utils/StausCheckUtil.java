package org.application.deploy.utils;

import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Namespace;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.apache.commons.collections.CollectionUtils;
import org.application.common.exception.WorkException;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.service.KubernetesService;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sen
 * @effect 参数转换 及 参数校验
 */
public class StausCheckUtil {

    public static Deployment getDeploymentTemplate(InitParameter initParameter) throws WorkException {
        Deployment deployment = initParameter.getKubernetesService().getDeploymentByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
        int i = 0;
        while ( deployment != null && i < 10 ){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deployment = initParameter.getKubernetesService().getDeploymentByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
            i++;
        }
        if (deployment == null){
            throw new WorkException("创建 Deployment 出现未知异常，请检查参数及机器环境是否正常");
        }
        return deployment;
    }
    /**
     * 检查传入集群信息是否有误
     * @param kubernetesService
     * @param clusterName
     * @throws WorkException
     */
    public static Map<String, String> checkClusterIpValid(KubernetesService kubernetesService, String clusterName) throws Exception {
        try {
            Map<String, String> map =  kubernetesService.getClientVersion();
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("连接集群 " + clusterName + " 失败，请检查集群主机服务是否正常启动，且主机服务地址能正常使用");
        }
    }


    /**
     * 判断 deployment 是否已经删除
     * @param kubernetesService
     * @param namespaceName
     * @param containerName
     * @return
     */
    public static Deployment forGetDeployment(KubernetesService kubernetesService, String namespaceName, String containerName){
        Deployment deployment = kubernetesService.getDeploymentByName(namespaceName, containerName);

        int i = 0;
        while ( Optional.ofNullable(deployment).isPresent() && i < 10 ){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            deployment = kubernetesService.getDeploymentByName(namespaceName, containerName);
            i++;
        }
        return deployment;
    }

    /**
     * 轮询命名空间信息
     * @param kubernetesService
     * @param namespaceName
     * @return
     */
    public static Namespace forGetNamespace(KubernetesService kubernetesService, String namespaceName){
        Namespace namespace = kubernetesService.getNamespaceByName(namespaceName);
        int i = 0;
        while (!Optional.ofNullable(namespace).isPresent() && i < 10){
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            namespace = kubernetesService.getNamespaceByName(namespaceName);
            i++;
        }

        return namespace;
    }

    public static ContainerStatus getContainerStatus(Pod pod){
        List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses() ;
        return CollectionUtils.isNotEmpty( containerStatuses ) ? containerStatuses.get( containerStatuses.size()-1 ) : null;
    }
}
