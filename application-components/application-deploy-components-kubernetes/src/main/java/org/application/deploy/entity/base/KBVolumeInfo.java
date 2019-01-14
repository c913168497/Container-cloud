package org.application.deploy.entity.base;

import lombok.Data;
import org.application.deploy.K8enum.KBMemUnit;
import org.application.deploy.K8enum.KBPathCheckType;

@Data
public class KBVolumeInfo {
    //卷挂载名称唯一标识 （注意 必填， 且必须小写）
    private String name;
    // 宿主机 卷路径
    private String hostVolumePath;
    // 卷挂载 大小
    private Integer size;
    // 卷挂载 单位
    private KBMemUnit unit;
    // 卷挂载检查方式
    private KBPathCheckType kbPathCheckType;
    // 容器卷挂载路径 （如果是公共卷，该属性不必填）
    private String containerVolumePath;
    // 是否只读 true 只读 ，false 可读可写
    private Boolean readOnly;
}
