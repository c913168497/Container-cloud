package org.application.common.entity;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 资源信息
 */
@Data
public class Resource {

   // 文件路径参数
   private String path ;
   // 名称
   private String projectName;

   private String imageVersion;

   private String imageRepositryName;

   private String houseType;

   private Map<String,String> envs = new HashMap<>();
}
