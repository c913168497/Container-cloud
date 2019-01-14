package org.application.deploy.webdocker.pipe;

import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.ExecWatch;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;

/**
 * @author Sen
 */
@Slf4j
public class DockerSocketPipeRunnable implements Runnable{

    private KubernetesClient client;
    // 命名空间
    private String namespace;
    // 容器名
    private String podName;
    //写入容器通道
    private PipedInputStream is;
    // websocket数据写入接收管道
    private PipedOutputStream inputStream ;
    // 返回数据 接收通道
    private PipedOutputStream os;
    // 容器保持连接
    volatile boolean keepConnected;
    // 会话状态保持
    volatile boolean needConnected;
    // 默认 100 秒
    private volatile long lifeTime;
    // exec 命令界面
    private ExecWatch execWatcher ;
    // 连接生命周期监听
    private KbConnectStatusListener listener;
    // 连接各种镜像的各个命令（共4个，一般以 前两个较为常见）
    private final static String [] startExecs = { "bash" ,"sh" ,"cmd" , "powershell" };
    // exec 执行命令
    private String startExec;

    public String getStartExec() { return startExec; }
    public PipedOutputStream getOs() {
        return os;
    }
    public KbConnectStatusListener getListener() {
        return listener;
    }
    public PipedOutputStream getInputStream() {
        return inputStream;
    }
    public void flushLifeTime(){this.lifeTime = 100;}
    public void closeKeepConnected(){this.keepConnected = false; this.execWatcher.close();this.needConnected = false;}

    Thread thread;

    public DockerSocketPipeRunnable(KubernetesClient client, String namespace, String podName) throws IOException {
        this.client = client;
        this.namespace = namespace;
        this.podName = podName;
        this.lifeTime = 100;
        this.is = new PipedInputStream();
        this.inputStream = new PipedOutputStream();
        this.os = new PipedOutputStream();
        this.keepConnected = true;
        this.needConnected = true;
        this.is.connect(inputStream);
        this.listener = new KbConnectStatusListener(this);
        log.info("初始化容器通信");
    }

    @Override
    public void run() {
        synchronized (this) {
            thread = Thread.currentThread();
        }
        try {
            if (!Optional.ofNullable( client.pods().inNamespace(namespace).withName(podName).get() ).isPresent()){
                String error = "Container connection failed , No found this instance, Please check your data!";
                log.error(error + " namespace : " + namespace + " podName : " + podName);
                return;
            }
            log.info("开始连接...................");
            for (String startExec : startExecs) {
                 keepConnected = true;
                if ( needConnected ){
                    this.startExec = startExec;
                    connectionPodDocker( startExec );
                }
            }
        } catch (Exception e) {
            log.error("连接到容器出现预期之外的错误，请检查：".concat(e.getMessage()));
            e.printStackTrace();
        }finally {
            try {
                is.close();
                inputStream.close();
            }catch (Exception e){
                e.printStackTrace();
                log.error("关闭数据流异常" + e.getMessage());
            }
        }
    }


    public void connectionPodDocker(String startExec){
        try (
                ExecWatch execWatch = client.pods().inNamespace( namespace ).withName( podName )
                        .readingInput( is)
                        .writingOutput( os )
                        .writingError( os )
                        .withTTY()
                        .usingListener( listener )
                        .exec(new String[]{"env", "COLUMNS=150", "LINES=100", startExec})
        ){
            execWatcher = execWatch;
            while (lifeTime > 0 && keepConnected){
                Thread.sleep(1000);
                lifeTime -= 1;
            }
        }catch (Exception e){

            if (!keepConnected) {
                return;
            }

            if (!thread.isInterrupted()) {
                log.error("Error while pumping stream.", e);
            } else {
                log.debug("Interrupted while pumping stream.");
            }
        }
    }
}
