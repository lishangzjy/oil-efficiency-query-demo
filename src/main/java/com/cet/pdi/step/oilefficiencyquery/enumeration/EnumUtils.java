package com.cet.pdi.step.oilefficiencyquery.enumeration;

/**
 * 工具类，提供获取SQL类型与kettle类型的方法
 *
 * @author lichunsheng
 * @date 2020/11/10
 */
@SuppressWarnings("rawtypes")
public class EnumUtils {

    /**
     * 通过SQL类型获取类型枚举对象
     *
     * @param type      SQL类型
     * @param enumClass 类型对应关系类的类对象
     * @param <T>       类型对应关系枚举对象
     * @return 类型对应关系枚举对象
     */
    public static <T extends TypeEnumCode> T getBySqlType(String type, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            //利用code进行循环比较，获取对应的枚举
            String sType = each.getSqlType();
            if (type.equals(sType)) {
                return each;
            }
        }
        return (T) TypeEnumCode.TypeOther;
    }

    /**
     * 通过枚举对象唯一标识码，获取类型枚举对象
     *
     * @param code      类型对应关系标识码
     * @param enumClass 类型对应关系类的类对象
     * @param <T>       类型对应关系枚举对象
     * @return 类型对应关系枚举对象
     */
    public static <T extends DataEnumCode> T getByCode(int code, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            //利用code进行循环比较，获取对应的枚举
            int eachCode = Integer.parseInt(String.valueOf(each.getCode()));
            if (code == eachCode) {
                return each;
            }
        }
        return null;
    }

    /**
     * 通过Kettle类型，获取类型对应关系枚举对象
     *
     * @param type      Kettle类型字符串
     * @param enumClass 类型对应关系类的类对象
     * @param <T>       类型对应关系枚举对象
     * @return 类型对应关系枚举对象
     */
    public static <T extends TypeEnumCode> T getByKettleType(String type, Class<T> enumClass) {
        for (T each : enumClass.getEnumConstants()) {
            //利用code进行循环比较，获取对应的枚举
            String kType = each.getKettleType();
            if (type.equals(kType)) {
                return each;
            }
        }
        return (T) TypeEnumCode.TypeOther;
    }
}
