package org.application.common.common;

// 环境变量类型   1：环境变量 2：端口映射 3: 配置文件 4: 卷挂载 5: 日志挂载 6.资源属性限制（cpu,memory）
    public enum VarsType {
        ENV_VAR(1),

        ENV_PORT(2),

        ENV_FILE(3),

        ENV_VOLUME(4),

        ENV_LOG(5),

        RESOURCE_LIMIT(6);

        private Integer value;

        VarsType(Integer value) {
            this.value = value;
        }

        public Integer getValue() {
            return value;
        }
    }