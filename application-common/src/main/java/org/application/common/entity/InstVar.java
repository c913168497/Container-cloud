package org.application.common.entity;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class InstVar {
    private String instId; //实例编号
    private Integer varsType; //环境变量类型    1：环境变量 2：端口映射 3: 配置文件 4: 卷挂载 5: 日志挂载 6.资源属性限制（cpu,memory）
    private String varsName; //变量名称
    private String varsValue; //变量值
}
