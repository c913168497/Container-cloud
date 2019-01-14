package org.application.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper =  false)
@AllArgsConstructor
@NoArgsConstructor
public class DeployLogEntity {

    private String opid;
    // 日志信息
    private String message;
    // 实例ID
    private String appInstId;
    // 流程ID
    private String workInstId;
    // 从文件读取到的日志ID
    private String logId;
    // 创建时间
    private String createtime;

  /*  private static class MessageLog{
        private final static DeployLogEntity message = new DeployLogEntity();
    }*/

  /*  public static DeployLogEntity sendMessageLog(String opid, String message, String appInstId, String workInstId, String logId, String createtime){
        DeployLogEntity.MessageLog.message.setOpid(opid);
        DeployLogEntity.MessageLog.message.setMessage(message);
        DeployLogEntity.MessageLog.message.setAppInstId(appInstId);
        DeployLogEntity.MessageLog.message.setWorkInstId(workInstId);
        DeployLogEntity.MessageLog.message.setLogId(logId);
        DeployLogEntity.MessageLog.message.setCreatetime(createtime);
        return DeployLogEntity.MessageLog.message;
    }*/
}
