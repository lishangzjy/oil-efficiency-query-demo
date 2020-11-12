package com.cet.pdi.step.oilefficiencyquery;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

/**
 * 存储对象label和id查询条件，以及name展示的对象
 *      唯一性，由{@link LabelAndIds#modelLabel} 确定
 * @author Li Jinhua
 * @author Li Chunsheng
 */
@Data
@EqualsAndHashCode(exclude = {
        "id2NameMap"
})
public class LabelAndIds {

    /**
     * 模型对象label，比如 platform
     */
    private String modelLabel;

    /**
     * 单个模型label对象，从id到name的映射
     * 比如 1 -> 1#平台
     */
    private Map<Long, String> id2NameMap;
}
