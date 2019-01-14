package org.application.common.exception;

import lombok.Data;

@Data
public class WorkException extends Exception {

    private String error ;

    public WorkException(String error) {
        super(error);
        this.error = error;
    }
}
