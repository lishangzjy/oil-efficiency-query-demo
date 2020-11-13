package com.cet.pdi.step.oilefficiencyquery.service;

import com.cet.eem.common.definition.ColumnDef;
import com.cet.pdi.step.oilefficiencyquery.dao.ModelQueryDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 模型服务查询业务
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/13 9:26
 */
@Data
@Service(value = "modelQueryService")
public class ModelQueryService {

    /**
     * 模型服务查询对象
     */
    @Resource(name = "modelQueryDao")
    private ModelQueryDao modelQueryDao;

    public ModelQueryService() throws IOException {
        modelQueryDao = new ModelQueryDao();
    }

    public ModelQueryService(String modelServiceIp, Integer modelServicePort) {
        modelQueryDao = new ModelQueryDao(modelServiceIp, modelServicePort);
    }

    /**
     * 获取具体的层级对象
     *
     * @param ids 层级对象ID列表，可以为空
     * @return 层级对象格式为【id_name】的数组
     */
    public List<String> getLevelObject(String modelLabel, List<Long> ids) {

        List<Map<String, Object>> modelData = modelQueryDao.getModelData(modelLabel, ids, null);
        List<String> idAndNames = new ArrayList<>();
        modelData.forEach(rowMap -> {
            Object idOpt = rowMap.get(ColumnDef.ID);
            Object nameOpt = rowMap.get(ColumnDef.NAME);
            if (idOpt != null && nameOpt != null) {
                idAndNames.add(idOpt + "$_$" + nameOpt);
            }
        });

        return idAndNames;
    }

    /**
     * 查询模型的属性
     * 如果为空或解析异常，返回一个null
     *
     * @param modelLabel 模型label
     * @return 元数据属性列集合
     */
    public List<Map<String, Object>> getModelProperties(String modelLabel) {
        Map<String, Object> modelMeta = modelQueryDao.getModelMeta(modelLabel);
        if (modelMeta == null) {
            return new ArrayList<>();
        }
        final String propertyKey = "propertyList";
        ObjectMapper objectMapper = new ObjectMapper();
        List<Map<String, Object>> properties;
        try {
            properties = objectMapper.readValue((String) modelMeta.get(propertyKey), new TypeReference<List<Map<String, Object>>>() {
            });
            return properties;
        } catch (JsonProcessingException jex) {
            return null;
        }
    }

    /**
     * 获取指定油气田模型，指定条件的能效数据
     * 此处需注意入参label和油气田能效label有一个对应关系
     *
     * @param modelLabel 油气田模型
     * @param ids 油气田模型id范围
     * @return 油气田的能效模型数据列表
     */
    public List<Map<String, Object>> getModelOilEfficiency(String modelLabel, List<Long> ids) {



        return null;
    }

}
