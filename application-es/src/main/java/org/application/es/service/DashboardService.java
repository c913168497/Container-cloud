package org.application.es.service;

import com.alibaba.fastjson.JSONObject;
import com.szzt.smart.framework.kafka.LuImmediateMessage;
import com.szzt.smart.framework.util.JsonUtil;
import com.szzt.smart.framework.web.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.entity.DashboardData;
import org.application.es.entity.DashboardEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import static org.application.es.constant.Constants.*;

/**
 * @Auther: csz
 * @Date: 2018/8/31 17:13
 * @Description: 监控数据
 */
@Service
@Slf4j
public class DashboardService {

    @KafkaListener(topics = "dashboard_log", containerFactory = "kafkaListenerContainerFactory")
    public void dashboardListen(LuImmediateMessage luImmediateMessage) {
        String source = luImmediateMessage.getSource();
        String message =  luImmediateMessage.getMessage();
        if(StringUtils.isNotEmpty(source)&&StringUtils.isNotEmpty(message)){
            if(source.contains("_")){
                String ip = source.substring(34,source.indexOf("_"));
                if(StringUtils.isNotEmpty(ip)){
                    DashboardData dashboardData = JsonUtil.str2obj(message, DashboardData.class);
                    if(dashboardData!=null){
                        String objStr = JSONObject.toJSONString(new DashboardEntity(ip, UUID.randomUUID().toString(),message, DateUtil.formatDatetime(new Date())));
                        JSONObject jsonObject = JSONObject.parseObject(objStr);
                        ElasticsearchUtil.addData(jsonObject, INDEX_DASHBOARD , TYPE_DASHBOARD);

                        //添加单机的总量数据
                        String dashboardTotalStr = JSONObject.toJSONString(new DashboardEntity(ip, UUID.randomUUID().toString(),JsonUtil.obj2string(dashboardData.getHost()), DateUtil.formatDatetime(new Date())));
                        JSONObject dashboardTotalJson = JSONObject.parseObject(dashboardTotalStr);
                        ElasticsearchUtil.addData(dashboardTotalJson, INDEX_DASHBOARD_TOTAL , TYPE_DASHBOARD_TOTAL,ip);
                    }
                }
            }
        }
    }
}
