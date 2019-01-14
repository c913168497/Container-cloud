package org.application.es.service;

import com.szzt.smart.framework.kafka.LuImmediateMessage;
import com.szzt.smart.framework.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.application.es.entity.HistoryDomain;
import org.application.es.invoke.AppFeignDatalApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

/**
 * @Auther: Administrator
 * @Date: 2018/9/5 0005 14:24
 * @Description: 操作日志监听
 */
@Service
@Slf4j
public class HistoryLogService {
    @Autowired
    private  AppFeignDatalApi appFeignDatalApi;

    @KafkaListener(topics = "history_log", containerFactory = "kafkaListenerContainerFactory")
    public void historyLogListen(LuImmediateMessage luImmediateMessage){
        log.info("----------构建、发布历史记录：" + luImmediateMessage.toString());
        if(StringUtils.isBlank(luImmediateMessage.getMessage()))
            return;
        HistoryDomain domain = null;
        try {
            domain = JsonUtil.str2obj(luImmediateMessage.getMessage(), HistoryDomain.class);
            appFeignDatalApi.saveHistory(domain);
        }catch (Exception e){
            log.error("保存构建、发布历史记录异常,异常信息,{}" ,e.getMessage());
        }
    }
}
