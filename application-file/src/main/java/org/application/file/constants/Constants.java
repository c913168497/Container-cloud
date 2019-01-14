package org.application.file.constants;

public class Constants {

    // 每天 凌晨1点 执行 (清除 未上传 成功 的 分片文件)
    public final static String oneDay = "0 0 1 * * ?";
     // kafka topic
    public static final String DEPLOY_CONTENT_TOPIC = "deploy_content";
}
