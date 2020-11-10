package com.cet.pdi.step.oilefficiencyquery.constant;

import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型与PDI数据类型对应关系
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/10 13:42
 */
public class Model2PdiDataTypeMap {
    private static final Map<String, Integer> m2Pdi;

    static {
        m2Pdi = new HashMap<>();
        m2Pdi.put("int4", ValueMetaInterface.TYPE_INTEGER);
        m2Pdi.put("int8", ValueMetaInterface.TYPE_INTEGER);
        m2Pdi.put("float", ValueMetaInterface.TYPE_NUMBER);
        m2Pdi.put("string", ValueMetaInterface.TYPE_STRING);
        m2Pdi.put("boolean", ValueMetaInterface.TYPE_BOOLEAN);
    }

    public static Map<String, Integer> getM2Pdi() {
        return m2Pdi;
    }
}
