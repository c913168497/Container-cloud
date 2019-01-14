package org.application.deploy.K8enum;

/**
 * 主机目录路径是否存在检测方式
 */
public enum  KBPathCheckType {

    DIRECTORYORCREATE("DirectoryOrCreate"), //目录不存在则创建
    FILEORCREATE("FileOrCreate"), //文件不存在则创建
    DIRECTORY("Directory"), //已存在的现有的目录
    FILE("File"),//已存在的现有文件
    NONE(""); // 不检查目标路径

    private String value;

    KBPathCheckType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
