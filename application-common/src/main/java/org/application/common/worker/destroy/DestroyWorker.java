package org.application.common.worker.destroy;

import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

public interface DestroyWorker {

    public FlowParameterResponse destory(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException;

}

