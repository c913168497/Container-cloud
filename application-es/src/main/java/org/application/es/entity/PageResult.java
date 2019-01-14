package org.application.es.entity;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class PageResult<T> {
    //每页条数
    private int pageSize = 20;
    //第几页
    private int pageNum = 1;
    //总数量
    private long totalNum;
    //总页数
    private long totalPage;

    private List<T> data;
}
