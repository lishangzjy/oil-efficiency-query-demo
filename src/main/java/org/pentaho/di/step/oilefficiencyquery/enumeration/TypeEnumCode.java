package org.pentaho.di.step.oilefficiencyquery.enumeration;

import lombok.Getter;
import org.pentaho.di.core.row.ValueMetaInterface;

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
    TypeString("string", "String", ValueMetaInterface.TYPE_STRING),
    TypeInt4("int4", "Integer", ValueMetaInterface.TYPE_INTEGER),
    TypeInt8("int8", "Integer", ValueMetaInterface.TYPE_INTEGER),
    TypeFloat("float", "Number", ValueMetaInterface.TYPE_NUMBER),
    TypeBoolean("boolean", "Boolean", ValueMetaInterface.TYPE_BOOLEAN),
    TypeJsonb("jsonb", "String", ValueMetaInterface.TYPE_STRING),
    TypeOther("Integer", ValueMetaInterface.TYPE_INTEGER);

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
