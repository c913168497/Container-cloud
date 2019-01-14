package org.application.deploy.worker;


import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.update.UpdateWorker;
import org.application.deploy.config.KubernetesProperties;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;

@AllArgsConstructor
@Slf4j
public class KubernetesUpdateWorker implements UpdateWorker {

    private KubernetesProperties kubernetesProperties ;
    @Override
    public FlowParameterResponse update(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {

        log.info("update deploy params : {}", flowParameterRequest.toString());
        listener.onEvent(flowParameterRequest, "Initializing the parameters required for instance update，Please wait.....");

        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initUpdatePatameter( flowParameterRequest, listener, kubernetesProperties);

        CommonFunction commonFunction = CommonFunction.newInstance();
        listener.onEvent(flowParameterRequest, initParameter.toString());
        listener.onEvent(flowParameterRequest, "Parameter initialization complete ! ");

        listener.onEvent(flowParameterRequest, "Checking the cluster health status.........");
        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(flowParameterRequest, "The cluster is healthy !");

        listener.onEvent(flowParameterRequest, "Updating instance ，Please wait.....");
        commonFunction.update(initParameter);

        commonFunction.getPodInfoByDeploymentName(initParameter);

        listener.onEvent(flowParameterRequest, "Start checking the instance status......");
        commonFunction.getpodOrDisPodMapByPod(initParameter);

        commonFunction.printPodEventAndLogInfo(initParameter);

        listener.onEvent(flowParameterRequest, "Instance status okay !");

        commonFunction.getServiceInfoByName(initParameter);

        listener.onEvent(flowParameterRequest, "Update instance Success !");

        commonFunction.saveResponseParameter(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();
        listener.onEvent(flowParameterRequest, "Start saving process data : " + deployResponse.toString());
        listener.onFinish(flowParameterRequest, deployResponse);
        return deployResponse;
    }
}
