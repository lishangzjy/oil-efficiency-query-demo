package com.cet.pdi.step.oilefficiencyquery.enumeration;

import lombok.Getter;

/**
 * 类型对应关系枚举对象
 * SQL类型与Kettle类型的对应关系
 *
 * @author lichunsheng
 */
@Getter
public enum TypeEnumCode implements DataEnumCode {

    /**
     * SQL数据类型名 <-> Kettle类型名
     */
    TypeString("string", "String", 2),
    TypeInt4("int4", "Integer", 5),
    TypeInt8("int8", "Integer", 5),
    TypeFloat("float", "Number", 1),
    TypeBoolean("boolean", "Boolean", 4),
    TypeJsonb("jsonb", "String", 2),
    TypeOther("Integer", 5);

    /**
     * 类型对应关系唯一标识码
     */
    private final int code;

    /**
     * SQL类型
     */
    private String sqlType;

    /**
     * Kettle类型
     */
    private final String kettleType;

    TypeEnumCode(String sqlType, String kettleType, int code) {
        this.code = code;
        this.sqlType = sqlType;
        this.kettleType = kettleType;
    }

    TypeEnumCode(String kettleType, int code) {
        this.code = code;
        this.kettleType = kettleType;
    }

    @Override
    public Object getCode() {
        return code;
    }
}
