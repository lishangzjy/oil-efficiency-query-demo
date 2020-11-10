package com.cet.pdi.step.oilefficiencyquery;


import java.util.List;


/**
 * 存储对象label和id查询条件的对象
 *
 * @author Li Jinhua
 * @author Li Chunsheng
 */
public class LabelAndIds {

    /**
     * 模型对象label，比如 platform
     */
    private String modelLabel;

    /**
     * 模型对象id列表
     */
    private List<Long> ids;
}
