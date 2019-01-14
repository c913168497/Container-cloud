package org.application.deploy.K8enum;


public enum KBSelector {
    //** 工作负载选择器
    WORK_LOAD("workload.user.cattle.io/workloadselector");

    private String value;

    KBSelector(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
