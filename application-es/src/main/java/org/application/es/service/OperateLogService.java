package org.application.es.service;

import com.szzt.smart.framework.kafka.LuImmediateMessage;
import com.szzt.smart.framework.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.constant.ElasticConfigration;
import org.application.es.entity.OperateLogDomain;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * @Auther: Administrator
 * @Date: 2018/9/5 0005 14:24
 * @Description: 操作日志监听
 */
@Service
@Slf4j
public class OperateLogService {
    @Autowired
    private ElasticConfigration elasticConfigration;

    @KafkaListener(topics = "operate_log", containerFactory = "kafkaListenerContainerFactory")
    public void operateLogListen(LuImmediateMessage luImmediateMessage){
        log.info("----------操作日志：" + luImmediateMessage.toString());
        if(StringUtils.isBlank(luImmediateMessage.getMessage()))
            return;
        OperateLogDomain domain = null;
        try {
            domain = JsonUtil.str2obj(luImmediateMessage.getMessage(), OperateLogDomain.class);
            domain.setCreateTime(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(Calendar.getInstance().getTime()));
            domain.setCreateTimeFormart(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
            ElasticsearchUtil.saveOperationLog(domain);
        }catch (Exception e){
            log.error("保存操作日志异常,异常信息,{}",e.getMessage());
        }
    }

    // 业务日志监听
    @KafkaListener(topics = "business_log", containerFactory = "kafkaListenerContainerFactory")
    public void businessLogListen(LuImmediateMessage luImmediateMessage){

        String source = luImmediateMessage.getSource();

        if( StringUtils.isBlank(source) ) {

            return;

        }

        String message = luImmediateMessage.getMessage();

        if(StringUtils.isBlank( message )) {

            return;

        }

        String msgId = source.substring(elasticConfigration.getLogPath().length(), source.length());

        ElasticsearchUtil.saveBussinesLog(message , msgId);

    }
}
