package org.application.es.components.listener;

import lombok.extern.slf4j.Slf4j;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.components.queue.DeployLogQueue;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static org.application.es.constant.Constants.INDEX_DEPLOY;
import static org.application.es.constant.Constants.TYPE_DEPLOY;

@Slf4j
@Component
public class DeployLogListener implements Runnable , ApplicationRunner {

    public void run() {
        while (true) {
            try {
                if(DeployLogQueue.isEmpty()){
                    Thread.sleep(2000);
                }
                List<Map<String,Object>> datas = DeployLogQueue.poll(50);
                if(datas == null || datas.size() == 0) continue;
                log.info("batch save deploy log, array size:{}", datas.size());
                ElasticsearchUtil.batchAdd(INDEX_DEPLOY, TYPE_DEPLOY,datas);
            } catch (Exception e) {
                log.error("deploy listener error :{}", e.getCause());
            }
        }
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("open deploy log listener thread...");
        new Thread(this).start();
        log.info("open success...");
    }
}
