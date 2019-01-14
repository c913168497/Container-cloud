package org.application.common.constants;

public enum WorkerEnum {

    CLONE("clone"),
    CHECK("check"),
    COMPILE("compile") ,
    PACKAGE("package") ,
    VOID("void") ,
    SYNC("sync"),
    DEPLOY("deploy"),
    STOP("stop"),
    START("start"),
    RESTART("restart"),
    DESTROY("destroy"),
    UPGRADE("upgrade"),
    ROLLBACK("rollback"),
    ;
    public String name  ;

    private WorkerEnum(String name) {
        this.name = name ;
    }

}
