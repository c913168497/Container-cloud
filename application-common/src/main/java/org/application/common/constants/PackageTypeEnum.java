package org.application.common.constants;

/**
 * 应用类型
 */
public enum PackageTypeEnum {

    MAVEN("maven"),
    GRADLE("gradle"),
    VUE("vue"),
    ANT("ant") ;
    public String value  ;

    private PackageTypeEnum(String value) {
        this.value = value ;
    }



}
