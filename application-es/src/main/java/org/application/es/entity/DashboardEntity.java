package org.application.es.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper =  false)
public class DashboardEntity {
        private String opid;
        private String ip;
        private String message;
        private String createtime;

        public DashboardEntity(String ip, String opid,String message, String createtime){
                this.ip = ip;
                this.createtime = createtime;
                this.opid = opid;
                this.message  = message;
        }
}