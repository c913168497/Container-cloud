package org.application.deploy.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Sen
 */
@Slf4j
@Component
@ServerEndpoint("/token/{token}/initId/{initId}")
public class WebSocketServer {

    //当前在线连接数。
    private volatile static  int onlineCount = 0;
    //用来存放每个客户端对应的MyWebSocket对象。
    private static ConcurrentHashMap<String , Session> tokenInitSessionMap = new ConcurrentHashMap<>();
    /**
     * 连接建立成功调用的方法
     * token 需要接入授权
     */
    @OnOpen
    public void onOpen(Session session, @PathParam(value = "token") String token, @PathParam(value = "initId") String initId) throws IOException {
            log.info("连接一打开");
    }



    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam(value = "token") String token, @PathParam(value = "initId") String initId) {

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
        log.info("message： " + message);
    }


    /**
     * @param session
     * @param error
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("发生错误");
        error.printStackTrace();
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

}
