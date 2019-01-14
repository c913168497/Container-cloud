package org.application.es.components.queue;

import com.alibaba.fastjson.JSONObject;
import com.szzt.smart.framework.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.application.es.entity.BuildLogEntity;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 发布日志队列
 */
@Slf4j
public class BuildLogQueue {


   //队列
   private static ArrayBlockingQueue<String> queue = new ArrayBlockingQueue<String>(10000,true ,Collections.EMPTY_LIST);


   /**
    * 接受消息
    * @param id
    * @param message
    */
   public static  void add(String id, String message) {
      try {
         BuildLogEntity logEntity = JsonUtil.str2obj(message, BuildLogEntity.class);
         logEntity.setOpid(id);
         logEntity.setCreatetime(new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
         logEntity.setMessage(dealTimes(logEntity.getMessage()));
         queue.add(JSONObject.toJSONString(logEntity));
      } catch (Exception e) {
         log.error("添加到队列失败:{}", e.getCause());
      }
   }

   public static List<Map<String,Object>> poll(int size) {
      int i = 0 ;
      String log = "" ;
      List<Map<String,Object>> datas = new ArrayList<>();
      while( i ++ <= size && log != null ) {
         log = queue.poll();
         Optional.ofNullable(log).ifPresent(l -> {
            datas.add(JSONObject.parseObject(l));
         });
      }
      return datas ;
   }


   private static String dealTimes(String message) {
      String regex = ".*\\d{4}[-|/]\\d{1,2}[-|/]{1,2}.+";
      if (Optional.ofNullable(message).isPresent() && !message.trim().matches(regex)) {
         String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").format(new Date());
         message = format.concat(" INFO ").concat(message);
      }
      return message;
   }

   public static boolean isEmpty(){
      return queue.isEmpty();
   }
}
