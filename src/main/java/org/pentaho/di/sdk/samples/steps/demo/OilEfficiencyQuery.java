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

package org.pentaho.di.sdk.samples.steps.demo;

import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowDataUtil;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;

import java.util.List;
import java.util.Map;

/**
 * 业务逻辑实现，负责数据处理，转换和流转。这里面主要由processRow()方法来处理。
 * @author Jinhua
 */
public class OilEfficiencyQuery extends BaseStep implements StepInterface {

    /**
     * for i18n purposes
     */
    private static final Class<?> PKG = OilEfficiencyQueryMeta.class;

    /**
     * The constructor should simply pass on its arguments to the parent class.
     *
     * @param s                 step description
     * @param stepDataInterface step data class
     * @param c                 step copy
     * @param t                 transformation description
     * @param dis               transformation executing
     */
    public OilEfficiencyQuery(StepMeta s, StepDataInterface stepDataInterface, int c, TransMeta t, Trans dis ) {
        super( s, stepDataInterface, c, t, dis );
    }

    /**
     * 初始化方法，可以建立数据库链接、获取文件句柄等操作，会被PDI调用。
     * @param smi 元数据
     * @param sdi 数据
     * @return 初始化结果
     */
    @Override
    public boolean init(StepMetaInterface smi, StepDataInterface sdi ) {
        // Casting to step-specific implementation classes is safe
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;
        if (!super.init(meta, data)) {
            return false;
        }

        return true;
        // Add any step-specific initialization that may be needed here
    }

    /**
     * 读取行的业务逻辑，会被PDI调用，当此方法返回false时，完成行读取。
     * @param smi
     *          The steps metadata to work with
     * @param sdi
     *          The steps temporary working data to work with (database connections, result sets, caches, temporary
     *          variables, etc.)
     * @return 执行结果
     * @throws KettleException kettle异常
     */
    @Override
    public boolean processRow(StepMetaInterface smi, StepDataInterface sdi ) throws KettleException {

        // safely cast the step settings (meta) and runtime info (data) to specific implementations
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;

        // 从输入流中读取一行,通过分割符进行分割，存入object数组中
        Object[] r = getRow();

        // 若读不到下一行，则读写完成，调用setOutputDone(),return false;
        if ( r == null ) {
            setOutputDone();
            return false;
        }

        // the "first" flag is inherited from the base step implementation
        // it is used to guard some processing tasks, like figuring out field indexes
        // in the row structure that only need to be done once
        if ( first ) {
            first = false;
            // 如果是第一行则保存数据行元信息到data类中，后续使用
            data.outputRowMeta = getInputRowMeta().clone();
//            // use meta.getFields() to change it, so it reflects the output row structure
//            meta.getFields( data.outputRowMeta, getStepname(), null, null, this, null, null );
//
//            // Locate the row index for this step's field
//            // If less than 0, the field was not found.
//            data.outputFieldIndex = data.outputRowMeta.indexOfValue( meta.getOutputField() );
//            if ( data.outputFieldIndex < 0 ) {
//                log.logError( BaseMessages.getString( PKG, "DemoStep.Error.NoOutputField" ) );
//                setErrors( 1L );
//                setOutputDone();
//                return false;
//            }
        }

        // safely add the string "Hello World!" at the end of the output row
        // the row array will be resized if necessary
        Object[] outputRow = RowDataUtil.resizeArray( r, data.outputRowMeta.size() );
        outputRow[data.outputFieldIndex] = "Hello World!";

//        // 字符替换的业务逻辑
//        for (int i = 0; i < r.length && r[i] != null; i++) {
//            String str = data.outputRowMeta.getString(r, i);
//            if (changeColList.contains((i + 1) + "")) {
//                Iterator iter = changeStr.entrySet().iterator();
//                while (iter.hasNext()) {
//                    Map.Entry entry = (Map.Entry) iter.next();
//                    Object before = entry.getKey();
//                    Object after = entry.getValue();
//                    str = str.replace(String.valueOf(before), String.valueOf(after));
//                }
//                r[i] = str.getBytes();
//            }
//        }

        // 将行放进输出行流，正确记录输出
        putRow( data.outputRowMeta, outputRow );
        // 错误数据输出使用putError传递数据

        // log progress if it is time to to so
        if ( checkFeedback( getLinesRead() ) ) {
            // Some basic logging
            logBasic( BaseMessages.getString( PKG, "DemoStep.Linenr", getLinesRead() ) );
        }

        // indicate that processRow() should be called again
        return true;
    }

    /**
     * 析构函数，用来释放资源，会被PDI调用
     * @param smi 元数据
     * @param sdi 数据
     */
    @Override
    public void dispose(StepMetaInterface smi, StepDataInterface sdi ) {

        // Casting to step-specific implementation classes is safe
        OilEfficiencyQueryMeta meta = (OilEfficiencyQueryMeta) smi;
        OilEfficiencyQueryData data = (OilEfficiencyQueryData) sdi;

        // Add any step-specific initialization that may be needed here

        // Call superclass dispose()
        super.dispose( meta, data );
    }
}
