package org.application.common.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 主机信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Host {

    private String ip ;

    private Integer port ;

    //0:虚拟机IP  0：容器IP
    private Integer type;

    public Host(String ip ) {
        this.ip = ip ;
    }

}
