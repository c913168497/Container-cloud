package org.application.deploy.entity.base;

import lombok.Data;
import org.application.deploy.K8enum.KBMemUnit;

/**
 * 资源大小 CPU 内存
 */
@Data
public class KBResource {
    // 1000 表示 1个 cpu
    private Integer cpu;
    // 内存大小
    private Integer memory;
    // 内存单位
    private KBMemUnit kbMemUnit;
}
