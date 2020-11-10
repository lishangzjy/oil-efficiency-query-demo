package org.pentaho.di.sdk.samples.steps.demo.constant;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

/**
 * 模型服务查询方式枚举类
 *
 * @author Jinhua
 * @version 1.0
 * @date 2020/11/10 9:03
 */
@Getter
public enum ModelQueryMethodEnum {

    /**
     * 查询模型元数据
     */
    MODEL_META(1, "查询模型元数据", "/model-meta/v1/models/label/"),

    /**
     * 模型数据
     */
    MODEL_DATA(2, "查询模型数据", "/model/v1/query");


    /**
     * 查询方式枚举值
     */
    private final int method;

    /**
     * 查询方式描述
     */
    private final String description;

    /**
     * 查询接口路径
     */
    private final String path;

    ModelQueryMethodEnum(int method, String description, String path) {
        this.method = method;
        this.description = description;
        this.path = path;
    }

    /**
     * 通过枚举值找到枚举对象
     * 若不存在，返回空
     *
     * @param method 枚举值
     * @return 查询枚举对象
     */
    public static ModelQueryMethodEnum ofInt(int method) {

        Optional<ModelQueryMethodEnum> queryMethodOpt = Arrays.stream(ModelQueryMethodEnum.values())
                .filter(m -> m.getMethod() == method).findFirst();
        return queryMethodOpt.orElse(null);
    }
}
