package org.application.es.entity;

import lombok.Data;

import javax.validation.constraints.Min;

@Data
public class OperationSearchEntity {

    private String keyWord;//关键词

    private String result;//状态

    private String createTimeStart;

    private String createTimeEnd;
    //每页条数
    private int pageSize = 20;
    //第几页
    @Min(value = 1, message = "最小分页参数为1")
    private int pageNum = 1;

}
