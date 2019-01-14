package org.application.es.entity;

import lombok.Data;

/**
 * @Auther: csz
 * @Date: 2018/8/27 17:37
 * @Description: 10.21.237.42  docker
 */
@Data
public class Host {
    private String cpu; //cpu使用率
    private String memoryTotal;//内存总量
    private String memoryUsed;//内存使用量
    private String memoryRate;//内存剩余量
    private String diskTotal;//磁盘总量
    private String diskUsed;//磁盘使用量
    private String diskRate;//磁盘剩余量
}
