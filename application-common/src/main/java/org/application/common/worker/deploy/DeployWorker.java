package org.application.common.worker.deploy;

import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

public interface DeployWorker {


    /**
     * 发布
     * @param flowParameterRequest 发布请求
     * @result 发布返回结果
     */
    public FlowParameterResponse deploy(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException;


}
