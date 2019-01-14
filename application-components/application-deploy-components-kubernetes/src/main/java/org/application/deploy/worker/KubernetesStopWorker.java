package org.application.deploy.worker;

import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.stop.StopWorker;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;

@Slf4j
public class KubernetesStopWorker implements StopWorker {

    /**
     * rancher 停止服务 直接调用容器服务移除
     * @param instance
     * @param listener
     * @return
     * @throws WorkException
     */
    @Override
    public FlowParameterResponse stop(FlowParameterRequest instance, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {

        listener.onEvent(instance, "Initializing the parameters required for instance stop，Please wait.....");
        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initStopParameter( instance, listener);

        CommonFunction commonFunction = CommonFunction.newInstance();
        listener.onEvent(instance, initParameter.toString());
        listener.onEvent(instance, "Parameter initialization complete ! ");

        listener.onEvent(instance, "Checking the cluster health status.........");
        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(instance, "The cluster is healthy !");

        listener.onEvent(instance, "Stopping instance，Please wait.....");
        commonFunction.stop(initParameter);

        listener.onEvent(instance, "Stopping instance Success !");

        commonFunction.saveResponseParameterNoHostInfo(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();
        listener.onEvent(instance, "Stop saving process data : " + deployResponse.toString());
        listener.onFinish(instance, deployResponse);

        return deployResponse;
    }

}
