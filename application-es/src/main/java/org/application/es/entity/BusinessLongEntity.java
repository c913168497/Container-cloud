package org.application.es.entity;

import lombok.Data;

@Data
public class BusinessLongEntity {

    private String appInstId;
    // 日志信息
    private String message;
    // 创建时间
    private String createTime;


    private static class MessageLog{
        private final static BusinessLongEntity message = new BusinessLongEntity();
    }

    public static BusinessLongEntity sendMessageLog(String message, String appInstId,  String createTime){
        BusinessLongEntity.MessageLog.message.setMessage(message);
        BusinessLongEntity.MessageLog.message.setAppInstId(appInstId);
        BusinessLongEntity.MessageLog.message.setCreateTime(createTime);
        return BusinessLongEntity.MessageLog.message;
    }
}
