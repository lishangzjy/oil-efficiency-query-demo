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

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.CheckResult;
import org.pentaho.di.core.CheckResultInterface;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleStepException;
import org.pentaho.di.core.exception.KettleValueException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.injection.Injection;
import org.pentaho.di.core.injection.InjectionSupported;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.*;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.List;
import java.util.Map;

/**
 * 插件元，可以将插件的变量保存在这个类，元数据的处理，
 * 加载xml，校验，主要是对一个步骤的定义的基本数据。
 * @author Jinhua
 * @author Li Chunsheng
 */
@Step(
        id = "DemoStep",
        name = "DemoStep.Name",
        description = "DemoStep.TooltipDesc",
        image = "com.cet.pdi.step.oilefficiencyquery/resources/demo.svg",
        categoryDescription = "i18n:org.pentaho.di.trans.step:BaseStep.Category.Transform",
        i18nPackageName = "org.pentaho.di.sdk.samples.steps.demo",
        documentationUrl = "DemoStep.DocumentationURL",
        casesUrl = "DemoStep.CasesURL",
        forumUrl = "DemoStep.ForumURL"
)
@InjectionSupported( localizationPrefix = "OilEfficiencyQueryMeta.Injection." )
@Data
public class OilEfficiencyQueryMeta extends BaseStepMeta implements StepMetaInterface {

    /**
     *  The PKG member is used when looking up internationalized strings.
     *  The properties file with localized keys is expected to reside in
     *  {the package of the class specified}/messages/messages_{locale}.properties
     *  for i18n purposes
     */
    private static final Class<?> PKG = OilEfficiencyQueryMeta.class;

    /**
     * Stores the name of the field added to the row-stream.
     */
    @Injection( name = "OUTPUT_FIELD" )
    // TODO: 原始demo组件的字段，待移除
    private String outputField;

    private DatabaseMeta databaseMeta;

    // TODO: 三个字段存到 油气田管理层级模型中
//    private String operateArea;
//    private String platform;
//    private String machine;

    // TODO：用ValueMetaInterface 代替
//    private List<EfficiencyField> efficiencyFields;

    /**
     * TODO： 待移除
     * 油气田管理层级模型
     */
//    private List<OilManagementModel> oilManagementModels;


    /**
     * 查询条件，表示某个字段所在的条件范围
     *      流程查询能效数据时用到，需要在button确认按钮事件时候读取并初始化。
     *      比如:
     *          1. modelLabel字段等于 "platform"
     *          2. modelId 在 (1, 2, 3) 范围内
     *      需要在meta对象构建的时候从文件中反序列化出来。
     *
     *      当字段（key值）为modelLabel时候，我们取查询条件只取list的最后一个元素
     */
    private Map<ValueMetaInterface, List<Object>> fieldConditionMap;

    /**
     * 存储对象与时段字段，作为能效数据的基础维度，执行查询时候放能效字段前
     *      1. 对象字段 -> modelLabel，modelId
     *      2. 时段字段 -> aggregationCycle, logTime
     */
    private List<ValueMetaInterface> objectAndTimeFieldMetas;

    /**
     * 存储能效字段，不包括label, id, name
     * 在确定按钮事件发生时，按展示的顺序，加到这个列表中
     */
    private List<ValueMetaInterface> effFieldMetas;

    /**
     * Constructor should call super() to make sure the base class has a chance to initialize properly.
     */
    public OilEfficiencyQueryMeta() {
        super();
        /*
         * 初始化要展示的对象和时间字段
         */
        objectAndTimeFieldMetas.add(new ValueMetaBase("modelLabel", ValueMetaInterface.TYPE_STRING));
        objectAndTimeFieldMetas.add(new ValueMetaBase("modelId", ValueMetaInterface.TYPE_INTEGER));
        objectAndTimeFieldMetas.add(new ValueMetaBase("aggregationCycle", ValueMetaInterface.TYPE_INTEGER));
        objectAndTimeFieldMetas.add(new ValueMetaBase("logTime", ValueMetaInterface.TYPE_INTEGER));

//        // TODO： 待移除
//        oilManagementModels = new ArrayList<>(3);
//        oilManagementModels.add(new OilManagementModel("operationArea".toLowerCase()));
//        oilManagementModels.add(new OilManagementModel("platform".toLowerCase()));
//        oilManagementModels.add(new OilManagementModel("mechanicalMiningMachine".toLowerCase()));
    }

    /**
     * 返回实现的对话框
     * @param shell    an SWT Shell
     * @param meta     description of the step
     * @param transMeta  description of the the transformation
     * @param name    the name of the step
     * @return       new instance of a dialog for this step
     */
    public StepDialogInterface getDialog( Shell shell, StepMetaInterface meta, TransMeta transMeta, String name ) {
        return new OilEfficiencyQueryDialog( shell, meta, transMeta, name );
    }

    /**
     * 返回实现的步骤处理类
     * @param stepMeta        description of the step
     * @param stepDataInterface    instance of a step data class
     * @param cnr          copy number
     * @param transMeta        description of the transformation
     * @param disp          runtime implementation of the transformation
     * @return            the new instance of a step implementation
     */
    @Override
    public StepInterface getStep(StepMeta stepMeta, StepDataInterface stepDataInterface, int cnr, TransMeta transMeta,
                                 Trans disp ) {
        return new OilEfficiencyQuery( stepMeta, stepDataInterface, cnr, transMeta, disp );
    }

    /**
     * 返回实现的步骤数据类
     */
    @Override
    public StepDataInterface getStepData() {
        return new OilEfficiencyQueryData();
    }

    /**
     * 每次创建Step时会被Spoon调用，一般在这里设置一些需要初始化的值。
     */
    @Override
    public void setDefault() {
        setOutputField( "demo_field" );
    }

    /**
     * Getter for the name of the field added by this step
     * @return the name of the field added
     */
    public String getOutputField() {
        return outputField;
    }

    /**
     * Setter for the name of the field added by this step
     * @param outputField the name of the field added
     */
    public void setOutputField( String outputField ) {
        this.outputField = outputField;
    }

    /**
     * 拷贝Step，必须在里面实现对变量的深拷贝
     * @return 当前对象的深拷贝
     */
    @Override
    public Object clone() {
        Object retValue = super.clone();

        return retValue;
    }

    /**
     * This method is called by Spoon when a step needs to serialize its configuration to XML. The expected
     * return value is an XML fragment consisting of one or more XML tags.
     *
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently generate the XML.
     *
     * @return a string containing the XML serialization of this step
     */
    @SneakyThrows
    @Override
    public String getXML() throws KettleValueException {

        StringBuilder xml = new StringBuilder();

//        xml.append(XMLHandler.addTagValue("operateArea",operateArea));
//        xml.append(XMLHandler.addTagValue("platform",platform));
//        xml.append(XMLHandler.addTagValue("machine",machine));
//        String fieldStr = new ObjectMapper().writeValueAsString(efficiencyFields);
//        String oilModels=new ObjectMapper().writeValueAsString(oilManagementModels);
//        xml.append(XMLHandler.addTagValue("oilModels",oilModels));
//        xml.append(XMLHandler.addTagValue("operateArea",oilManagementModels.get(0).getName()));
//        xml.append(XMLHandler.addTagValue("platform",oilManagementModels.get(1).getName()));
//        xml.append(XMLHandler.addTagValue("machine",oilManagementModels.get(2).getName()));

        String fieldStr = new ObjectMapper().writeValueAsString(effFieldMetas);
        xml.append(XMLHandler.addTagValue("fieldstr",fieldStr));
//        String objectAndTimeFieldMetas=new  ObjectMapper().writeValueAsString(objectAndTimeFieldMetas));
        return xml.toString();
    }

    /**
     * This method is called by PDI when a step needs to load its configuration from XML.
     *
     * Please use org.pentaho.di.core.xml.XMLHandler to conveniently read from the
     * XML node passed in.
     *
     * @param stepnode  the XML node containing the configuration
     * @param databases  the databases available in the transformation
     * @param metaStore the metaStore to optionally read from
     */
    @Override
    public void loadXML(Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
        try {
            String con = XMLHandler.getTagValue( stepnode, "connection" );
            databaseMeta = DatabaseMeta.findDatabase( databases, con );
            String fieldStr = XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "fields"));
            setEffFieldMetas(new ObjectMapper().readValue(fieldStr,List.class));
            String oilModels=XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode,"oilModels"));
//            setOilManagementModels(new ObjectMapper().readValue(oilModels,List.class));
//            setEfficiencyFields(new ObjectMapper().readValue(fieldStr, List.class));
//            setOperateArea(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "operateArea")));
//            setPlatform(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "platform")));
//            setMachine(XMLHandler.getNodeValue(XMLHandler.getSubNode(stepnode, "machine")));
        } catch ( Exception e ) {
            throw new KettleXMLException( "Demo plugin unable to read step info from XML node", e );
        }
    }

    /**
     * 当step需要将其配置保存到存储库时被spoon调用
     *
     * @param rep                 the repository to save to
     * @param metaStore           the metaStore to optionally write to
     * @param id_transformation   the id to use for the transformation when saving
     * @param id_step             the id to use for the step  when saving
     */
    @Override
    public void saveRep(Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step )
            throws KettleException {
        try {
//            // $NON-NLS-1$
//            rep.saveStepAttribute( id_transformation, id_step, "outputfield", outputField );
        } catch ( Exception e ) {
            throw new KettleException( "Unable to save step into repository: " + id_step, e );
        }
    }

    /**
     * 在step需要从存储库中读取配置时调用改方法
     *
     * @param rep        the repository to read from
     * @param metaStore  the metaStore to optionally read from
     * @param id_step    the id of the step being read
     * @param databases  the databases available in the transformation
     */
    @Override
    public void readRep(Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases )
            throws KettleException {
        try {
            databaseMeta = rep.loadDatabaseMetaFromStepAttribute( id_step, "id_connection", databases );
//            outputField  = rep.getStepAttributeString( id_step, "outputfield" );
        } catch ( Exception e ) {
            throw new KettleException( "Unable to load step from repository", e );
        }
    }

    /**
     * 在Step对行流所做任何更改时，必须调用此方法。
     *     如果想在下一个步骤获取这个步骤新增的字段名称，必须在这里新增字段。
     *     定义下个组件能接受到的字段，如input组件SQL中的字段，都需要在这个方法里面写，才能在下个组件里面接受
     *
     * @param inputRowMeta    the row structure coming in to the step
     * @param name         the name of the step making the changes
     * @param info        row structures of any info steps coming in
     * @param nextStep      the description of a step this step is passing rows to
     * @param space        the variable space for resolving variables
     * @param repository    the repository instance optionally read from
     * @param metaStore      the metaStore to optionally read from
     */
    public void getEfficiencyFields(RowMetaInterface inputRowMeta, String name, RowMetaInterface[] info, StepMeta nextStep,
                                    VariableSpace space, Repository repository, IMetaStore metaStore ) throws KettleStepException {

        /*
         * This implementation appends the outputField to the row-stream
         */

//        // a value meta object contains the meta data for a field
//        ValueMetaInterface v = new ValueMetaString( outputField );
//
//        // setting trim type to "both"
//        v.setTrimType( ValueMetaInterface.TRIM_TYPE_BOTH );
//
//        // the name of the step that adds this field
//        v.setOrigin( name );
//
//        // modify the row structure and add the field this step generates
//        inputRowMeta.addValueMeta( v );
    }

    /**
     * 在用户选择"Verify Transformation"时，Spoon将调用此方法
     *
     * Typical checks include:
     * - verify that all mandatory configuration is given
     * - verify that the step receives any input, unless it's a row generating step
     * - verify that the step does not receive any input if it does not take them into account
     * - verify that the step finds fields it relies on in the row-stream
     *
     *   @param remarks    the list of remarks to append to
     *   @param transMeta  the description of the transformation
     *   @param stepMeta  the description of the step
     *   @param prev      the structure of the incoming row-stream
     *   @param input      names of steps sending input to the step
     *   @param output    names of steps this step is sending output to
     *   @param info      fields coming in from info steps
     *   @param metaStore  metaStore to optionally read from
     */
    @Override
    public void check(List<CheckResultInterface> remarks, TransMeta transMeta, StepMeta stepMeta, RowMetaInterface prev,
                      String[] input, String[] output, RowMetaInterface info, VariableSpace space, Repository repository,
                      IMetaStore metaStore ) {
        CheckResult cr;

        // See if there are input streams leading to this step!
        if ( input != null && input.length > 0 ) {
            cr = new CheckResult( CheckResult.TYPE_RESULT_OK,
                    BaseMessages.getString( PKG, "Demo.CheckResult.ReceivingRows.OK" ), stepMeta );
            remarks.add( cr );
        } else {
            cr = new CheckResult( CheckResult.TYPE_RESULT_ERROR,
                    BaseMessages.getString( PKG, "Demo.CheckResult.ReceivingRows.ERROR" ), stepMeta );
            remarks.add( cr );
        }
    }

    /**
     * 如果需要对插件进行错误处理步骤，实现错误分流（主步骤输出，错误处理步骤），需要重写父类方法，返回true
     * @return 是否支持错误处理
     */
    @Override
    public boolean supportsErrorHandling() {
        return true;
    }
}