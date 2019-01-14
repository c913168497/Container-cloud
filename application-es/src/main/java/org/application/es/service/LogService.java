package org.application.es.service;

import com.szzt.smart.framework.kafka.LuImmediateMessage;
import com.szzt.smart.framework.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.components.queue.BuildLogQueue;
import org.application.es.components.queue.DeployLogQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @author cnc
 */
@Service
@Slf4j
public class LogService {

    @Autowired
    private ElasticsearchUtil elasticsearchUtil;

    @KafkaListener(topics = "ansible_log",containerFactory = "kafkaListenerContainerFactory")
    public void ansibleListen(LuImmediateMessage luImmediateMessage) {
        String source = luImmediateMessage.getSource();
        String message = luImmediateMessage.getMessage();
        log.info("Es ansible_log : " +  JsonUtil.obj2string(luImmediateMessage));
        String msgId = source.substring(source.lastIndexOf("/") + 1, source.lastIndexOf(".log"));
        DeployLogQueue.add(luImmediateMessage.getOpid(),msgId ,message);
    }

    /**
     * 发布日志分发
     */
    @KafkaListener(topics = "deploy_log", containerFactory = "kafkaListenerContainerFactory")
    public void deployLog(LuImmediateMessage luImmediateMessage) {
        log.info("----------Es 发布日志：" + JsonUtil.obj2string(luImmediateMessage));
        elasticsearchUtil.saveDeployLog(luImmediateMessage);
        DeployLogQueue.add(luImmediateMessage.getOpid(),luImmediateMessage.getMessage());
    }

    /**
     * 构建日志分发
     */
    @KafkaListener(topics = "build_log", containerFactory = "kafkaListenerContainerFactory")
    public void receiveSpiderMsg(LuImmediateMessage luImmediateMessage) {
        log.info("----------Es 构建日志：" + JsonUtil.obj2string(luImmediateMessage));
       // elasticsearchUtil.saveBuildLog(luImmediateMessage);
        BuildLogQueue.add(luImmediateMessage.getOpid(),luImmediateMessage.getMessage());
    }



    private int size = 0 ;
    @KafkaListener(topics = "demo", containerFactory = "kafkaListenerContainerFactory")
    public void demo(LuImmediateMessage luImmediateMessage) {
        size ++ ;
        log.info("收到消息:{}",size);
    }

}
