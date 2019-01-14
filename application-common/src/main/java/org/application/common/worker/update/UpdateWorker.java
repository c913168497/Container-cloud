package org.application.common.worker.update;


import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

public interface UpdateWorker{


    /**
     * 更新
     * @param flowParameterRequest 更新请求
     * @result 发布返回结果
     */
   public FlowParameterResponse update(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException;


}
