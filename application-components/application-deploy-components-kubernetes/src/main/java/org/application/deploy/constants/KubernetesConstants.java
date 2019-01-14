package org.application.deploy.constants;

public class KubernetesConstants {
    public final static String httpHead = "http://";

    public final static String colon = ":";

    public final static String containerHead = "kb-";

    public static final String DESTORY_SRCRIPT = "/opt/script/do_delete_deploy.sh";

    /**
     * OperateType 操作类型发布或者 更新
     */
    public enum OperateType {
        DEPLOY,
        START,
        STOP,
        RESTART,
        DESTORY,
        UPDATE;
    }
}
