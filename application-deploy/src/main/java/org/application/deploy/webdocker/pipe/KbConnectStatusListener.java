package org.application.deploy.webdocker.pipe;

import io.fabric8.kubernetes.client.dsl.ExecListener;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;

/**
 * @author Sen
 */
@Slf4j
public  class KbConnectStatusListener implements ExecListener {

    volatile boolean isConnected;

    private DockerSocketPipeRunnable dockerSocketPipeRunnable;

    public boolean isConnected() {
        return isConnected;
    }

    public KbConnectStatusListener(DockerSocketPipeRunnable dockerSocketPipeRunnable) {
        this.isConnected = false;
        this.dockerSocketPipeRunnable = dockerSocketPipeRunnable;
    }

    @Override
        public void onOpen(Response response) {
            log.info("已连接");
            this.isConnected = true;
            this.dockerSocketPipeRunnable.needConnected = false;
        }
        @Override
        public void onFailure(Throwable t, Response response) {
            log.info("连接失败关闭{}", t.toString());
            t.printStackTrace();
            this.isConnected = false;
            this.dockerSocketPipeRunnable.keepConnected = false;
            this.dockerSocketPipeRunnable.needConnected = true;
        }

        @Override
        public void onClose(int code, String reason) {
            log.info("连接关闭");
            this.isConnected = false;
            dockerSocketPipeRunnable.keepConnected = false;

            this.dockerSocketPipeRunnable.needConnected = true;
        }
    }