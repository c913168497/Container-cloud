package org.application.common.annotation;



import org.application.common.constants.WorkerEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface WorkGourp {

    /**
     * 当前执行单元名称
     * @return
     */
    public WorkerEnum name() ;

    /**
     * 依赖的执行工作单元
     * @return
     */
    public WorkerEnum depend() ;
}
