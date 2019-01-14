package org.application.common.worker.start;


import org.application.common.entity.FlowParameterRequest;
import org.application.common.entity.FlowParameterResponse;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

public interface StartWorker {

    public FlowParameterResponse start(FlowParameterRequest instance, EventListener<FlowParameterRequest, FlowParameterResponse> listener) throws WorkException;

}

