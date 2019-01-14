package org.application.deploy.webdocker.pipe;

import com.appcloud.apigateway.webdocker.WebSocketServer;
import lombok.extern.slf4j.Slf4j;

import javax.websocket.Session;
import java.io.IOException;
import java.io.PipedInputStream;

/**
 * @author Sen
 */

@Slf4j
public class WebSocketPipeRunnable implements Runnable{

    private Session session;

    private PipedInputStream outPutOs;

    volatile boolean keepReading;

    private DockerSocketPipeRunnable runnable;

    Thread thread;

    public WebSocketPipeRunnable(Session session, DockerSocketPipeRunnable runnable) {
        this.session = session;
        this.outPutOs = new PipedInputStream();
        this.keepReading = true;
        this.runnable = runnable;
        log.info("初始化web通道");
    }

    public PipedInputStream getOutPutOs() {
        return outPutOs;
    }

    public void closeKeepReading(){this.keepReading = false;}

    @Override
    public void run() {

        synchronized (this) {

            thread = Thread.currentThread();

        }

        byte[] buffer = new byte[1024];

        try {

            int length;
            boolean clearError = true;
            while (runnable.getListener().isConnected && keepReading && !Thread.currentThread().isInterrupted() && (length = outPutOs.read(buffer)) != -1) {

                byte[] actual = new byte[length];

                System.arraycopy(buffer, 0, actual, 0, length);
                String message = new String(actual);
                if (clearError){
                   message = message.replaceAll("env: can't execute '[\\s\\S]*?': No such file or directory\r\n", "");
                }
                clearError = false;
                WebSocketServer.sendMessage(session,  message);

            }

        } catch (IOException e) {

            if (!keepReading) {
                return;
            }

            if (!thread.isInterrupted()) {
                log.error("Error while pumping stream.", e);
            } else {
                log.debug("Interrupted while pumping stream.");
            }

        }finally {
            try{
                outPutOs.close();
            }catch (Exception e){
                log.error(e.getMessage());
            }
        }
    }

}