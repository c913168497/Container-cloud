package org.application.es.entity;

import lombok.Data;

/**
 * @Auther: csz
 * @Date: 2018/8/27 17:41
 * @Description:
 */
@Data
public class Deploy {
    private String name; //应用名称

    private String id; //应用id

    private String cpu; //cpu占用率

    private String netIo; //网络io

    private String memPerc;

    private String memUsage;

    public void setNetIo(String netIo){
        this.netIo = netIo.substring(0,netIo.length()-1);
    }
}
