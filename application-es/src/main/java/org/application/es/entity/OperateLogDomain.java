package org.application.es.entity;

import lombok.Data;
import lombok.ToString;

/**
 * @Auther: Administrator
 * @Date: 2018/9/5 0005 11:49
 * @Description: 操作日志
 */
@Data
@ToString
public class OperateLogDomain {

    private String id;

    private String operateName;//操作名称

    private Integer result;//操作结果 0：成功  1:失败

    private String creator;//操作人

    private String createTime;//创建时间(日志时间)

    private String createTimeFormart;//创建时间（格式化时间）

    private String detail;//明细
}
