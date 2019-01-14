package org.application.common.exception;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class LicenseException extends  RuntimeException{

    private Integer code ;

    private String message ;

}
