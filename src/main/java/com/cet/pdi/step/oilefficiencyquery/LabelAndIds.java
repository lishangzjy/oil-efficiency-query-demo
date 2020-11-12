package com.cet.pdi.step.oilefficiencyquery;


import com.fasterxml.jackson.databind.ObjectMapper;
import groovy.transform.EqualsAndHashCode;
import lombok.Data;
import lombok.SneakyThrows;

import java.util.Map;


/**
 * 存储对象label和id查询条件的对象
 * 唯一性由{@link LabelAndIds#modelLabel} 确定
 *
 * @author Li Jinhua
 * @author Li Chunsheng
 */
@Data
@EqualsAndHashCode(excludes = {
        "id2NameMap"
})
public class LabelAndIds {

    /**
     * 模型对象label，比如 platform
     */
    private String modelLabel;

    /**
     * 模型对象id -> name 映射
     */
    private Map<Long, String> id2NameMap;

    @SneakyThrows
    @Override
    public String toString() {
        return new ObjectMapper().writeValueAsString(this);
    }
}
