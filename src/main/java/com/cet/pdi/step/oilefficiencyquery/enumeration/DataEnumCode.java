package com.cet.pdi.step.oilefficiencyquery.enumeration;

/**
 * kettle与Java数据类型对应关系对象的唯一识别码
 *
 * @author lichunsheng
 * @date 2020/11/10
 */
public interface DataEnumCode<T> {

    /**
     * kettle与Java数据类型对应关系对象的唯一识别码
     *
     * @return 唯一识别码
     */
    T getCode();
}
