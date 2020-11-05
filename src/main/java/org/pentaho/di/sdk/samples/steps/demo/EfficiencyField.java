package org.pentaho.di.sdk.samples.steps.demo;

import lombok.Data;

/**
 * @author Li Chunsheng
 * 数据库字段实体类
 */

@Data
public class EfficiencyField {
    /**
     * 数据库字段名
     */
    private String field;
    /**
     * 数据库字段类型
     */
    private String type;
    /**
     * 是否选中
     */
    private String selected;
}
