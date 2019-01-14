package org.application.es.interpte;


import lombok.extern.slf4j.Slf4j;
import org.application.es.constant.ElasticConfigration;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.net.InetAddress;


/**
 * @Title:
 * @Package
 * @Description: TODO project 初始化 环境
 * @author cnc
 * @date
 * @version V1.0
 */
@Configuration
@Slf4j
public class InitBeanConfigration extends WebMvcConfigurerAdapter {

    @Autowired
    private ElasticConfigration elasticConfigration;

    /**
     * Bean name default  函数名字
     * @return
     */
    @Bean(name = "transportClient")
    public TransportClient transportClient() {
        log.info("initializing elasticsearch............");
        TransportClient transportClient = null;
        try {
            // 配置信息
            Settings esSetting = Settings.builder()
                    .put("cluster.name", elasticConfigration.getClusterName()) //集群名字
                    .put("client.transport.sniff", true)//增加嗅探机制，找到ES集群
                    .put("thread_pool.search.size", Integer.parseInt(elasticConfigration.getPoolSize()))//增加线程池个数，暂时设为5
                    .build();
            //配置信息Settings自定义
            transportClient = new PreBuiltTransportClient(esSetting);
            TransportAddress transportAddress = new TransportAddress(InetAddress.getByName(elasticConfigration.getHostName()), Integer.valueOf(elasticConfigration.getPort()));
            transportClient.addTransportAddresses(transportAddress);
        } catch (Exception e) {
            log.error("elasticsearch TransportClient create error!!", e);
        }
        return transportClient;
    }

    /**
     * 拦截器配置
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册监控拦截器
        registry.addInterceptor(new GlobalInterceptor())
                .addPathPatterns("/**");
        //如果要 对 /configuration/ui 进行不 验证拦截则 加上
        // .excludePathPatterns("/configuration/ui")
    }

}
