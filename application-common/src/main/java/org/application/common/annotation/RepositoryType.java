package org.application.common.annotation;


import org.application.common.constants.SourceTypeEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface RepositoryType {

    public SourceTypeEnum value() ;

    public int priority() default 0 ;

}
