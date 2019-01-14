package org.application.deploy.worker;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.restart.RestartWorker;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;

@AllArgsConstructor
@Slf4j
public class KubernetesReStartWorker implements RestartWorker {

    @Override
    public FlowParameterResponse restart(FlowParameterRequest instance, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {

        listener.onEvent(instance, "Initializing the parameters required for instance stop，Please wait.....");
        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initRestartParameter( instance, listener);

        CommonFunction commonFunction = CommonFunction.newInstance();
        listener.onEvent(instance, initParameter.toString());
        listener.onEvent(instance, "Parameter initialization complete ! ");

        listener.onEvent(instance, "Checking the cluster health status.........");
        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(instance, "The cluster is healthy !");


        listener.onEvent(instance, "Stopping instance，Please wait.....");
        commonFunction.stop(initParameter);
        listener.onEvent(instance, "Stopping instance success !");

        listener.onEvent(instance, "Starting instance，Please wait.....");
        commonFunction.start(initParameter);

        commonFunction.getPodInfoByDeploymentName(initParameter);

        listener.onEvent(instance, "Start checking the instance status......");
        commonFunction.getpodOrDisPodMapByPod(initParameter);

        commonFunction.printPodEventAndLogInfo(initParameter);

        listener.onEvent(instance, "Instance status okay !");

        commonFunction.getServiceInfoByName(initParameter);

        listener.onEvent(instance, "Starting instance Success !");

        commonFunction.saveResponseParameter(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();
        listener.onEvent(instance, "Start saving process data : " + deployResponse.toString());
        listener.onFinish(instance, deployResponse);
        return deployResponse;
    }
}
