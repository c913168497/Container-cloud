package org.application.common.annotation;



import org.application.common.constants.PackageTypeEnum;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface PackageType {

    public PackageTypeEnum value() ;

}

