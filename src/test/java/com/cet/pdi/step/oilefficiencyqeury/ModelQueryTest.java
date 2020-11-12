package com.cet.pdi.step.oilefficiencyqeury;

import com.cet.pdi.step.oilefficiencyquery.LabelAndIds;
import com.cet.pdi.step.oilefficiencyquery.dao.ModelQueryDao;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.Test;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.KettleVariablesList;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaString;
import org.pentaho.di.laf.BasePropertyHandler;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * 接口查询模型服务测试
 *
 * @author Jinhua
 * @version 1.1     加入序列化测试
 * @date 2020/11/10 15:00
 */
public class ModelQueryTest {

    private final ModelQueryDao modelQueryDao = new ModelQueryDao("172.16.6.28", 8085);

    //@BeforeClass
    public static void setUpBeforeClass() throws KettleException {
        KettleEnvironment.init(true);
    }

    @Test
    public void testBuildUri() {
        final String modelLabel = "building";
        Map<String, Object> modelMeta = modelQueryDao.getModelMeta(modelLabel);

        for (Map.Entry<String, Object> entry : modelMeta.entrySet()) {
            System.out.println("key = " + entry.getKey() + "----====----value = " + entry.getValue());
        }

    }

    @Test
    public void testProperty() {
        // kettle环境变量，目前只能加载kettle默认的，不能获取到kettle.properties文件中的。
        KettleVariablesList kettleVariables = KettleVariablesList.getInstance();
        Map<String, String> defaultValueMap = kettleVariables.getDefaultValueMap();
    }

    @SneakyThrows
    @Test
    public void testSerialization() {
        Map<ValueMetaInterface, List<LabelAndIds>> fieldConditionMap = new HashMap<>();

        LabelAndIds modelLabel = new LabelAndIds();
        modelLabel.setModelLabel("platform");

        Map<Long, String> id2NameMap = new HashMap<>(16);
        id2NameMap.put(1L, "1#平台");
        id2NameMap.put(2L, "2#平台");

        modelLabel.setId2NameMap(id2NameMap);
        ValueMetaInterface v1 = new ValueMetaString("modelLabel");

        List<LabelAndIds> labelsAndIds = new ArrayList<>(Collections.singletonList(modelLabel));
        fieldConditionMap.put(v1, labelsAndIds);

        ObjectMapper objectMapper = new ObjectMapper();
        String fCondition = objectMapper.writeValueAsString(fieldConditionMap);
        System.out.println(fCondition);

        // 反序列化失败，所以此处需要用String类型来存储
        Map<ValueMetaInterface, List<LabelAndIds>> valueMetaInterfaceListMap = objectMapper.readValue(
                fCondition, new TypeReference<Map<ValueMetaInterface, List<LabelAndIds>>>() {
                });

        for (Map.Entry<ValueMetaInterface, List<LabelAndIds>> entry : valueMetaInterfaceListMap.entrySet()) {
            System.out.println("字段 = " + entry.getKey());
            List<LabelAndIds> values = entry.getValue();
            for (LabelAndIds value : values) {
                System.out.println("modelLabel = " + value.getModelLabel());
                for (Map.Entry<Long, String> lsEntry : value.getId2NameMap().entrySet()) {
                    System.out.println("id = " + lsEntry.getKey());
                    System.out.println("name = " + lsEntry.getValue());
                    System.out.println("-----------------------------");
                }
                System.out.println("=========================");
            }
            System.out.println("-=-=-=-=-=-=-=-=");
        }
    }

    /**
     * 测试资源绑定文件
     * @throws IOException IO读取异常
     */
    @Test
    public void testProperties() throws IOException {
        String kettlePropertiesFilename = Const.getKettlePropertiesFilename();
        InputStream is = new FileInputStream(kettlePropertiesFilename);
        ResourceBundle bundle = new PropertyResourceBundle(is);
        System.out.println(bundle.getString("model_service_ip"));
    }

}
