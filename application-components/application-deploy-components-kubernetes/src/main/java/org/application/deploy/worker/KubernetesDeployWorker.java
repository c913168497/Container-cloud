package org.application.deploy.worker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.deploy.DeployWorker;
import org.application.deploy.config.KubernetesProperties;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;

import java.util.Optional;


@AllArgsConstructor
@Slf4j
public class KubernetesDeployWorker implements DeployWorker {

    private KubernetesProperties kubernetesProperties;
    /**
     * 发布 容器应用
     *
     * @param
     * @param listener
     */
    public FlowParameterResponse deploy(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {
        log.info("dockter deploy params : {}", flowParameterRequest.toString());

        listener.onEvent(flowParameterRequest, "Initializing the parameters required for instance deployment，Please wait.....");

        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initDeployParameter( flowParameterRequest, listener, kubernetesProperties );

        listener.onEvent(flowParameterRequest, initParameter.toString());
        listener.onEvent(flowParameterRequest, "Parameter initialization complete !");

        CommonFunction commonFunction = CommonFunction.newInstance();

        listener.onEvent(flowParameterRequest, "Checking the cluster health status.........");
        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(flowParameterRequest, "The cluster is healthy !");

        listener.onEvent(flowParameterRequest, "Checking the resources needed to publish the instance......");
        commonFunction.checkK8sClusterResource(initParameter);
        listener.onEvent(flowParameterRequest, "resources: " + Optional.ofNullable(initParameter.getKbResource()).map(v -> v.toString()).orElse("not set "));

        listener.onEvent(flowParameterRequest, "An instance is being created，Please wait.....");
        commonFunction.deploy(initParameter);

        commonFunction.getPodInfoByDeploymentName(initParameter);
        listener.onEvent(flowParameterRequest, "Instance creation has ended !");

        listener.onEvent(flowParameterRequest, "Start checking the instance status......");
        commonFunction.getpodOrDisPodMapByPod(initParameter);

        commonFunction.printPodEventAndLogInfo(initParameter);

        listener.onEvent(flowParameterRequest, "Instance status okay !");

        listener.onEvent(flowParameterRequest, "Start creating Service ， Please wait......");
        commonFunction.createService(initParameter);

        commonFunction.saveResponseParameter(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();

        listener.onEvent(flowParameterRequest, "Deployed service Success !");
        listener.onEvent(flowParameterRequest, "Start saving process data : " + deployResponse.toString());
        listener.onFinish(flowParameterRequest, deployResponse);
        return deployResponse;
    }

}
