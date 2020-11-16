package org.pentaho.di.step.oilefficiencyquery.dao;

import com.cet.eem.common.model.QueryCondition;
import com.cet.eem.common.model.Result;
import org.pentaho.di.step.oilefficiencyquery.constant.ModelQueryMethodEnum;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.pentaho.di.core.Const;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.*;

/**
 * 模型查询持久化对象
 * 1. 如果通过空参构造，则本类完成模型服务ip和port的获取；
 * 2. 否则将构造参数中的ip和port作为模型服务ip和port。
 * 3. 不提供Setter。
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/5 18:49
 */
public class ModelQueryDao {

    /**
     * rest接口请求工具
     */
    private static RestTemplate restTemplate;

    /**
     * 模型服务IP的键
     */
    private static final String MODEL_SERVICE_IP_KEY = "model_service_ip";

    /**
     * 模型服务port的键
     */
    private static final String MODEL_SERVICE_PORT_KEY = "model_service_port";

    /**
     * kettle用户配置属性文件名
     */
    private static final String PROPERTIES_FILE = "kettle.properties";

    /**
     * 存配置中模型服务IP的值
     */
    private final String modelServiceIp;

    /**
     * 配置中模型服务的PORT的值
     */
    private final Integer modelServicePort;

    /**
     * 资源绑定 kettle.properties
     */
    private ResourceBundle bundle;

    /**
     * 若不传入模型服务IP和端口，则读取资源绑定文件中的IP和端口
     */
    public ModelQueryDao() throws IOException {
        // 1. 初始化rest接口请求工具
        restTemplate = new RestTemplate();
        // 2. 初始化资源绑定文件对象
        InputStream stream = new FileInputStream(Const.getKettleDirectory() + Const.FILE_SEPARATOR + PROPERTIES_FILE);
        this.bundle = new PropertyResourceBundle(stream);
        // 3. 设置ip和port
        this.modelServiceIp = bundle.getString(MODEL_SERVICE_IP_KEY);
        this.modelServicePort = Integer.parseInt(bundle.getString(MODEL_SERVICE_PORT_KEY));
    }

    /**
     * 如果传入模型服务的IP和port，则直接按照传入的查询
     *
     * @param modelServiceIp   外部传入模型服务IP
     * @param modelServicePort 外部传入模型服务端口
     */
    public ModelQueryDao(String modelServiceIp, Integer modelServicePort) {
        restTemplate = new RestTemplate();
        this.modelServiceIp = modelServiceIp;
        this.modelServicePort = modelServicePort;
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
        builder.setScheme("http").setHost(modelServiceIp).setPort(modelServicePort).setPath(path);
        return builder.build();
    }

    /**
     * 查询模型数据
     * 1. 构建URI
     * 2. 构建查询条件
     * 3. 发POST请求
     * 4. 解析结果
     *
     * @param qc 查询条件
     * @return 返回模型数据集合
     */
    @SneakyThrows
    @SuppressWarnings("rawtypes")
    public List<Map<String, Object>> getModelData(QueryCondition qc) {
        if (StringUtils.isEmpty(qc.getRootLabel())) {
            return new ArrayList<>();
        }

        URI uri = buildQueryUri(qc.getRootLabel(), ModelQueryMethodEnum.MODEL_DATA);
        Result result = restTemplate.postForObject(uri, qc, Result.class);
        if (result == null || result.getCode() != 0) {
            return new ArrayList<>();
        }

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(objectMapper.writeValueAsString(result.getData()),
                new TypeReference<List<Map<String, Object>>>() {
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
        String dataStr = objectMapper.writeValueAsString(result.getData());
        return objectMapper.readValue(dataStr, new TypeReference<Map<String, Object>>() {
        });
    }
}
