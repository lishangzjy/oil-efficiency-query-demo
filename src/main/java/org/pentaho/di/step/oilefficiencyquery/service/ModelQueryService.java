package org.pentaho.di.step.oilefficiencyquery.service;

import com.cet.eem.common.definition.ColumnDef;
import com.cet.eem.common.model.ConditionBlock;
import com.cet.eem.common.model.ConditionBlockCompose;
import com.cet.eem.common.model.FlatQueryConditionDTO;
import com.cet.eem.common.model.QueryCondition;
import org.pentaho.di.step.oilefficiencyquery.constant.StaticMaps;
import org.pentaho.di.step.oilefficiencyquery.dao.ModelQueryDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * 模型服务查询业务
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/13 9:26
 */
@Data
public class ModelQueryService {

    /**
     * 模型服务查询对象
     */
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
        if (StringUtils.isEmpty(modelLabel)) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> modelData = modelQueryDao.getModelData(
                buildQueryCondition(modelLabel, ids, null)
        );
        List<String> idAndNames = new ArrayList<>();
        modelData.forEach(rowMap -> {
            Object idOpt = rowMap.get(ColumnDef.ID);
            Object nameOpt = rowMap.get(ColumnDef.NAME);
            if (idOpt != null && nameOpt != null) {
                idAndNames.add(idOpt + "_" + nameOpt);
            }
        });

        return idAndNames;
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
            return qc;
        }
        qc.setRootLabel(modelLabel);
        if (CollectionUtils.isNotEmpty(ids)) {
            rootCondition = new FlatQueryConditionDTO();
            ConditionBlock idExpr;
            if (ids.size() == 1) {
                idExpr = new ConditionBlock(ColumnDef.ID, ConditionBlock.OPERATOR_EQ, ids.get(0));
            } else {
                idExpr = new ConditionBlock(ColumnDef.ID, ConditionBlock.OPERATOR_IN, ids);
            }
            rootCondition.setFilter(new ConditionBlockCompose(new ArrayList<>(Collections.singletonList(idExpr))));
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
            properties = objectMapper.readValue(objectMapper.writeValueAsString(modelMeta.get(propertyKey)),
                    new TypeReference<List<Map<String, Object>>>() {
            });
            return properties;
        } catch (JsonProcessingException jex) {
            return null;
        }
    }

    /**
     * 获取指定油气田模型，指定id条件的能效数据
     * 此处需注意入参label和油气田能效label有一个对应关系
     * 所有的label必须在{@link StaticMaps#getModel2EffModelNameMap()} 中有定义
     *
     * @param modelLabel 油气田模型
     * @param ids        油气田模型id范围
     * @return 油气田的能效模型数据列表
     */
    public List<Map<String, Object>> getModelOilEfficiency(String modelLabel, List<Long> ids, List<String> props) {

        final String objectLabelKey = "objectLabel".toLowerCase();
        final String objectIdKey = "objectId".toLowerCase();
        final String oilFieldKpiLabel = "oilFieldKpi".toLowerCase();
        final String idSuffix = "_id";

        // 找出要查询的能效模型
        String effModelLabel = StaticMaps.getModel2EffModelNameMap().get(modelLabel);
        if (StringUtils.isEmpty(effModelLabel)) {
            return new ArrayList<>();
        }
        // 构建查询条件
        QueryCondition qc = buildQueryCondition(effModelLabel, ids, props);
        if (CollectionUtils.isNotEmpty(ids)) {
            // 需要替换label和id字段查询条件
            List<ConditionBlock> exps = qc.getRootCondition().getFilter().getExpressions();
            Optional<ConditionBlock> idOpt = exps.stream().filter(exp -> ColumnDef.ID.equals(exp.getProp())).findFirst();
            // 移除原有的id条件
            exps.remove(idOpt.orElse(null));
            if (idOpt.isPresent()) {
                // 拿到id条件，需要做修改，并设置到原来的条件中
                ConditionBlock idCondition = idOpt.get();
                List<ConditionBlock> labelAndIdExps = new ArrayList<>();
                // oilFieldKpi表，对象模型是两个字段条件
                if (oilFieldKpiLabel.equals(effModelLabel)) {
                    labelAndIdExps.add(new ConditionBlock(objectLabelKey, ConditionBlock.OPERATOR_EQ, modelLabel));
                    idCondition.setProp(objectIdKey);
                } else {
                    idCondition.setProp(modelLabel + idSuffix);
                }
                labelAndIdExps.add(idCondition);
                exps.addAll(labelAndIdExps);
            }
        }
        return modelQueryDao.getModelData(qc);
    }
}
