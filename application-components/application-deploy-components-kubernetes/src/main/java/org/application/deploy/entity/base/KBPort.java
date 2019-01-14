package org.application.deploy.entity.base;

import lombok.Data;

@Data
public class KBPort {
    // 名称标识(一般由 内部端口，协议，外部端口-deploy名称组成 3306tcp123050-mysqlv) （必须小写）
    private String name;
    // 协议
    private String protocol;
    // 端口号 (内部)
    private Integer port;
}
