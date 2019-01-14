package org.application.es.components.listener;

import lombok.extern.slf4j.Slf4j;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.components.queue.BuildLogQueue;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.application.es.constant.Constants.INDEX_BUILD;
import static org.application.es.constant.Constants.TYPE_BUILD;

@Slf4j
@Component
public class BuilderLogListener implements Runnable , ApplicationRunner {

    public void run() {
        while (true) {
            try {
                if(BuildLogQueue.isEmpty()){
                    Thread.sleep(2000);
                }
                List<Map<String,Object>> datas = BuildLogQueue.poll(50);
                if(datas == null || datas.size() == 0) continue;
                log.info("batch save build log.  array size:{}",datas.size());
                ElasticsearchUtil.batchAdd(INDEX_BUILD, TYPE_BUILD,datas);
            } catch (Exception e) {
                log.error("build log listener error :{}", e.getCause());
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("open build log listener thread  ...");
        new Thread(this).start();
        log.info("open success...");
    }
}
