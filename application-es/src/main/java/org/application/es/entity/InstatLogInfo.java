package org.application.es.entity;

import com.cloud.appcloud.utils.validateannotation.NotBlank;
import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class InstatLogInfo {

    @NotBlank(message = "查询ID 不能为空")
    private String id;

    private String logInfo;

    //每页条数
    private int pageSize = 20;
    //第几页
    @Min(value = 1, message = "最小分页参数为1")
    private int pageNum = 1;


}
