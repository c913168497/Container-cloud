package org.application.es.constant;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;


@Configuration
@Data
public class ElasticConfigration {

    @Value("${elasticsearch.ip}")
    private String hostName;
    /**
     * 端口
     */
    @Value("${elasticsearch.port}")
    private String port;
    /**
     * 集群名称
     */
    @Value("${elasticsearch.cluster-name}")
    private String clusterName;
    /**
     * 日志路径
     */
    @Value("${elasticsearch.log-path}")
    private  String logPath;
    /**
     * 连接池
     */
    @Value("${elasticsearch.pool}")
    private String poolSize;
}
