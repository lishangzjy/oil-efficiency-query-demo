package org.pentaho.di.sdk.samples.steps.demo.dao;

import com.cet.eem.common.definition.ColumnDef;
import com.cet.eem.common.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pentaho.di.core.KettleVariablesList;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 模型查询持久化对象
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/5 18:49
 */
@Repository
public class ModelQueryDao {

    /**
     * rest接口请求工具
     */
    private static RestTemplate restTemplate;

    /**
     * 配置中模型服务IP的键
     */
    private final String MODEL_SERVICE_IP = "model-service-ip";

    /**
     * 配置中模型服务的PORT的键
     */
    private final String MODEL_SERVICE_PORT = "model-service-port";

    /**
     * 元数据查询接口路径
     */
    private final String META_PATH = "/model-meta/v1/models/label/";

    /**
     * 数据查询接口
     */
    private final String DATA_PATH = "/model/v1/query";

    /**
     * kettle环境变量
     */
    private final Map<String, String> kettleVariables = KettleVariablesList.getInstance().getDefaultValueMap();

    public ModelQueryDao() {
        restTemplate = new RestTemplate();
    }


    /**
     * 构建模型查询的URL，不带URL参数
     *
     * @param modelLabel 模型label
     * @param queryType  查询类型
     *                   1. false：查元数据
     *                   2. true：查数据
     * @return 模型元数据查询请求路径
     */
    @SneakyThrows
    private URI buildQueryUri(String modelLabel, boolean queryType) {
        URIBuilder builder = new URIBuilder();
        String host = kettleVariables.get(MODEL_SERVICE_IP);
        String port = kettleVariables.get(MODEL_SERVICE_PORT);
        String path = queryType ? DATA_PATH : META_PATH + modelLabel;
        builder.setScheme("http").setHost(host).setPort(Integer.parseInt(port)).setPath(path);
        return builder.build();
    }

    /**
     * 根据模型label获取模型基础数据
     *
     * @param modelLabel 模型的label
     * @param ids        对象ID列表
     * @param queryType  查询的数据信息类型
     *                   1. true -> 数据
     *                   2. false -> 元数据
     */
    @SneakyThrows
    public List<Map<String, Object>> getModelData(String modelLabel, List<Long> ids, boolean queryType) {
        QueryCondition qc = new QueryCondition();
        qc.setRootLabel(modelLabel);
        if (CollectionUtils.isNotEmpty(ids)) {
            if (ids.size() == 1) {
                qc.setRootID(ids.get(0));
            } else {
                FlatQueryConditionDTO rootCondition = new FlatQueryConditionDTO();
                rootCondition.setFilter(new ConditionBlockCompose(new ArrayList<>(
                        Collections.singletonList(new ConditionBlock(ColumnDef.ID, ConditionBlock.OPERATOR_IN, ids))))
                );
                qc.setRootCondition(rootCondition);
            }
        }

        Result result = restTemplate.postForObject(buildQueryUri(modelLabel, queryType), qc, Result.class);
        if (result.getCode() != 0) {
            return new ArrayList<>();
        }
        ObjectMapper objectMapper = new ObjectMapper();

        return objectMapper.readValue(
                (String) result.getData(), new TypeReference<List<Map<String, Object>>>() {
                });
    }
}
