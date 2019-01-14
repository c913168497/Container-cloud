package org.application.es.entity;

import lombok.Data;

import java.util.Date;

/**
 * @Auther: csz
 * @Date: 2018/8/29 18:48
 * @Description:
 */
@Data
public class HistoryDomain {
    private Long id;

    private String name; //名称

    private Integer status; //状态  0 成功 1 失败

    private Integer type; //类型  0 构建 1 发布

    private String sourceId; //源编号

    private Date createTime;//创建时间

    private String creator;//执行人
}
