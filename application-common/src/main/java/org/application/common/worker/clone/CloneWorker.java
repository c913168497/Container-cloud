package org.application.common.worker.clone;


import org.application.common.entity.FlowParameterResponse;
import org.application.common.entity.Repository;
import org.application.common.entity.Resource;
import org.application.common.exception.WorkException;
import org.application.common.listener.EventListener;

/**
 * 代码克隆接口
 */
public interface CloneWorker {

    /**
     * 克隆动作，将目标资源克隆到目标位置
     * @param repository
     * @param listener
     */
    public FlowParameterResponse clone(Repository repository, EventListener<Repository, Resource> listener) throws WorkException;

}
