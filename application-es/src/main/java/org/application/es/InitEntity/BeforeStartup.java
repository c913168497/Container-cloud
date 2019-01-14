package org.application.es.InitEntity;

import lombok.extern.slf4j.Slf4j;
import org.application.es.components.ElasticsearchUtil;
import org.application.es.constant.Constants;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

import static org.application.es.constant.Constants.*;

@Configuration
@Slf4j
public class BeforeStartup implements CommandLineRunner {



    @Override
    public void run(String... strings) throws Exception {
        init();
    }

    public void init() {

        ElasticsearchUtil.updateSetting(Constants.MAX_RESULT_WINDOW);

        if (!ElasticsearchUtil.isIndexExist(INDEX_BUILD)){
            ElasticsearchUtil.createIndex(INDEX_BUILD);
            ElasticsearchUtil.putMapping(INDEX_BUILD, TYPE_BUILD);
            log.info("--------Create " + INDEX_BUILD + " Index----------");
        }
        if (!ElasticsearchUtil.isIndexExist(INDEX_DEPLOY)){
            ElasticsearchUtil.createIndex(INDEX_DEPLOY);
            ElasticsearchUtil.putMapping(INDEX_DEPLOY, TYPE_DEPLOY);
            log.info("--------Create " + INDEX_DEPLOY + " Index----------");
        }
        if (!ElasticsearchUtil.isIndexExist(INDEX_BUSSINESS_APP)){
            ElasticsearchUtil.createIndex(INDEX_BUSSINESS_APP);
            ElasticsearchUtil.putMapping(INDEX_BUSSINESS_APP, TYPE_BUSSINESS_APP);
            log.info("--------Create " + INDEX_BUSSINESS_APP + " Index----------");
        }
    }
}