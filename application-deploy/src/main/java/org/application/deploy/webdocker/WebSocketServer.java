package org.application.deploy.webdocker;

import com.appcloud.apigateway.entity.AppInstance;
import com.appcloud.apigateway.service.ApiGatewayService;
import com.appcloud.apigateway.webdocker.client.KuberneteClinet;
import com.appcloud.apigateway.webdocker.pipe.DockerSocketPipeRunnable;
import com.appcloud.apigateway.webdocker.pipe.WebSocketPipeRunnable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.*;

/**
 * @author Sen
 */
@Slf4j
@Component
@ServerEndpoint("/token/{token}/initId/{initId}")
public class WebSocketServer {
    @Autowired
    private ApiGatewayService apiGatewayService;

    private static WebSocketServer webSocketServer;
    //用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String , Session> tokenInitSessionMap = new ConcurrentHashMap<>();

    //用来存放每个客户端对应的KuberneteUtils对象。
    private static ConcurrentHashMap<Session, KuberneteClinet> kuberneteUtilsConcurrentHashMap = new ConcurrentHashMap<>();

    //用来存放每个客户端对应的KuberneteUtils对象。
    private static ConcurrentHashMap<Session, DockerSocketPipeRunnable> kbConnectedDockerConcurrentHashMap = new ConcurrentHashMap<>();

    //用来存放每个客户端对应的KuberneteUtils对象。
    private static ConcurrentHashMap<Session, WebSocketPipeRunnable> webSocketPipeRunnableConcurrentHashMap = new ConcurrentHashMap<>();
    //websocket容器连接线程
    private static ExecutorService executorSendDockerMessage = new ThreadPoolExecutor(
            50, 100, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(10));

    //websocket容器连接线程
    private static ExecutorService executorSendWebMessage = new ThreadPoolExecutor(
            50, 100, 1000, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<Runnable>(10));
    //当前在线连接数。
    private volatile static  int onlineCount = 0;

    @PostConstruct
    public void init() {
        webSocketServer = this;
        webSocketServer.apiGatewayService = this.apiGatewayService;
    }

    /**
     * 连接建立成功调用的方法
     * token 需要接入授权
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "token") String token, @PathParam(value = "initId") String initId) throws IOException {

        if (Optional.ofNullable( tokenInitSessionMap.get( token.concat(initId) ) ).isPresent()){
            sendMessage(session, "Please do not open  the remote connection repeatedly!");
            return;
        }
        // 设置在线人数
        this.setOnlineCount();
        // 获取应用实例信息
        AppInstance appInstance = webSocketServer.apiGatewayService.getAppInstById(token ,initId, session);

        if (!Optional.ofNullable(appInstance).isPresent()){
            session.close();
            return;
        }

        tokenInitSessionMap.put(token.concat(initId), session);

        KuberneteClinet kuberneteUtils = this.getKuberneteClinet(session, appInstance);

        DockerSocketPipeRunnable runnable =  this.initDockerSocketPipeRunnable(session, kuberneteUtils, appInstance);

        WebSocketPipeRunnable sendMessageRunnable = this.initWebSocketPipeRunnable(session, runnable);

        if ( !checkConnectSuccess(runnable) ){

            this.sendMessage(session, "connected fail, please check this pod status !");
            clearnMapCache(session);

            return;
        }
        // 启动线程
        executorSendWebMessage.execute(sendMessageRunnable);
    }

    /**
     * 获取k8s 客户端信息
     * @param session
     * @param appInstance
     * @return
     */
    public KuberneteClinet getKuberneteClinet(Session session, AppInstance appInstance){
        KuberneteClinet kuberneteUtils = kuberneteUtilsConcurrentHashMap.get( session );

        if ( !Optional.ofNullable( kuberneteUtils ).isPresent() ){

            kuberneteUtils = new KuberneteClinet( appInstance.getContainerClusterName() );

            kuberneteUtilsConcurrentHashMap.put( session , kuberneteUtils );

        }
        return kuberneteUtils;
    }

    /**
     * 初始化java连接到容器管道线程
     * @param session
     * @param kuberneteClinet
     * @param appInstance
     * @return
     */
    public DockerSocketPipeRunnable initDockerSocketPipeRunnable(Session session, KuberneteClinet kuberneteClinet, AppInstance appInstance){
        DockerSocketPipeRunnable runnable =  kbConnectedDockerConcurrentHashMap.get(session);
        if (!Optional.ofNullable( runnable ).isPresent()){
            // 装载
            try {
                runnable = new DockerSocketPipeRunnable(kuberneteClinet.getClient(), appInstance.getContainerNameSpace(), appInstance.getContainerProjectId());
                // 启动 连接 容器线程
                executorSendDockerMessage.execute(runnable);
                kbConnectedDockerConcurrentHashMap.put(session, runnable);
            } catch (IOException e) {
                e.printStackTrace();
                log.error(e.getMessage());
            }
        }
        return runnable;
    }

    /**
     * 初始化web 到 java 管道线程
     * @param session
     * @param runnable
     * @return
     */
    public WebSocketPipeRunnable initWebSocketPipeRunnable(Session session, DockerSocketPipeRunnable runnable){
        WebSocketPipeRunnable sendMessageRunnable = webSocketPipeRunnableConcurrentHashMap.get( session );
        if (!Optional.ofNullable( sendMessageRunnable ).isPresent()){
            try {
                sendMessageRunnable = new WebSocketPipeRunnable(session, runnable);
                log.info("通信管道建立中.....");
                sendMessageRunnable.getOutPutOs().connect( runnable.getOs() );
                log.info("通信管道建立成功");
                webSocketPipeRunnableConcurrentHashMap.put(session, sendMessageRunnable);
            } catch (IOException e) {
                log.error("通信管道建立异常");
                log.error(e.getMessage());
                e.printStackTrace();
            }
        }
        return sendMessageRunnable;
    }

    /**
     * 检查容器管道是否连接成功
     * @param runnable
     * @return
     */
    public Boolean checkConnectSuccess(DockerSocketPipeRunnable runnable){
        int i = 0;
        while (!runnable.getListener().isConnected() && i < 10){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            i++;
        }
        return runnable.getListener().isConnected();
    }


    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam(value = "token") String token, @PathParam(value = "initId") String initId) {
        clearnMapCache( tokenInitSessionMap.get(token.concat(initId)) );
        tokenInitSessionMap.remove(token.concat(initId));
        setOnlineCount();           //在线数减1
        log.info(token.concat(initId) + " 已经退出 有一连接关闭！当前在线人数为" + getOnlineCount());
    }

    /**
     * 收到客户端消息后调用的方法
     *
     * @param message 客户端发送过来的消息
     */
    @OnMessage
    public static void onMessage(String message, Session session) {
        DockerSocketPipeRunnable kbConnectedDocker = kbConnectedDockerConcurrentHashMap.get(session);
        if (Optional.ofNullable( kbConnectedDocker ).isPresent()){
            try {
                kbConnectedDocker.flushLifeTime();
                kbConnectedDocker.getInputStream().write(message.getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
        clearnMapCache(session );
        setOnlineCount();           //在线数减1
    }

    public static void sendMessage(Session session, String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            e.printStackTrace();
            log.error("websocket信息发送失败".concat(e.getMessage()));
        }
    }

    public static synchronized int getOnlineCount() {
        return onlineCount;
    }

    public  static synchronized void setOnlineCount() {
        WebSocketServer.onlineCount = tokenInitSessionMap.size();
    }

    public void clearnMapCache(Session session){
        if (!Optional.ofNullable(session).isPresent()){
            return;
        }
        try {
            session.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // 清空 java 连接 到 docker 通道
        DockerSocketPipeRunnable dockerSocketPipeRunnable = kbConnectedDockerConcurrentHashMap.get(session);
        if ( Optional.ofNullable( dockerSocketPipeRunnable ).isPresent() ){
            dockerSocketPipeRunnable.closeKeepConnected();
            kbConnectedDockerConcurrentHashMap.remove(session);
        }

        WebSocketPipeRunnable webSocketPipeRunnable = webSocketPipeRunnableConcurrentHashMap.get(session);

        if (Optional.ofNullable( webSocketPipeRunnable ).isPresent()){
            webSocketPipeRunnable.closeKeepReading();
            webSocketPipeRunnableConcurrentHashMap.remove(session);
        }

        // 清空客户端连接
        KuberneteClinet kuberneteClinet = kuberneteUtilsConcurrentHashMap.get( session );

        if ( Optional.ofNullable( kuberneteClinet ).isPresent()){
            kuberneteUtilsConcurrentHashMap.remove( session );
        }

    }

}
