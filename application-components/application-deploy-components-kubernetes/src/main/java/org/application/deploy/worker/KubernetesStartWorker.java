package org.application.deploy.worker;

import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.start.StartWorker;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;

/**
 * 启动d
 */
@Slf4j
public class KubernetesStartWorker  implements StartWorker {

    /**
     * rancher 启动 直接调用 发布即可
     * @param listener
     * @return
     * @throws
     */
    @Override
    public FlowParameterResponse start(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {

        listener.onEvent(flowParameterRequest, "Initializing the parameters required for instance start，Please wait.....");
        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initStartParameter( flowParameterRequest, listener);

        CommonFunction commonFunction = CommonFunction.newInstance();
        listener.onEvent(flowParameterRequest, initParameter.toString());
        listener.onEvent(flowParameterRequest, "Parameter initialization complete ! ");

        listener.onEvent(flowParameterRequest, "Checking the cluster health status.........");
        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(flowParameterRequest, "The cluster is healthy !");

        listener.onEvent(flowParameterRequest, "Starting instance，Please wait.....");
        commonFunction.start(initParameter);

        commonFunction.getPodInfoByDeploymentName(initParameter);

        listener.onEvent(flowParameterRequest, "Start checking the instance status......");
        commonFunction.getpodOrDisPodMapByPod(initParameter);

        commonFunction.printPodEventAndLogInfo(initParameter);

        listener.onEvent(flowParameterRequest, "Instance status okay !");

        commonFunction.getServiceInfoByName(initParameter);

        listener.onEvent(flowParameterRequest, "Starting instance Success !");

        commonFunction.saveResponseParameter(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();
        listener.onEvent(flowParameterRequest, "Start saving process data : " +  deployResponse.toString());
        listener.onFinish(flowParameterRequest, deployResponse);

        return deployResponse;
    }
}
