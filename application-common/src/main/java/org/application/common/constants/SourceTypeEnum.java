package org.application.common.constants;

public enum SourceTypeEnum {

    SVN("svn"),
    GIT("git"),
    FILE("file"),
    DOCKER("docker");
    public String value  ;

    private SourceTypeEnum(String value) {
        this.value = value ;
    }

}
