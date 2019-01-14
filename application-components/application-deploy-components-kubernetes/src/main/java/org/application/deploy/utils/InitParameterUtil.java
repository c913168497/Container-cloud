package org.application.deploy.utils;

import io.fabric8.kubernetes.api.model.ContainerPort;
import io.fabric8.kubernetes.api.model.EnvVar;
import org.apache.commons.lang3.StringUtils;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.entity.InstVar;
import org.application.common.listener.EventListener;
import org.application.deploy.K8enum.KBMemUnit;
import org.application.deploy.K8enum.KBPathCheckType;
import org.application.deploy.config.KubernetesProperties;
import org.application.deploy.constants.KubernetesConstants;
import org.application.deploy.entity.base.KBResource;
import org.application.deploy.entity.base.KBVolumeInfo;
import org.application.deploy.entity.common.InitParameter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


/**
 * @author Sen
 * @effect 公共参数初始化
 */
public  class InitParameterUtil {
    /**
     * 初始化发布参数
     * @param flowParameterRequest
     * @param eventListener
     * @param kubernetesProperties
     * @return
     */
    public  InitParameter<FlowParameterRequest, FlowParameterResponse> initDeployParameter(FlowParameterRequest flowParameterRequest, EventListener eventListener, KubernetesProperties kubernetesProperties){

        InitParameter<FlowParameterRequest, FlowParameterResponse> parameter = new InitParameter();

        parameter.setObj(flowParameterRequest);

        parameter.setEventListener(eventListener);

        parameter.setNamespaceName( Optional.ofNullable(flowParameterRequest.getNamespaceName()).isPresent() ? flowParameterRequest.getNamespaceName() : kubernetesProperties.getNamespaceName() );
        // 发布名称
        parameter.setDeploymentName(  KubernetesConstants.containerHead.concat( flowParameterRequest.getContainerName() ));
        // 发布指定节点
        parameter.setNodeName( flowParameterRequest.getIp() );
        //协议
        parameter.setProtocol( kubernetesProperties.getAgreementType() );
        // 环境变量
        parameter.setEnvVars( iniEnvVars( flowParameterRequest.getEnvs(), flowParameterRequest.getIp() ));
        // 日志路径
        parameter.setLogPath( kubernetesProperties.getLogPath() );
        // 初始化端口
        parameter.setContainerPorts( initPortParameter( flowParameterRequest.getEnvs(), kubernetesProperties.getAgreementType()));
        // 初始化卷挂载
        parameter.setKbVolumeInfos( initVolumeInfo( flowParameterRequest.getEnvs(), kubernetesProperties.getLogPath() , flowParameterRequest.getInstId()));
        // 初始化机器资源
        parameter.setKbResource( initContainerResource( flowParameterRequest.getEnvs() ) );
        // 镜像地址
        parameter.setImgUrl(flowParameterRequest.getImageUrl());
        // 集群名称
        parameter.setClusterName( KubernetesConstants.httpHead .concat( flowParameterRequest.getClusterAddress() ) .concat( KubernetesConstants.colon )  .concat( kubernetesProperties.getPort() ) );
        // k8s 端口
        parameter.setPort( kubernetesProperties.getPort() );

        return parameter;
    }

    /**
     * 初始化销毁参数
     * @param flowParameterRequest
     * @param eventListener
     * @return
     */
    public InitParameter<FlowParameterRequest, FlowParameterResponse> initDestoryParameter(FlowParameterRequest flowParameterRequest, EventListener eventListener){
        InitParameter<FlowParameterRequest, FlowParameterResponse> parameter = new InitParameter();
        parameter.setObj(flowParameterRequest);
        parameter.setEventListener(eventListener);
        parameter.setClusterName( flowParameterRequest.getClusterAddress() );
        parameter.setContainerName( flowParameterRequest.getContainerName() );
        parameter.setDeploymentName( flowParameterRequest.getContainerName() );
        parameter.setNamespaceName( flowParameterRequest.getNamespaceName() );
        return parameter;
    }

    /**
     * 初始化重启参数
     * @param flowParameterRequest
     * @param eventListener
     * @return
     */
    public InitParameter<FlowParameterRequest, FlowParameterResponse> initRestartParameter(FlowParameterRequest flowParameterRequest, EventListener eventListener){
        InitParameter parameter = initDestoryParameter(flowParameterRequest, eventListener);
        parameter.setNodeName( flowParameterRequest.getIp() );
        return parameter;
    }

    /**
     * 初始化启动参数
     * @param flowParameterRequest
     * @param eventListener
     * @return
     */
    public InitParameter<FlowParameterRequest, FlowParameterResponse> initStartParameter(FlowParameterRequest flowParameterRequest, EventListener eventListener){
        InitParameter<FlowParameterRequest, FlowParameterResponse> parameter = new InitParameter();
        parameter.setObj(flowParameterRequest);
        parameter.setEventListener(eventListener);
        parameter.setNodeName( flowParameterRequest.getIp() );
        parameter.setClusterName( flowParameterRequest.getClusterAddress() );
        parameter.setContainerName( flowParameterRequest.getContainerName() );
        parameter.setNamespaceName( flowParameterRequest.getNamespaceName() );
        parameter.setDeploymentName( flowParameterRequest.getContainerName() );
        return parameter;
    }

    /**
     * 初始化停止参数
     * @param flowParameterRequest
     * @param eventListener
     * @return
     */
    public InitParameter<FlowParameterRequest, FlowParameterResponse> initStopParameter(FlowParameterRequest flowParameterRequest, EventListener eventListener){
        return initDestoryParameter(flowParameterRequest, eventListener);
    }

    /**
     * 初始化升级参数
     * @param flowParameterRequest
     * @param eventListener
     * @param kubernetesProperties
     * @return
     */
    public InitParameter<FlowParameterRequest, FlowParameterResponse> initUpdatePatameter(FlowParameterRequest flowParameterRequest, EventListener eventListener, KubernetesProperties kubernetesProperties){
        InitParameter<FlowParameterRequest, FlowParameterResponse> parameter = new InitParameter();
        parameter.setObj(flowParameterRequest);
        parameter.setEventListener(eventListener);
        parameter.setNamespaceName( Optional.ofNullable(flowParameterRequest.getNamespaceName()).isPresent() ? flowParameterRequest.getNamespaceName() : kubernetesProperties.getNamespaceName() );
        parameter.setDeploymentName( flowParameterRequest.getContainerName() );
        parameter.setNodeName( flowParameterRequest.getIp() );
        parameter.setProtocol( kubernetesProperties.getAgreementType() );
        parameter.setClusterName( flowParameterRequest.getClusterAddress() );
        parameter.setImgUrl(flowParameterRequest.getImageUrl());
        return parameter;
    }

    /**
     * 初始化发布时 环境变量
     * @param InstVars
     * @param nodeName
     * @return
     */
    private List<EnvVar> iniEnvVars(List<InstVar> InstVars , String nodeName){
        InstVars = InstVarGetUtil.getVarsType1(InstVars);
        List<EnvVar> envVars = InstVars.stream().map(InstVar -> new EnvVar(InstVar.getVarsName(), InstVar.getVarsValue(), null)).collect(Collectors.toList());
        if ( StringUtils.isNotEmpty( nodeName ) ){
            envVars.add(new EnvVar("TARGET_IP", nodeName, null));          //（MYSQL 主从时用到）
        }
        return envVars;
    }

    /**
     * 初始化端口参数
     * @param InstVars
     * @param agreementType
     * @return
     */
    public static List<ContainerPort>  initPortParameter(List<InstVar> InstVars, String agreementType){

        InstVars = InstVarGetUtil.getVarsType2(InstVars);
        List<ContainerPort> deployPorts = new ArrayList<>();
        for (InstVar InstVar : InstVars) {

            Optional.ofNullable( ParameterUtils.parameterStringToInteger(InstVar.getVarsName()) ).ifPresent(v -> {

                ContainerPort containerPort = new ContainerPort();
                Integer hp = ParameterUtils.parameterStringToInteger(InstVar.getVarsValue());
                containerPort.setContainerPort( v );
                containerPort.setProtocol( agreementType );
                containerPort.setHostPort( hp );

                String cpName =  v + agreementType.toLowerCase();
                if ( Optional.ofNullable(hp).isPresent() ){
                    cpName += hp;
                }

                containerPort.setName( cpName );
                deployPorts.add(containerPort);
            });
        }
        return deployPorts;
    }

    /**
     * 初始化挂载卷属性
     * @param InstVars
     * @param logPath
     * @param instId
     * @return
     */
    public static List<KBVolumeInfo> initVolumeInfo(List<InstVar> InstVars, String logPath, String instId){
        List<KBVolumeInfo> kbVolumeInfos = new ArrayList<>();

        List<InstVar> volumes = InstVarGetUtil.getVarType4( InstVars );
        List<InstVar> logVars = InstVarGetUtil.getVarType5( InstVars );

        for (InstVar volume : volumes) {
            KBVolumeInfo kbVolumeInfo = new KBVolumeInfo();
            kbVolumeInfo.setName( ParameterUtils.getVolumeName() );
            kbVolumeInfo.setHostVolumePath( volume.getVarsName() );
            kbVolumeInfo.setKbPathCheckType( KBPathCheckType.DIRECTORYORCREATE ); // 默认目录不存在则创建
            kbVolumeInfo.setReadOnly( ParameterUtils.setFileReadOnly( volume.getVarsValue() ) );
            kbVolumeInfo.setContainerVolumePath( volume.getVarsValue() );
            kbVolumeInfos.add(kbVolumeInfo);
        }
        for (InstVar logVar : logVars) {
            KBVolumeInfo kbVolumeInfo = new KBVolumeInfo();
            kbVolumeInfo.setName( ParameterUtils.getVolumeName() );
            kbVolumeInfo.setHostVolumePath( logPath + instId );
            kbVolumeInfo.setKbPathCheckType( KBPathCheckType.DIRECTORYORCREATE );
            kbVolumeInfo.setReadOnly( false );
            kbVolumeInfo.setContainerVolumePath( logVar.getVarsValue() );
            kbVolumeInfos.add(kbVolumeInfo);
        }
        return kbVolumeInfos;
    }

    /**
     * 容器资源控制 参数 过滤
     * @param InstVars
     * @return
     */
    public static KBResource initContainerResource(List<InstVar> InstVars) {
        List<InstVar> resourceLimit = InstVarGetUtil.getVarType6( InstVars );
        KBResource kbResource = null;
        for (InstVar InstVar : resourceLimit) {
            kbResource = new KBResource();
            String [] machineResource = InstVar.getVarsValue().split("/");
            if (machineResource.length > 1){
                Integer cpu = Integer.parseInt(machineResource[0]);
                Integer memory = Integer.parseInt( machineResource[1] );
                kbResource.setCpu( cpu );
                kbResource.setMemory( memory );
                kbResource.setKbMemUnit(KBMemUnit.Mi);
            }
        }
        return kbResource;
    }

}
