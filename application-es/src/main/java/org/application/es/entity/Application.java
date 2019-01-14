package org.application.es.entity;

import lombok.Data;

import java.util.List;

/**
 * @Auther: csz
 * @Date: 2018/8/27 17:37
 * @Description:
 */
@Data
public class Application {
    private List<Deploy> deployList; //容器发布的应用集合
}
