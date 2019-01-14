package org.application.es.entity;


import com.cloud.appcloud.utils.validateannotation.NotBlank;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class BusinessLogSearchEntity {

    @ApiModelProperty(value="日志ID")
    @NotBlank(message = "日志ID不能为空")
    private String logId;
    @ApiModelProperty(value="日志信息")
    private String logInfo;
    @ApiModelProperty(value="创建时间开始")
    private String createTimeStart;
    @ApiModelProperty(value="创建时间止")
    private String createTimeEnd;
    //每页条数
    private int pageSize = 20;
    //第几页
    @Min(value = 1, message = "最小分页参数为1")
    private int pageNum = 1;
}
