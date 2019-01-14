package org.application.deploy.worker;

import lombok.extern.slf4j.Slf4j;
import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;
import org.application.common.worker.destroy.DestroyWorker;
import org.application.deploy.entity.common.InitParameter;
import org.application.deploy.utils.InitParameterUtil;


@Slf4j
public class KubernetesDestoryWorker implements DestroyWorker {

    /**
     * rancher 停止时 服务已经被移除，因此无须做任何操作，直接执行上层数据删除服务即可
     * @param instance
     * @param listener
     * @return
     * @throws WorkException
     */
    @Override
    public FlowParameterResponse destory(FlowParameterRequest instance, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException {
        listener.onEvent(instance, "Initializing the parameters required for instance stop，Please wait.....");

        InitParameterUtil initParameterFactory = new InitParameterUtil();
        InitParameter initParameter = initParameterFactory.initDestoryParameter( instance, listener );
        CommonFunction commonFunction = CommonFunction.newInstance();

        listener.onEvent(instance, initParameter.toString());
        listener.onEvent(instance, "Parameter initialization complete ! ");

        listener.onEvent(instance, "Checking the cluster health status.........");

        commonFunction.initK8sClientInfo(initParameter);
        listener.onEvent(instance, "The cluster is healthy !");

        listener.onEvent(instance, "Destruction instance，Please wait.....");
        commonFunction.destory(initParameter);
        listener.onEvent(instance, "Destruction instance Success !");

        commonFunction.saveResponseParameterNoHostInfo(initParameter);
        FlowParameterResponse deployResponse = initParameter.getFlowParameterResponse();
        listener.onEvent(instance, "Destruction saving process data : " + ((FlowParameterResponse) deployResponse).toString());
        listener.onFinish(instance, deployResponse);
        return deployResponse;
    }

}
