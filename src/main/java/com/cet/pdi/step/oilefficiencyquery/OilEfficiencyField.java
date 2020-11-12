package com.cet.pdi.step.oilefficiencyquery;

import lombok.Data;

/**
 * 油气田能效字段
 *
 * @author lichunsheng
 */
@Data
public class OilEfficiencyField {

    /**
     * 能效字段名称
     */
    private String efficiencyName;

    /**
     * 字段Kettle类型
     */
    private int fieldKettleType;

}
