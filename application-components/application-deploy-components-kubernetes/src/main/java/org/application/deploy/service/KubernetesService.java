package org.application.deploy.service;

import io.fabric8.kubernetes.api.model.*;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.api.model.apps.DeploymentSpec;
import io.fabric8.kubernetes.api.model.apps.DeploymentStrategy;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.application.deploy.K8enum.KBSelector;
import org.application.deploy.K8enum.QuantityType;
import org.application.deploy.entity.DeploymentTemplate;
import org.application.deploy.entity.ServiceInfo;
import org.application.deploy.entity.base.KBClusterResource;
import org.application.deploy.entity.base.KBResource;
import org.application.deploy.utils.StausCheckUtil;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class KubernetesService {

    private KubernetesClient client;

    // 传入 kubernetes 集群地址
    public KubernetesService( String masterUrl) {
        Config config = new ConfigBuilder().withMasterUrl(masterUrl).build();
        this.client = new DefaultKubernetesClient(config);
    }

    public Map<String, String> getClientVersion() throws Exception {
        try {
            VersionInfo version =  client.getVersion();
            int i = 0;
            while ( i < 10 && !Optional.ofNullable(version).isPresent()){
                version = client.getVersion();
                Thread.sleep(3000);
            }
            return version.getData();
        }catch (Exception e){
            log.error(e.getMessage());
            e.printStackTrace();
            throw new Exception("请检查集群地址是否正确");
        }
    }

    //################## Namespace ##################//
    /**
     * 通过命名空间名称获取命名空间信息
     * @param namespace
     * @return
     */
    public Namespace getNamespaceByName(String namespace){
        Namespace myns = null;
        try {
            myns = client.namespaces().withName( namespace ).get();
        }catch (Exception e){
            log.info("命名空间不存在" + e.getMessage());
        }
        return myns;
    }
    /**
     * 创建命名空间
     * @return
     */
    public Namespace createNamespace(String namespaceName){
        Namespace namespace = new Namespace();
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName(namespaceName);
        namespace.setMetadata(objectMeta);
        Namespace myns = client.namespaces().create(namespace);
        return myns;
    }
    //################################################################################################//



    //################### Node ##############################//
    /**
     * 获取节点下 已经占用的 pod 数量
     */
    public Node getNodePods(String nodeName){
        Node node = null;
        try {
            node = client.nodes().withName(nodeName).get();
        }catch (Exception e){
            log.error("集群下找不到该机器节点信息，请检查" + e.getMessage());
        }
        return node;
    }
    //################################################################################################//



    //####################### Service #########################//
    /**
     * 创建服务( 记得与部署一起调用，形成绑定关系 )
     * servicePort.setNodePort(33662); // 如果是 NodePort 方式 那就换成 ClusterIP
     * @return
     */
    public Service createService(ServiceInfo serviceInfo){
        ServiceSpecBuilder serviceSpecBuilder = new ServiceSpecBuilder();
        List<ServicePort> servicePorts = new ArrayList<>();
        Optional.ofNullable(serviceInfo.getKbPorts()).ifPresent(
                kbPorts -> {
                    kbPorts.forEach(kbPort -> {
                        ServicePort servicePort = new ServicePort();
                        servicePort.setName( kbPort.getName() );
                        servicePort.setProtocol( kbPort.getProtocol() );
                        servicePort.setPort( kbPort.getPort() );
                        IntOrString intOrString  = new IntOrString();
                        intOrString.setIntVal( kbPort.getPort() );
                        servicePort.setTargetPort(intOrString);
                        servicePorts.add(servicePort);
                    });
                }
        );
        Map<String, String> selector = new HashMap<>();
        //选择器 "deployment-cloud-webapptest"( 这个与 容器组进行绑定时使用)
        selector.put(KBSelector.WORK_LOAD.getValue(), serviceInfo.getSelectorName() );
        serviceSpecBuilder.withPorts(servicePorts)
                .withType(serviceInfo.getKbServiceType().name())
                .withSelector(selector).build();
        Service service = new Service();
        service.setSpec(serviceSpecBuilder.build());
        ObjectMeta objectMeta = new ObjectMeta();
        objectMeta.setName( serviceInfo.getName());
        objectMeta.setNamespace( serviceInfo.getNamespaceName() );
        service.setMetadata(objectMeta);
        return client.services().create(service);
    }

    /**
     * 检查命名空间
     * @param kubernetesService
     * @param namespaceName
     * @return
     * @throws Exception
     */
    public static Namespace checkNamespaceExistAndCreate(KubernetesService kubernetesService, String namespaceName) throws Exception {

        Namespace namespace = kubernetesService.getNamespaceByName(namespaceName);
        // 不存在命令空间则创建
        if (!Optional.ofNullable(namespace).isPresent()){
            kubernetesService.createNamespace(namespaceName);
        }
        namespace = StausCheckUtil.forGetNamespace(kubernetesService, namespaceName);
        if (!Optional.ofNullable(namespace).isPresent()){
            log.error("create namespace fail, please check your machine infomation !");
            throw new Exception("create namespace fail, please check your machine infomation !");
        }
        return namespace;
    }

    /**
     * 检查集群下机器节点是否存在
     * @param nodeName
     * @param kubernetesService
     * @throws Exception
     */
    public static void checkClusterMachineInfo(String nodeName, KubernetesService kubernetesService) throws Exception{
        if ( StringUtils.isNotEmpty( nodeName ) && !Optional.ofNullable(kubernetesService.getNodePods( nodeName )).isPresent() ){
            throw new Exception("集群下找不到该机器节点信息，请检查机器节点:" + nodeName) ;
        }
    }

    /**
     * 通过服务名获取服务信息
     * @param namespace
     * @param servicename
     * @return
     */
    public Service getServiceByName(String namespace, String servicename){
        Service service = null;
        try {
            service = client.services().inNamespace(namespace).withName(servicename).get();
        }catch (Exception e){
            log.info("服务名不存在" + e.getMessage());
        }
        return service;
    }
    public void deleteService (String namespaceName, String deploymentName){
        Service service = client.services().inNamespace(namespaceName).withName(deploymentName).get();
        if (Optional.ofNullable(service).isPresent()){
            client.services().inNamespace(namespaceName).withName(deploymentName).delete();
        }
    }

    //################################################################################################//




    //############################################ Deployment #######################################//
    /**
     * 更新 pod 副本数
     * @param namespaceName
     * @param deploymentName
     * @param replicas
     */
    public Deployment updateDeployment(String namespaceName, String deploymentName, Integer replicas, Deployment deployment) throws Exception {
        // 副本数(pod) 1 个 默认
        try {
            deployment.getSpec().setReplicas(replicas);
            return client.apps().deployments().inNamespace(namespaceName).withName(deploymentName).replace(deployment);
        }catch (Exception e){
            throw new Exception("更新出现异常：" + e.getMessage());
        }
    }
    /**
     * 根据名称获取 deployment 信息
     * @param namespaceName
     * @param deploymentName
     * @return
     */
    public Deployment getDeploymentByName(String namespaceName, String deploymentName){
        Deployment deployment = null;
        try {
            deployment = client.apps().deployments().inNamespace(namespaceName).withName(deploymentName).get();
        }catch (Exception e){
            log.info("deployment 不存在"+ e.getMessage());
        }
        return deployment;
    }

    /**
     * 删除 deployment
     * @param namespaceName
     * @param deploymentName
     */
    public void deleteDeployment(String namespaceName, String deploymentName){
        Deployment deployment = client.apps().deployments().inNamespace(namespaceName).withName(deploymentName).get();
        if (Optional.ofNullable(deployment).isPresent()){
            client.apps().deployments().inNamespace(namespaceName).withName(deploymentName).delete();
        }
    }

    /**
     * 获取 节点机器 可分配资源
     * @param ip
     * @return
     */
    public KBResource getNodeResource(String ip){

        Node  node = client.nodes().withName( ip ).get();

        KBResource kbResource = new KBResource();
        kbResource.setCpu( Integer.parseInt( node.getStatus().getAllocatable().get("cpu").getAmount().replaceAll("[a-zA-Z]","" ) ) );

        kbResource.setMemory( Integer.parseInt( node.getStatus().getAllocatable().get("memory").getAmount().replaceAll("[a-zA-Z]","" ) ) );

        return kbResource;
    }


    /**
     * 获取 集群下 所有节点机器 可分配资源
     * @return
     */
    public List<KBClusterResource> getNodeResourceList(){

        NodeList nodes = client.nodes().list();

        return   Optional.ofNullable(nodes.getItems()).map(nodesList -> {
            List<KBClusterResource> list = new ArrayList<>();
            nodesList.forEach(node -> {
                KBClusterResource kbResource = new KBClusterResource();

                kbResource.setCpu( Integer.parseInt( node.getStatus().getAllocatable().get("cpu").getAmount().replaceAll("[a-zA-Z]","" ) ) );

                kbResource.setMemory( Long.parseLong( node.getStatus().getAllocatable().get("memory").getAmount().replaceAll("[a-zA-Z]","" ) ) );

                list.add(kbResource);
            });
            return list;
        }).orElse(new ArrayList<>());
    }

    /**
     * 发布
     * @param deploymentTemplate
     */
    public void createDeployment(DeploymentTemplate deploymentTemplate){

        List<Container> containers = new ArrayList<>();
        Container container = new Container();

        PodSpec spec = new PodSpec();
        //环境变量
        container.setEnv(deploymentTemplate.getEnvVars());

        //设置镜像 地址
        container.setImage( deploymentTemplate.getImageAddress() );
        container.setName( deploymentTemplate.getName() );
        container.setStdin(true);
        container.setTerminationMessagePath("/dev/termination-log");
        container.setTerminationMessagePolicy("File");
        container.setTty(true);
        SecurityContext securityContext = new SecurityContext();
        securityContext.setPrivileged(true);
        container.setSecurityContext(securityContext);
        // 设置 端口映射及其主机
        container.setPorts(deploymentTemplate.getContainerPort());

        //卷挂载
        //容器内部信息
        List<VolumeMount> volumeMounts = new ArrayList<>();
        //宿主信息
        List<Volume> volumes = new ArrayList<>();

        // 卷容器信息
        Optional.ofNullable(deploymentTemplate.getVolumeInfos()).ifPresent(kbVolumeInfos -> {
            kbVolumeInfos.forEach(kbVolumeInfo -> {
                VolumeMount volumeMount = new VolumeMount();
                String filePath = kbVolumeInfo.getContainerVolumePath();
                //卷挂载 容器内 信息填写
                volumeMount.setMountPath( kbVolumeInfo.getContainerVolumePath() ); //容器内部挂载路径
                String fileName = filePath.substring(filePath.lastIndexOf("/")+1);
                volumeMount.setSubPath(fileName);
                volumeMount.setName( kbVolumeInfo.getName() );// 卷挂载唯一标识

                volumeMount.setReadOnly(  kbVolumeInfo.getReadOnly() ); //只读
                volumeMounts.add(volumeMount);

                //卷挂载 宿主机 信息填写
                Volume volume = new Volume();
                volume.setName( kbVolumeInfo.getName() );

                HostPathVolumeSource hostPathVolumeSource = new HostPathVolumeSource();
                hostPathVolumeSource.setPath( kbVolumeInfo.getHostVolumePath() );
                hostPathVolumeSource.setType( kbVolumeInfo.getKbPathCheckType().getValue() );
                volume.setHostPath(hostPathVolumeSource);

                volumes.add(volume);

            });
        });


        // 标识卷挂载容器内部信息
        container.setVolumeMounts(volumeMounts); //容器内卷信息
        spec.setVolumes(volumes); // 宿主机卷信息
        //资源控制
        Map<String , Quantity> mapLimit = new HashMap<>();

        Optional.ofNullable(deploymentTemplate.getKbResource()).map(kbResource ->  kbResource.getCpu()).ifPresent(
                cpu -> {
                    // 1-设置CPU
                    Quantity quantityCpu = new Quantity();
                    quantityCpu.setAmount(  cpu + "m" );
                    mapLimit.put(QuantityType.cpu.name(), quantityCpu );
                }
        );
        // 2-设置内存
        Optional.ofNullable(deploymentTemplate.getKbResource()).map(kbResource ->  kbResource.getMemory()).ifPresent(
                memory -> {
                    // 1-设置Memory
                    Quantity quantityMem = new Quantity();
                    quantityMem.setAmount(  memory.toString() +  deploymentTemplate.getKbResource().getKbMemUnit() );
                    mapLimit.put(QuantityType.memory.name(), quantityMem );
                }
        );


        ResourceRequirements resources = new ResourceRequirements();
        resources.setLimits(mapLimit);
        container.setResources(resources);
        containers.add(container);

        spec.setContainers(containers);
        spec.setRestartPolicy("Always");

        // 设置发布节点 名称
        if(StringUtils.isNotBlank(deploymentTemplate.getNodeName())){
            spec.setNodeName(deploymentTemplate.getNodeName());
        }

        //标签信息
        Map<String, String> mapStr = new HashMap<>();
        mapStr.put(KBSelector.WORK_LOAD.getValue(), deploymentTemplate.getSelectorName());

        //选择器
        LabelSelector selector = new LabelSelector();
        selector.setMatchLabels(mapStr);

        // pod 模板
        PodTemplateSpec template = new PodTemplateSpec();

        // pod 模板 基础信息 , 这里 metadata 不设置 name 原因是 有 kubernetes 自身产生 容器组内 pod 随机名称，无需自己添加
        ObjectMeta metadata = new ObjectMeta();
        metadata.setLabels(mapStr);
//        metadata.setName(deploymentTemplate.getName());
//        metadata.setGenerateName(deploymentTemplate.getName());

        template.setMetadata(metadata);
        template.setSpec(spec);


        // 整体模板配置 信息
        DeploymentSpec deploymentSpec = new DeploymentSpec();

        // 副本数(pod) 1 个 默认
        deploymentSpec.setReplicas(Optional.ofNullable(deploymentTemplate.getReplicas()).isPresent() ? deploymentTemplate.getReplicas() : 1);
        deploymentSpec.setSelector(selector);
        deploymentSpec.setTemplate(template);
        DeploymentStrategy deploymentStrategy = new DeploymentStrategy();
        deploymentStrategy.setType("Recreate");//杀死所有pod 重新创建
        deploymentSpec.setStrategy(deploymentStrategy);

        Deployment deployment = new Deployment();
        deployment.setSpec(deploymentSpec);

        ObjectMeta objectMeta = new ObjectMeta();
        // 设置 部署名
        objectMeta.setName(   deploymentTemplate.getName() );
        objectMeta.setNamespace( deploymentTemplate.getNamespace() );

        // 设置 部署 标签
        objectMeta.setLabels(mapStr);
        // 载入信息
        deployment.setMetadata(objectMeta);
        // 开始创建
        client.apps().deployments().create(deployment);
    }

    /**
     * 升级
     *
     */
    public Deployment updateDeploymentInfo(Deployment deployment, String imageAddress){
        // 设置镜像
        deployment.getSpec().getTemplate().getSpec().getContainers().get(0).setImage(imageAddress);
        deployment.getSpec().setReplicas(1);
        // 开始创建
        log.info("更新参数: "+ deployment.toString());
        return client.apps().deployments().inNamespace(deployment.getMetadata().getNamespace()).withName(deployment.getMetadata().getName()).replace(deployment);
    }

    //################################################################################################//


    //############################################      Log    ####################################//
    /**
     * 获取日志信息 通过 PodName
     * @throws IOException
     */
    public List<String> getLogInfo(String namespace, String podName)  {
        String  logPod =  client.pods().inNamespace(namespace).withName(podName).getLog(true);
        return logPod != null ? Arrays.asList( logPod.split("\r\n") ) : null;
    }

    //################################################################################################//



    //############################################ Pod ##############################################//

    /**
     * 查询 namespace 下 deployment 发布的 pod  的信息
     * @param namespace
     * @param deploymentName
     * @return
     */
    public List<Pod> listPodInfo(String namespace, String deploymentName){
        PodList pods = null;
        List<Pod> podList = new ArrayList<>();
        try {
            pods = client.pods().inNamespace(namespace).list();
            podList.addAll( Optional.ofNullable(pods.getItems()).map(podListData -> {
                return podListData.stream().filter( pod ->   {
                   return Optional.ofNullable( pod.getSpec()).map(  spec -> spec.getContainers()  )
                            .map( containers -> { return containers.size() > 0 ? deploymentName.equals(containers.get(0).getName()) : false; } ).orElse(false);
                } ).collect(Collectors.toList());
            }).orElse(new ArrayList<>()) );
        }catch (Exception e){
            log.error("pod 不存在".concat(e.getMessage()).concat(pods.toString()));
        }
        if (podList.size() > 0 ){
            podList = sorPodList(podList);
        }
        return podList;
    }



    public static List<Pod> sorPodList(List<Pod> podList){
        // 排序取最近
        podList.sort((o1, o2) ->
                (int) (Long.valueOf( o2.getMetadata().getCreationTimestamp().replaceAll("[^\\d]", "") )
                        - Long.valueOf( o1.getMetadata().getCreationTimestamp().replaceAll("[^\\d]", "") )));

        return podList;
    }
    /**
     * 删除 Pod
     * @param namespace
     * @param deploymentName
     */
    public void deletePodListByName(String namespace, String deploymentName){
        List<Pod> podList = listPodInfo(namespace, deploymentName);
        if ( podList.size() > 0 ){
            client.pods().inNamespace(namespace).delete(podList);
        }
        int i = 0;
        while (podList.size() > 0 && i < 20){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            podList = listPodInfo(namespace, deploymentName);
            i ++;
        }
    }

    /**
     * 根据命名空间 和 pod 名称 获取 事件信息
     * @param namespace
     * @param podName
     * @return
     * @throws IOException
     */
    public List<Event> getEventListByPodName(String namespace, String podName){
        EventList eventList = client.events().inNamespace(namespace).list();
        List<Event> events = eventList.getItems();
        if ( events.size() > 0 ){
            return  events.stream().filter(eventListFileter -> {
             return  Optional.ofNullable( eventListFileter.getMetadata() ).map(meta -> meta.getName()).map(name -> name.contains(podName)).orElse(false);
            } ).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

}
