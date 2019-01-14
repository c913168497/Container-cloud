package org.application.deploy.worker;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.Event;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.application.common.constants.HostTypeEnum;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.entity.Host;
import org.application.common.exception.WorkException;
import org.application.common.listener.ShellEventListener;
import org.application.common.utils.FileUtils;
import org.application.common.utils.ShellUtils;
import org.application.deploy.K8enum.KBServiceType;
import org.application.deploy.constants.KubernetesConstants;
import org.application.deploy.entity.DeploymentTemplate;
import org.application.deploy.entity.ServiceInfo;
import org.application.deploy.entity.base.KBClusterResource;
import org.application.deploy.entity.base.KBPort;
import org.application.deploy.entity.base.KBResource;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.service.KubernetesService;
import org.application.deploy.utils.StausCheckUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class CommonFunction {

    private static CommonFunction commonFunction = new CommonFunction();
    private CommonFunction(){}
    public static CommonFunction newInstance(){
        return commonFunction;
    }

    // 初始化集群信息
    public void initK8sClientInfo(InitParameter initParameter) throws WorkException {
        if (initParameter == null){
            initParameter.getEventListener().onError(initParameter.getObj(), "集群信息校验参数不能为空!");
            throw new WorkException("集群校验参数不能为空!");
        }
        // 集群信息
        KubernetesService kubernetesService = new KubernetesService( initParameter.getClusterName() );
        // 检查集群信息、命名空间 是否有误
        try {
            Map<String, String> map =  StausCheckUtil.checkClusterIpValid(kubernetesService, initParameter.getClusterName());
            KubernetesService.checkNamespaceExistAndCreate(kubernetesService, initParameter.getNamespaceName());
            KubernetesService.checkClusterMachineInfo(initParameter.getNodeName(), kubernetesService);
            initParameter.getEventListener().onEvent(initParameter.getObj(), "ClusterInfo:   gitVersion  " +
                    "-" + map.get("gitVersion") + "     goVersion-" + map.get("goVersion") + "    platform-" + map.get("platform"));
        } catch (Exception e) {
            initParameter.getEventListener().onError(initParameter.getObj(), e.getMessage());
            throw new WorkException(e.getMessage());
        }
        initParameter.setKubernetesService(kubernetesService);
    }
    // 检查集群健康状态
    public void checkK8sClusterResource(InitParameter initParameter) throws WorkException {
        // 检查 资源是否充足
        KBResource kbResource = initParameter.getKbResource();
        if (kbResource == null){
            return;
        }
        Integer cpuRequest = kbResource.getCpu().intValue() / 1000; // 1000 -> 1
        Integer memoryRequest = kbResource.getMemory().intValue() *  1024; // MB -> KB
        String nodeName = initParameter.getNodeName();
        // 如果指定了机器则只检查当前机器，否则检查整个集群
        if (  StringUtils.isNotEmpty( nodeName ) ){
            KBResource kbResourceMax = initParameter.getKubernetesService().getNodeResource( nodeName );
            if (cpuRequest > kbResourceMax.getCpu() || memoryRequest > kbResourceMax.getMemory() ){
                String message = "当前机器节点资源不足： ip: " + nodeName + "-- resource: " + " cpu : " + kbResourceMax.getCpu()  + "m   memory : " + kbResourceMax.getMemory() + "Ki";
                initParameter.getEventListener().onError(initParameter.getObj(), message);
                throw new WorkException(message);
            }
        }else {
            List<KBClusterResource> kbClusterResources =  initParameter.getKubernetesService().getNodeResourceList();
            int nodeSize = kbClusterResources.stream().filter(kbClusterResource -> {
                return  cpuRequest > kbClusterResource.getCpu() || Long.parseLong(memoryRequest.toString()) > kbClusterResource.getMemory();
            }).collect(Collectors.toList()).size();
            if (nodeSize == kbClusterResources.size()){
                String message = "当前集群下，没有足够的机器资源发布该应用！";
                initParameter.getEventListener().onError(initParameter.getObj(), message);
                throw new WorkException(message);
            }
        }
    }
    public void printPodEventAndLogInfo(InitParameter initParameter) throws WorkException {
        Map<Boolean, Pod> podMap = initParameter.getPodMap();
        List<Event> events = initParameter.getKubernetesService()
                .getEventListByPodName(
                        initParameter.getNamespaceName(),
                        Optional.ofNullable(podMap.get(Boolean.TRUE)).orElse(podMap.get(Boolean.FALSE)).getMetadata().getName()
                );
        for (Event event : events) {
            initParameter.getEventListener().onEvent(initParameter.getObj(), event.getMessage());
        }
        Pod pod = podMap.get(Boolean.TRUE);
        if (pod == null){
            initParameter.getEventListener().onError(initParameter.getObj(), "无法获取 Pod 信息，请检查日志，并检查机器环境是否正常！");
            throw new WorkException("无法获取 Pod 信息，请检查日志，并检查机器环境是否正常！");
        }

        List<String> logs = initParameter.getKubernetesService().getLogInfo(initParameter.getNamespaceName(), pod.getMetadata().getName());
        if (Optional.ofNullable(logs).isPresent()){
            for (String log : logs) {
                log = log.replaceAll("\\u001B", " "); //替换原日志中无用空白字符
                initParameter.getEventListener().onEvent(initParameter.getObj(), log);
            }
        }
        initParameter.setPod(pod);
    }
    public void getPodInfoByDeploymentName(InitParameter initParameter ) throws WorkException {
        List<Pod> podList = initParameter.getKubernetesService().listPodInfo(initParameter.getNamespaceName(), initParameter.getDeploymentName());
        int i = 0;
        while (podList.size() == 0 && i < 10){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            podList = initParameter.getKubernetesService().listPodInfo(initParameter.getNamespaceName(), initParameter.getDeploymentName());
            i ++;
        }

        if (CollectionUtils.isEmpty( podList )){
            initParameter.getEventListener().onError(initParameter.getObj(), "无法获取 Pod 信息，请检查参数及机器环境是否正常");
            throw new WorkException("无法获取 Pod 信息，请检查参数及机器环境是否正常");
        }
        initParameter.setPod(podList.get(0));
    }
    public void getpodOrDisPodMapByPod(InitParameter initParameter){
        Map<Boolean, Pod> podMap = new HashMap<>();
        List<Pod> podList = null;
        boolean ready = Optional.ofNullable(StausCheckUtil.getContainerStatus(  initParameter.getPod() )).map(status -> status.getReady()).orElse(false);
        if (ready){
            initParameter.getKubernetesService().getEventListByPodName(initParameter.getNamespaceName(), initParameter.getPod().getMetadata().getName());
            podMap.put(ready, initParameter.getPod());
            initParameter.setPodMap(podMap);
            return;
        }
        int i = 0;
        while ( !ready && i < 10 ){
            try {
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            podList = initParameter.getKubernetesService().listPodInfo(initParameter.getNamespaceName(), initParameter.getDeploymentName());
            ready = Optional.ofNullable(StausCheckUtil.getContainerStatus(  podList.get(0) )).map(status -> status.getReady()).orElse(false);
            i++;
        }
        Map<Boolean, Pod> map = new HashMap<>();
        map.put(ready, podList.get(0));
        initParameter.setPodMap(map);
    }
    public void saveResponseParameterNoHostInfo(InitParameter initParameter){
        initParameter.setFlowParameterResponse( commonSaveResponseParameter(initParameter) );
    }
    public void saveResponseParameter(InitParameter initParameter){
        FlowParameterResponse response = commonSaveResponseParameter(initParameter);
        String nodeIp = initParameter.getPod().getSpec().getNodeName(); // 外部
        String clusterIp = Optional.ofNullable(initParameter.getService()).isPresent() ?  initParameter.getService().getSpec().getClusterIP() : nodeIp; //内部
        List<Host> hosts = new ArrayList<>();
        //外部端口
        Host host1 = new Host();
        host1.setIp(nodeIp);
        host1.setType(HostTypeEnum.VM.type);
        // 内部端口
        Host host2 = new Host();
        host2.setIp(clusterIp);
        host2.setType(HostTypeEnum.CONTAINER.type);
        Optional.ofNullable(initParameter.getPod().getSpec().getContainers().get(0).getPorts()).ifPresent(
                containerPorts ->  {
                    if (containerPorts .size() > 0){
                        host1.setPort(containerPorts.get(0).getHostPort());//外部
                        host2.setPort(containerPorts.get(0).getContainerPort());//内部
                    }
                }
        );
        hosts.add(host1);
        hosts.add(host2);

        response.setHosts(hosts);
        response.setContainerName(initParameter.getPod().getMetadata().getName());
        String containerID = StausCheckUtil.getContainerStatus(initParameter.getPod()).getContainerID();
        containerID = containerID.replace("docker://","");
        response.setContainerId( containerID );
        initParameter.setFlowParameterResponse(response);
    }
    public void getServiceInfoByName(InitParameter initParameter) throws WorkException {
            Service service =  initParameter.getKubernetesService().getServiceByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
            // 轮询 service 状态
            int i = 0;
            while (service == null && i < 10){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                service =  initParameter.getKubernetesService().getServiceByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
                i ++;
            }
            if (service == null){
                initParameter.getEventListener().onError(initParameter.getObj(),
                        "无法获取 Service 信息， 请检查集群机器服务是否正常！");
                throw new WorkException("无法获取 Service 信息， 请检查集群机器服务是否正常！");
            }
            initParameter.setService(service);
    }
    public void createService(InitParameter initParameter) {
        KubernetesService kubernetesService = initParameter.getKubernetesService();
        List<ContainerPort> containerPorts = initParameter.getContainerPorts();

        if ( containerPorts.size() == 0 ){
            initParameter.getEventListener().onEvent(initParameter.getObj(), "未设置容器端口，Service 创建流程跳过!");
            initParameter.getEventListener().onEvent(initParameter.getObj(), "该实例未设置容器端口，因此外部服务无法访问， 请注意");
            return;
        }
        
        List<KBPort> kbPorts = new ArrayList<>();
        for (ContainerPort containerPort : containerPorts) {
            String kpName = containerPort.getContainerPort() + "cp-hp";
            Integer hp = containerPort.getHostPort();
            KBPort kbPort = new KBPort();
            kbPort.setProtocol( containerPort.getProtocol() );
            kbPort.setPort( containerPort.getContainerPort() );
            kbPort.setName( Optional.ofNullable( hp ).isPresent() ?  kpName : (kpName + hp) );
            kbPorts.add(kbPort);
        }
        ServiceInfo serviceInfo = new ServiceInfo();
        serviceInfo.setName(initParameter.getDeploymentName() );
        serviceInfo.setNamespaceName( initParameter.getNamespaceName() );
        serviceInfo.setSelectorName( initParameter.getDeploymentName()  + "-selector");
        serviceInfo.setKbPorts(kbPorts);
        serviceInfo.setKbServiceType(KBServiceType.ClusterIP);
        initParameter.setService(kubernetesService.createService(serviceInfo));
    }
    public void deploy(InitParameter initParameter) throws WorkException {
        KubernetesService kubernetesService = initParameter.getKubernetesService();
        DeploymentTemplate deploymentTemplate = new DeploymentTemplate();
        deploymentTemplate.setName( initParameter.getDeploymentName() );
        deploymentTemplate.setNamespace( initParameter.getNamespaceName() );
        deploymentTemplate.setSelectorName( initParameter.getDeploymentName() + "-selector");
        deploymentTemplate.setImageAddress( initParameter.getImgUrl() );
        deploymentTemplate.setEnvVars(initParameter.getEnvVars());
        deploymentTemplate.setContainerPort(initParameter.getContainerPorts());
        deploymentTemplate.setKbResource( initParameter.getKbResource() );
        deploymentTemplate.setVolumeInfos( initParameter.getKbVolumeInfos() );
        deploymentTemplate.setNodeName( initParameter.getNodeName() );
        kubernetesService.createDeployment(deploymentTemplate);
        initParameter.setDeployment( StausCheckUtil.getDeploymentTemplate(initParameter) );
    }
    public void destory(InitParameter initParameter) throws WorkException {
        KubernetesService kubernetesService = initParameter.getKubernetesService();

        ShellEventListener<?, ?> shellEvenListenerInstall = new ShellEventListener<>(initParameter.getObj(),initParameter.getEventListener());
        String hostIp = "";
        try {
            hostIp = new URL(initParameter.getClusterName()).getHost();
        } catch (MalformedURLException e) {
            initParameter.getEventListener().onError(initParameter.getObj(), "集群 IP 获取异常 ： " + initParameter.getClusterName());
            throw new WorkException("集群 IP 获取异常 ： " + initParameter.getClusterName()) ;
        }

        // 主节点机器端口
        String [] params = {hostIp , String.valueOf(22), initParameter.getNamespaceName(), initParameter.getDeploymentName()};

        //创建文件
        FileUtils.createFile(KubernetesConstants.DESTORY_SRCRIPT,this.getClass().getClassLoader(),"do_delete_deploy.sh");

        String destorySh = "/bin/sh " + KubernetesConstants.DESTORY_SRCRIPT;
        ShellUtils.run(destorySh, params, shellEvenListenerInstall ) ;

        kubernetesService.deleteDeployment(initParameter.getNamespaceName(), initParameter.getDeploymentName());

        Deployment deployment = StausCheckUtil.forGetDeployment(kubernetesService, initParameter.getNamespaceName(), initParameter.getDeploymentName());

        kubernetesService.deleteService(initParameter.getNamespaceName(), initParameter.getDeploymentName());

        if ( Optional.ofNullable(deployment).isPresent() ){
            initParameter.getEventListener().onError(initParameter.getObj(), "无法销毁该实例，请检查该机器资源状态" + deployment.toString());
            throw new WorkException("无法销毁该实例，请检查该机器资源状态") ;
        }
    }
    public void start(InitParameter initParameter) throws WorkException {
        KubernetesService kubernetesService = initParameter.getKubernetesService();
        Deployment deployment = kubernetesService.getDeploymentByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
        if (deployment == null){
            initParameter.getEventListener().onError(initParameter.getObj(), "实例已经被删除，请重新部署！");
            throw new WorkException( "实例已经被删除，请重新部署！" );
        }

        if (deployment.getSpec().getReplicas().intValue() == 1){
            initParameter.setDeployment(deployment);
            return;
        }

        if ( StringUtils.isNotEmpty( initParameter.getNodeName() ) ){
            deployment.getSpec().getTemplate().getSpec().setNodeName( initParameter.getNodeName() );
        }

        try {

            deployment =  kubernetesService.updateDeployment(initParameter.getNamespaceName(), initParameter.getDeploymentName(), 1, deployment);

        } catch (Exception e) {

            initParameter.getEventListener().onError(initParameter.getObj(), e.getMessage());
            throw new WorkException(e.getMessage());

        }
        initParameter.setDeployment(deployment);
    }
    public void stop(InitParameter initParameter) throws WorkException {
        KubernetesService kubernetesService = initParameter.getKubernetesService();
        Deployment deployment = kubernetesService.getDeploymentByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());

        if (deployment.getSpec().getReplicas() == 0){
            initParameter.setDeployment(deployment);
            return;
        }
        try {
            deployment =  kubernetesService.updateDeployment(initParameter.getNamespaceName(), initParameter.getContainerName(), 0, deployment);
        } catch (Exception e) {
            initParameter.getEventListener().onError(initParameter.getObj(), e.getMessage());
            throw new WorkException(e.getMessage()) ;
        }

        // 删除 相关联的 僵尸进程
        kubernetesService.deletePodListByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());

        List<Pod> podList = kubernetesService.listPodInfo(initParameter.getNamespaceName(), initParameter.getDeploymentName());
        int i = 0;
        while (CollectionUtils.isNotEmpty(podList) && i < 10){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            podList = kubernetesService.listPodInfo(initParameter.getNamespaceName(), initParameter.getDeploymentName());
            i ++;
        }

        if (CollectionUtils.isNotEmpty(podList)){
            initParameter.getEventListener().onError(initParameter.getObj(), "停止实例异常，无法停止实例!");
            initParameter.getEventListener().onError(initParameter.getObj(), podList.toString());
            throw new WorkException("停止实例异常，无法停止实例!") ;
        }
        initParameter.setDeployment(deployment);
    }
    public void update(InitParameter initParameter) throws WorkException {
        KubernetesService kubernetesService = initParameter.getKubernetesService();
        Deployment deploymentCheck = kubernetesService.getDeploymentByName(initParameter.getNamespaceName(), initParameter.getDeploymentName());
        if ( deploymentCheck == null ){
            String message = "Unable to find the instance, check that '" + initParameter.getDeploymentName() + "'is destroyed or deleted!";
            initParameter.getEventListener().onError(initParameter.getObj(), message);
            throw new WorkException(message) ;
        }
        initParameter.setDeployment( kubernetesService.updateDeploymentInfo( deploymentCheck, initParameter.getImgUrl()) );
    }

    private FlowParameterResponse commonSaveResponseParameter(InitParameter initParameter){
        FlowParameterResponse response = new FlowParameterResponse();
        response.setContainerName( initParameter.getDeploymentName() );
        response.setClusterAddress( initParameter.getClusterName() );
        response.setNamespaceName( initParameter.getNamespaceName() );
        return response;
    }
}
