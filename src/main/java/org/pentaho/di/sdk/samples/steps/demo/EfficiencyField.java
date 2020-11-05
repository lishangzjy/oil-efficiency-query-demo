package org.pentaho.di.sdk.samples.steps.demo;

import lombok.Data;

/**
 * 数据库字段实体类
 *
 * @author Li Chunsheng
 */
@Data
public class EfficiencyField {
    /**
     * 能效字段名
     */
    private String effField;
    /**
     * 数据库字段类型
     */
    private String type;
    /**
     * 是否选中
     */
    private String selected;
}
