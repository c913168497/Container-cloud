package org.application.es.components;


import com.alibaba.fastjson.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.application.common.utils.JsonUtil;
import org.application.es.constant.Constants;
import org.application.es.entity.*;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequestBuilder;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest.RefreshPolicy;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@Slf4j
public class ElasticsearchUtil {

    private static Gson gson = new GsonBuilder().create();

    @Autowired
    private TransportClient transportClient;

    private static TransportClient client;

    @PostConstruct
    public void init() {
       client = this.transportClient;
    }

    /**
     * 最大分页返回结果条数
     * @param index
     */
    public static void updateSetting(Integer index){
        Map<String,Integer> map = new HashMap<>();
        map.put("max_result_window", index);
        UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest();
        updateSettingsRequest.settings(map);
        client.admin().indices().updateSettings(updateSettingsRequest);
    }
    /**
     * 创建索引
     *
     * @param index
     * @return
     */
    public static boolean createIndex(String index) {
        if (!isIndexExist(index)) {
            log.info("Index is not exits!");
        }
        CreateIndexResponse indexResponse = client.admin().indices().prepareCreate(index).execute().actionGet();
        return indexResponse.isAcknowledged();
    }

    public static void putMapping(String index, String typeName) {

        PutMappingRequestBuilder mappingRequest = null;
        try {
            mappingRequest = client.admin().indices().preparePutMapping(index)
                    .setType(typeName)
                    .setSource(createModelMapping(typeName));
            mappingRequest.execute().actionGet();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     * 初始化日志 mapping
     *
     * @param typeName
     * @return
     * @throws Exception
     */
    private static XContentBuilder createModelMapping(String typeName) throws Exception {

        XContentBuilder mapping = null;
        // 构建日志
        if (Constants.TYPE_BUILD.equals(typeName)) {
            log.info("初始化 " + Constants.TYPE_BUILD + " 结构");
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(typeName)
                    .startObject("properties")
                    .startObject("id").field("type", "text").endObject()
                    .startObject("message").field("type", "text").endObject()
                    .startObject("templateId").field("type", "text").endObject()
                    .startObject("instId").field("type", "text").endObject()
                    .startObject("opid").field("type", "text").endObject()
                    .startObject("createtime").field("type", "text").field("fielddata", true).endObject()
                    .endObject()
                    .endObject()
                    .endObject();

        }
        // 发布日志
        if (Constants.TYPE_DEPLOY.equals(typeName)) {
            log.info("初始化 " + Constants.TYPE_DEPLOY + " 结构");
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(typeName)
                    .startObject("properties")
                    .startObject("id").field("type", "text").endObject()
                    .startObject("opid").field("type", "text").endObject()
                    .startObject("message").field("type", "text").endObject()
                    .startObject("appInstId").field("type", "text").endObject()
                    .startObject("workInstId").field("type", "text").endObject()
                    .startObject("logId").field("type", "double").endObject()
                    .startObject("createtime").field("type", "text").field("fielddata", true).endObject()
                    .endObject()
                    .endObject()
                    .endObject();
        }

        //业务日志
        if (Constants.TYPE_BUSSINESS_APP.equals(typeName)) {
            log.info("初始化 " + Constants.TYPE_BUSSINESS_APP + " 结构");
            mapping = XContentFactory.jsonBuilder()
                    .startObject()
                    .startObject(typeName)
                    .startObject("properties")
                    .startObject("appInstId").field("type", "text").endObject()
                    .startObject("message").field("type", "text").endObject()
                    .startObject("createTime").field("type", "text").field("fielddata", true).endObject()
                    .endObject()
                    .endObject()
                    .endObject();
        }
        return mapping;
    }

    /**
     * 删除索引
     *
     * @param index
     * @return
     */
    public static boolean deleteIndex(String index) {
        if (!isIndexExist(index)) {
            log.info("Index is not exits!");
        }
        DeleteIndexResponse dResponse = client.admin().indices().prepareDelete(index).execute().actionGet();
        if (dResponse.isAcknowledged()) {
            log.info("delete index " + index + "  successfully!");
        } else {
            log.info("Fail to delete index " + index);
        }
        return dResponse.isAcknowledged();
    }

    /**
     * 判断索引是否存在
     *
     * @param index
     * @return
     */
    public static boolean isIndexExist(String index) {
        IndicesExistsResponse inExistsResponse = client.admin().indices().exists(new IndicesExistsRequest(index)).actionGet();
        if (inExistsResponse.isExists()) {
            log.info("Index [" + index + "] is exist!");
        } else {
            log.info("Index [" + index + "] is not exist!");
        }
        return inExistsResponse.isExists();
    }

    /**
     * 数据添加，正定ID
     *
     * @param jsonObject 要增加的数据
     * @param index      索引，类似数据库
     * @param type       类型，类似表
     * @param id         数据ID
     * @return
     */
    public static String addData(JSONObject jsonObject, String index, String type, String id) {
        IndexResponse response = client.prepareIndex(index, type, id).setSource(jsonObject).setRefreshPolicy(RefreshPolicy.IMMEDIATE).get();
        log.info("addData response status:{},id:{}", response.status().getStatus(), response.getId());
        return response.getId();
    }


    /**
     * 通过ID删除数据
     *
     * @param index 索引，类似数据库
     * @param type  类型，类似表
     * @param id    数据ID
     */
    public static void deleteDataById(String index, String type, String id) {
        DeleteResponse response = client.prepareDelete(index, type, id).setRefreshPolicy(RefreshPolicy.IMMEDIATE).execute().actionGet();
        log.info("deleteDataById response status:{},id:{}", response.status().getStatus(), response.getId());
    }


    /**
     * 日志消息替换
     *
     * @param message
     * @return
     */
    public static String logAddTimeInfo(String message) {
        String regex = ".*\\d{4}[-|/]\\d{1,2}[-|/]{1,2}.+";
        if (Optional.ofNullable(message).isPresent() && !message.trim().matches(regex)) {
            String format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss").format(new Date());
            message = format.concat(" INFO ").concat(message);
        }
        return message;
    }



    /**
     * 分页结果集转换
     *
     * @param shs
     * @param pageNum
     * @param pageSize
     * @param data
     * @param <T>
     * @return
     */
    private static <T> PageResult<T> toPageResult(SearchHits shs, int pageNum, int pageSize, List<T> data) {

        PageResult<T> page = new PageResult<>();

        page.setPageNum(pageNum);
        page.setPageSize(pageSize);
        page.setTotalNum(shs.getTotalHits());

        long pageTotal = 0;

        if (shs.getTotalHits() > 0) {

            pageTotal = (shs.getTotalHits() / pageSize) + 1;

            if (pageNum <= pageTotal) {

                page.setData(data);

            }
        }
        page.setData( Optional.ofNullable( page.getData() ).isPresent() ? page.getData() : new ArrayList<>() );
        page.setTotalPage(pageTotal);
        return page;
    }


    //######### 基础方法 #############

    /**
     * 数据添加
     *
     * @param jsonObject 要增加的数据
     * @param index      索引，类似数据库
     * @param type       类型，类似表
     * @return
     */
    public static String addData(JSONObject jsonObject, String index, String type) {
        return addData(jsonObject, index, type, UUID.randomUUID().toString().replaceAll("-", "").toUpperCase());
    }


    public static void batchAdd(String index, String type, List<Map<String,Object>> datas) {

        if(!Optional.ofNullable(datas).isPresent() || datas.size() == 0) return ;
        BulkRequestBuilder bulkRequestBuilder = client.prepareBulk().setRefreshPolicy(RefreshPolicy.NONE) ;
        Optional.ofNullable(datas).ifPresent( ds -> {
            ds.forEach(m -> {
                String id = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase() ;
                bulkRequestBuilder.add(client.prepareIndex(index, type, id).setSource(m));
            });
        });
        BulkResponse response =  bulkRequestBuilder.execute().actionGet();
        log.info("bulkresponse status: {}" ,response.status());
    }


    /**
     * 通过ID 更新数据
     *
     * @param jsonObject 要增加的数据
     * @param index      索引，类似数据库
     * @param type       类型，类似表
     * @param id         数据ID
     * @return
     */
    public static void updateDataById(JSONObject jsonObject, String index, String type, String id) {
        UpdateRequest updateRequest = new UpdateRequest();
        updateRequest.index(index).type(type).id(id).doc(jsonObject);
        client.update(updateRequest);
    }

    /**
     * 通过ID获取数据
     *
     * @param index  索引，类似数据库
     * @param type   类型，类似表
     * @param id     数据ID
     * @param fields 需要显示的字段，逗号分隔（缺省为全部字段）
     * @return
     */
    public static Map<String, Object> searchDataById(String index, String type, String id, String fields) {

        GetRequestBuilder getRequestBuilder = client.prepareGet(index, type, id);
        if (StringUtils.isNotBlank(fields)) {
            getRequestBuilder.setFetchSource(fields.split(","), null);
        }
        GetResponse getResponse = getRequestBuilder.execute().actionGet();
        return getResponse.getSource();
    }

}