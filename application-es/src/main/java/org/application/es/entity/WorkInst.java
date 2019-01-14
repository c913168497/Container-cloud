package org.application.es.entity;


import lombok.Data;

@Data
public class WorkInst {

	private long id;
    private String instId;
    private String workName;
    private String workResult;//执行结果
    private String workParameters;//执行参数
    private Integer workType;
    private Integer status;
    private String message;//执行备注信息
    private String sourceId;
    private Integer version;//版本 默认1. 每个模板重新执行的时候需要+1
    private Integer isHistory = 0; //是否为历史记录 0：否 1：是
    private String creator;//执行人
	
}
