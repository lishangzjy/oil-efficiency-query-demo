package com.cet.pdi.step.oilefficiencyqeury;

import com.cet.pdi.step.oilefficiencyquery.dao.ModelQueryDao;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pentaho.di.core.KettleEnvironment;
import org.pentaho.di.core.exception.KettleException;

import java.util.Map;

/**
 * 接口查询模型服务测试
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/10 15:00
 */
public class ModelQueryTest {

    private final ModelQueryDao modelQueryDao = new ModelQueryDao("172.16.6.28", 8085);

    @BeforeClass
    public static void setUpBeforeClass() throws KettleException {
        KettleEnvironment.init( true );
    }

    @Test
    public void testBuildUri() {
        final String modelLabel = "building";
        Map<String, Object> modelMeta = modelQueryDao.getModelMeta(modelLabel);

        for (Map.Entry<String, Object> entry : modelMeta.entrySet()) {
            System.out.println("key = " + entry.getKey() + "value = " + entry.getValue());
        }

    }

}
