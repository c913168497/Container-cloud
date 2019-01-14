package org.application.deploy.entity.base;

import lombok.Data;

@Data
public class KBClusterResource {
    // 1000 表示 1个 cpu
    private Integer cpu;
    // 内存大小
    private Long memory;

}
