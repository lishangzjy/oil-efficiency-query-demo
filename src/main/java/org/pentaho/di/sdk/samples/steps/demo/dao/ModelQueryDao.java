package org.pentaho.di.sdk.samples.steps.demo.dao;

import com.cet.eem.common.definition.ColumnDef;
import com.cet.eem.common.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.http.client.utils.URIBuilder;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.sdk.samples.steps.demo.constant.ModelQueryMethodEnum;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;

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
    private URI buildQueryUri(String modelLabel, ModelQueryMethodEnum queryType) {
        URIBuilder builder = new URIBuilder();
        String host = kettleVariables.get(MODEL_SERVICE_IP);
        String port = kettleVariables.get(MODEL_SERVICE_PORT);
        String path;
        String pathPrefix = queryType.getPath();
        switch (queryType) {
            case MODEL_META: {
                path = pathPrefix + modelLabel;
                break;
            }
            case MODEL_DATA: {
                path = pathPrefix;
                break;
            }
            default: {
                return null;
            }
        }
        builder.setScheme("http").setHost(host).setPort(Integer.parseInt(port)).setPath(path);
        return builder.build();
    }

    /**
     * 构建模型数据查询条件
     *
     * @param modelLabel 模型label
     * @param ids        模型ID列表
     * @param props      需要查询的属性
     * @return 构建完成请求体对象
     */
    private QueryCondition buildQueryCondition(String modelLabel, List<Long> ids, List<String> props) {
        QueryCondition qc = new QueryCondition();
        FlatQueryConditionDTO rootCondition = null;
        if (StringUtils.isEmpty(modelLabel)) {
            return null;
        }
        qc.setRootLabel(modelLabel);
        if (CollectionUtils.isNotEmpty(ids)) {
            if (ids.size() == 1) {
                qc.setRootID(ids.get(0));
            } else {
                rootCondition = new FlatQueryConditionDTO();
                rootCondition.setFilter(new ConditionBlockCompose(new ArrayList<>(
                        Collections.singletonList(new ConditionBlock(ColumnDef.ID, ConditionBlock.OPERATOR_IN, ids))))
                );
            }
        }
        if (CollectionUtils.isNotEmpty(props)) {
            if (rootCondition == null) {
                rootCondition = new FlatQueryConditionDTO();
            }
            rootCondition.setProps(props);
        }
        qc.setRootCondition(rootCondition);
        return qc;
    }

    /**
     * 查询模型数据
     * 1. 构建URI
     * 2. 构建查询条件
     * 3. 发POST请求
     * 4. 解析结果
     *
     * @param modelLabel 模型label
     * @param ids        模型id列表
     * @param props      要展示的属性列
     * @return 返回模型数据集合
     */
    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> getModelData(String modelLabel, List<Long> ids, List<String> props) {

        URI uri = buildQueryUri(modelLabel, ModelQueryMethodEnum.MODEL_DATA);
        QueryCondition qc = buildQueryCondition(modelLabel, ids, props);
        Result result = restTemplate.postForObject(uri, qc, Result.class);
        if (result == null || result.getCode() != 0) {
            return new ArrayList<>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue((String) result.getData(), new TypeReference<List<Map<String, Object>>>() {
        });
    }

    /**
     * 查询模型元数据
     * 1. 构建URI
     * 2. 发起GET请求
     * 3. 解析查询结果
     *
     * @param modelLabel 模型label
     * @return 该模型的元数据对象
     */
    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public Map<String, Object> getModelMeta(String modelLabel) {
        URI uri = buildQueryUri(modelLabel, ModelQueryMethodEnum.MODEL_META);
        Result result = restTemplate.getForObject(uri, Result.class);
        if (result == null || result.getCode() != 0) {
            return new HashMap<>(2);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue((String) result.getData(), new TypeReference<Map<String, Object>>() {
        });
    }

}
