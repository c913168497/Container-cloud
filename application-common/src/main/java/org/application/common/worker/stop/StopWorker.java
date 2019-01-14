package org.application.common.worker.stop;


import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

public interface StopWorker {

    public FlowParameterResponse stop(FlowParameterRequest flowParameterRequest, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException;
}

