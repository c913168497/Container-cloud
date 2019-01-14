package org.application.es.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper =  false)
@NoArgsConstructor
@AllArgsConstructor
public class BuildLogEntity {

        private String instId;

        private String opid;

        private String message;

        private String templateId;

        private String createtime;

     /*   private static class MessageLog{
            private final static BuildLogEntity message = new BuildLogEntity();
        }
        public static BuildLogEntity sendMessageLog(String opid, String instId, String templateId, String message, String createtime){
                MessageLog.message.setMessage(message);
                MessageLog.message.setTemplateId(templateId);
                MessageLog.message.setOpid(opid);
                MessageLog.message.setInstId(instId);
                MessageLog.message.setCreatetime(createtime);
                return MessageLog.message;
        }*/
}