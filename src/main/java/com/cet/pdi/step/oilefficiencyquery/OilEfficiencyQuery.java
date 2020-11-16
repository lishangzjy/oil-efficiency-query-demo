/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package com.cet.pdi.step.oilefficiencyquery;

import com.cet.eem.common.constant.TableName;
import com.cet.eem.common.definition.ColumnDef;
import com.cet.pdi.step.oilefficiencyquery.service.ModelQueryService;
import lombok.SneakyThrows;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 业务逻辑实现，负责数据处理，转换和流转。这里面主要由processRow()方法来处理。
 *
 * @author Jinhua
 */
public class OilEfficiencyQuery extends BaseStep implements StepInterface {

    /**
     * for i18n purposes
     */
    private static final Class<?> PKG = OilEfficiencyQueryMeta.class;

    private Database database;

    /**
     * 步骤元数据
     */
    private OilEfficiencyQueryMeta meta;

    /**
     * 模型服务查询功能
     */
    private ModelQueryService modelQueryService;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public OilEfficiencyQuery(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis) {
        super(s, stepDataInterface, c, t, dis);
        try {
            this.modelQueryService = new ModelQueryService();
        } catch (IOException ignored) {

        }
    }

    /**
     * 初始化方法，可以建立数据库链接、获取文件句柄等操作，会被PDI调用。
     *
     * @param smi 元数据
     * @param sdi 数据
     * @return 初始化结果
     */
    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi) {
        // Casting to step-specific implementation classes is safe
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;
        if (!super.init(meta, data)) {
            return false;
        }
        DatabaseMeta databaseMeta = new DatabaseMeta("matterhorn", "PostgreSQL", "JNDI",
                "172.17.6.121", "matterhorn", "5432", "postgres", "y6fqdy");
        database = new Database(this, databaseMeta);

        RowMetaInterface inputRowMeta = getInputRowMeta();
        // 加入基本字段元数据
        List<ValueMetaInterface> objAndTimeMetas = ((OilEfficiencyQueryMeta) smi).getObjectAndTimeFieldMetas();
        objAndTimeMetas.forEach(inputRowMeta::addValueMeta);
        // 能效字段元数据
        List<ValueMetaInterface> effFieldMetas = ((OilEfficiencyQueryMeta) smi).getEffFieldMetas();
        effFieldMetas.forEach(inputRowMeta::addValueMeta);

        // 获取字段条件
        Map<String, Object> fieldConditionMap = ((OilEfficiencyQueryMeta) smi).getFieldConditionMap();
        // 解析，加入到输入行中
        resolveEffData(fieldConditionMap);
        return true;
    }

    /**
     * 根据meta中缓存的字段条件查询出能效时序数据
     * 并且逐行设置进入输入行
     *
     * @param fieldConditionMap 字段查询条件
     */
    @SneakyThrows
    private void resolveEffData(Map<String, Object> fieldConditionMap) {
        // 有序的字段名数组
        String[] fieldNames = getInputRowMeta().getFieldNames();
        // Label条件
        @SuppressWarnings("unchecked")
        List<LabelAndIds> labelAndIds = (List<LabelAndIds>) fieldConditionMap.getOrDefault(ColumnDef.MODEL_LABEL,
                Collections.singleton(new LabelAndIds(TableName.MECHANICAL_MINING_MACHINE))
        );
        // 对象条件放索引为0的位置
        LabelAndIds targetModel = labelAndIds.get(0);
        // 查询模型数据
        List<Map<String, Object>> efficiencyData = modelQueryService.getModelOilEfficiency(targetModel.getModelLabel(),
                new ArrayList<>(targetModel.getId2NameMap().keySet()), Arrays.asList(fieldNames));
        // 匹配字段，将所有的字段加入到inputRows数组中
        Object[] inputRow = null;
        // 每个记录行
        for (Map<String, Object> efficiencyDatum : efficiencyData) {
            inputRow = new Object[fieldNames.length];
            // 每个字段
            for (int i = 0; i < fieldNames.length; i++) {
                inputRow[i] = efficiencyDatum.get(fieldNames[i]);
                // 放入输入行
                putRow(getInputRowMeta(), inputRow);
            }
        }
    }

    /**
     * 读取行的业务逻辑，会被PDI调用，当此方法返回false时，完成行读取。
     *
     * @param smi The steps metadata to work with
     * @param sdi The steps temporary working data to work with (database connections, result sets, caches, temporary
     *            variables, etc.)
     * @return 执行结果
     * @throws KettleException kettle异常
     */
    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi) throws KettleException {

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;

        //写文件操作
        //writeFile();

        // 从输入流中读取一行,通过分割符进行分割，存入object数组中
        Object[] r = getRow();

        // 若读不到下一行，则读写完成，调用setOutputDone(),return false;
        if (r == null) {
            setOutputDone();
            return false;
        }
        if (first) {
            first = false;
            // 如果是第一行则保存数据行元信息到data类中，后续使用
            data.outputRowMeta = getInputRowMeta().clone();
        }

        // safely add the string "Hello World!" at the end of the output row
        // the row array will be resized if necessary
        //Object[] outputRow = RowDataUtil.resizeArray(r, data.outputRowMeta.size());
        //outputRow[data.outputFieldIndex] = "Hello World!";

        // 将行放进输出行流，正确记录输出
        putRow(data.outputRowMeta, r);
        // 错误数据输出使用putError传递数据

        // log progress if it is time to to so
        if (checkFeedback(getLinesRead())) {
            // Some basic logging
            logBasic(BaseMessages.getString(PKG, "OilEfficiencyQuery", getLinesRead()));
        }

        // indicate that processRow() should be called again
        return true;
    }

    /**
     * 析构函数，用来释放资源，会被PDI调用
     *
     * @param smi 元数据
     * @param sdi 数据
     */
    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi) {

        // Casting to step-specific implementation classes is safe
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;

        // Add any step-specific initialization that may be needed here

        // Call superclass dispose()
        super.dispose(meta, data);
    }

    /**
     * 解析返回参数
     *
     * @param rs 查询数据库返回的字节集
     * @return list集合
     */
    private List<Map<String, Object>> getRow(ResultSet rs) {
        List<Map<String, Object>> rows = new ArrayList<>();
        final int mapInitSize = 16;

        Map<String, Object> row;
        try {
            // 通过编译对象执行SQL指令
            if (rs != null) {
                // 获取结果集的元数据
                ResultSetMetaData rsd = rs.getMetaData();
                // 获取当前表的总列数
                int columnCount = rsd.getColumnCount();
                // 遍历结果集
                while (rs.next()) {
                    // 创建存储当前行的集合对象
                    row = new HashMap<>(mapInitSize);
                    // 遍历当前行每一列
                    for (int i = 0; i < columnCount; i++) {
                        // 获取列的编号获取列名
                        String columnName = rsd.getColumnName(i + 1);
                        // 通过列名获取当前遍历列的值
                        Object columnValue = rs.getObject(columnName);
                        // 列名和获取值作为 K 和 V 存入Map集合
                        row.put(columnName, columnValue);
                    }
                    // 把每次遍历列的Map集合存储到List集合中
                    rows.add(row);
                }
            }
        } catch (Exception ignored) {

        }
        return rows;
    }

    /**
     * 从数据库中查询出指定的字段数据并存入文件中
     */
    private void writeFile() {
        List<ValueMetaInterface> valueMetas = meta.getEffFieldMetas();
        List<String> str = valueMetas.stream().filter(e -> e.getName() != null && e.getName().trim().length() > 0).map(ValueMetaInterface::getName).collect(Collectors.toList());
        StringBuilder sql = new StringBuilder();
        sql.append(" select ");
        for (String s : str) {
            sql.append(s);
            sql.append(",");
        }
        sql.deleteCharAt(sql.length() - 1);
        sql.append(" from mechanicalminingmachine ");
        List<Map<String, Object>> rows = new ArrayList<>();
        try {
            database.connect();
            PreparedStatement ps = database.prepareSQL(sql.toString());
            ResultSet set = ps.executeQuery();
            rows = getRow(set);
        } catch (Exception ignored) {

        }
        try {
            FileWriter outFile = new FileWriter("F:/sqlFile.txt");
            BufferedWriter writer = new BufferedWriter(outFile);
            StringBuilder string;
            for (Map<String, Object> map : rows) {
                string = new StringBuilder();
                for (String key : map.keySet()) {
                    if (map.get(key) != null) {
                        string.append(map.get(key));
                    }
                    string.append(";");
                }
                string.deleteCharAt(string.length() - 1);
                writer.write(String.valueOf(string));
                writer.newLine();
            }
            writer.flush();
            writer.close();
        } catch (Exception ignored) {

        }
    }
}
