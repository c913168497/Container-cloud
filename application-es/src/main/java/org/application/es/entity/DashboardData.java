package org.application.es.entity;

import lombok.Data;

/**
 * @Auther: csz
 * @Date: 2018/8/31 19:04
 * @Description:
 */
@Data
public class DashboardData {
    private Host host; //本机信息

    private Application application; //容器信息
}
