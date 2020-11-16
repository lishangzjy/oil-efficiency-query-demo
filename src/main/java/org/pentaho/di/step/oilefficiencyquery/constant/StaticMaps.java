package org.pentaho.di.step.oilefficiencyquery.constant;

import com.cet.eem.common.constant.TableName;
import org.pentaho.di.core.row.ValueMetaInterface;

import java.util.HashMap;
import java.util.Map;

/**
 * 静态对应关系
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/10 13:42
 */
public class StaticMaps {

    /**
     * 模型与PDI数据类型对应关系
     */
    private static final Map<String, Integer> M2_PDI_DATA_TYPE_MAP;

    /**
     * 油气田模型对象与油气田能效模型对象名的映射
     */
    private static final Map<String, String> MODEL2_EFF_MODEL_NAME_MAP;

    static {
        final int mapSize = 16;
        M2_PDI_DATA_TYPE_MAP = new HashMap<>(mapSize);
        M2_PDI_DATA_TYPE_MAP.put("int4", ValueMetaInterface.TYPE_INTEGER);
        M2_PDI_DATA_TYPE_MAP.put("int8", ValueMetaInterface.TYPE_INTEGER);
        M2_PDI_DATA_TYPE_MAP.put("float", ValueMetaInterface.TYPE_NUMBER);
        M2_PDI_DATA_TYPE_MAP.put("string", ValueMetaInterface.TYPE_STRING);
        M2_PDI_DATA_TYPE_MAP.put("boolean", ValueMetaInterface.TYPE_BOOLEAN);

        MODEL2_EFF_MODEL_NAME_MAP = new HashMap<>(mapSize);
        final String aggDataSuffix = "aggrData".toLowerCase();
        final String kpiSuffix = "kpi";
        // 底层设备聚合数据
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.MECHANICAL_MINING_MACHINE, TableName.MECHANICAL_MINING_MACHINE + aggDataSuffix);
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.PUMP, TableName.PUMP + aggDataSuffix);
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.HEATING_FURNACE, TableName.HEATING_FURNACE + aggDataSuffix);
        // 管理层级
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.OIL_TRANSFER_STATION, TableName.OIL_TRANSFER_STATION + kpiSuffix);
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.PLATFORM, TableName.PLATFORM + kpiSuffix);
        MODEL2_EFF_MODEL_NAME_MAP.put("oilFactory".toLowerCase(), "oilFactoryKpi".toLowerCase());
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.COMBINED_STATION, TableName.COMBINED_STATION + kpiSuffix);
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.OPERATION_AREA, "oilFieldKpi".toLowerCase());
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.OIL_PRODUCTION_CREW, "oilFieldKpi".toLowerCase());
        MODEL2_EFF_MODEL_NAME_MAP.put(TableName.WATER_INJECTION_STATION, TableName.WATER_INJECTION_STATION + kpiSuffix);
    }

    public static Map<String, Integer> getM2PdiDataTypeMap() {
        return M2_PDI_DATA_TYPE_MAP;
    }

    public static Map<String, String> getModel2EffModelNameMap() {
        return MODEL2_EFF_MODEL_NAME_MAP;
    }
}
