package org.application.common.constants;

/**
 * 主机IP类型
 */
public enum HostTypeEnum {

    VM(0), // 宿主机
    CONTAINER(1); //容器IP
    public Integer type  ;

    private HostTypeEnum(Integer type) {
        this.type = type ;
    }

}
