package org.application.es.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * @Auther: csz
 * @Date: 2018/8/22 10:11
 * @Description: 记录实例与日志id关系
 */
@ToString
@Data
public class InstLogDomain {

    private long id;

    @ApiModelProperty(value = "实例编号")
    private String instId;

    @ApiModelProperty(value = "日志编号")
    private String logId;

    @ApiModelProperty(value = "动作名称 check compile package deploy等")
    private String workName;

    @ApiModelProperty(value = "日志类型  1:build  2:deploy")
    private Integer logType;
}
