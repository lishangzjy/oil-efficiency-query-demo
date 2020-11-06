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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.*;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.database.Database;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.core.row.value.ValueMetaBase;
import org.pentaho.di.core.row.value.ValueMetaFactory;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.ui.core.dialog.EnterSelectionDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 插件的对话框，提供GUI/dialog，编辑步骤的元数据。
 *
 * @author Jinhua
 * @author Li Chunsheng
 */
public class OilEfficiencyQueryDialog extends BaseStepDialog implements StepDialogInterface {

    /**
     * 当前类对象
     * for i18n purposes
     */
    private static Class<?> PKG = OilEfficiencyQueryMeta.class;

    /**
     * 步骤元数据
     */
    private OilEfficiencyQueryMeta meta;

    private Label operateAreaLabel;
    private Text operateAreaText;
    private Label platformLabel;
    private Text platformText;
    private Label machineLabel;
    private Text machineText;
    private TableView changeTableView;
    private Button operateButton;
    private Button platformButton;
    private Button machineButton;

    /**
     * The constructor should simply invoke super() and save the incoming meta
     * object to a local variable, so it can conveniently read and write settings
     * from/to it.
     *
     * @param parent    the SWT shell to open the dialog in
     * @param in        the meta object holding the step's settings
     * @param transMeta transformation description
     * @param stepName  the step name
     */
    public OilEfficiencyQueryDialog(Shell parent, Object in, TransMeta transMeta, String stepName) {
        super(parent, (BaseStepMeta) in, transMeta, stepName);
        meta = (OilEfficiencyQueryMeta) in;
    }

    /**
     * 在用户打开插件Dialog时被Spoon调用，仅当用户关闭对话框时返回，
     * 此方法必须在用户确认时返回这个步骤的名字，或者在用户取消时返回null。
     * 其中，changed标记必须反映对话框是否更改了步骤配置，用户取消时，标志位不能改变。
     */
    @Override
    public String open() {
        // store some convenient SWT variables
        Shell parent = getParent();
        Display display = parent.getDisplay();

        // SWT code for preparing the dialog
        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MIN | SWT.MAX);
        props.setLook(shell);
        setShellImage(shell, meta);

        // 保存changeed标志到meta对象，如果用户取消按这个值恢复。changed属性变量从BaseStepDialog类继承获得
        changed = meta.hasChanged();

        // ModifyListener用于监听所有控件，当发生操作时，更新meta对象changed标志
        ModifyListener lsMod = new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                meta.setChanged();
            }
        };

        // ------------------------------------------------------- //
        // SWT code for building the actual settings dialog        //
        // ------------------------------------------------------- //
        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;
        shell.setLayout(formLayout);
        shell.setText(BaseMessages.getString(PKG, "Demo.Shell.Title"));
        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        wlStepname = new Label(shell, SWT.RIGHT);
        wlStepname.setText(BaseMessages.getString(PKG, "System.Label.StepName"));
        props.setLook(wlStepname);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        wlStepname.setLayoutData(fdlStepname);

        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(lsMod);

        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        operateAreaLabel = new Label(shell, SWT.RIGHT);
        operateAreaLabel.setText("作业区");
        props.setLook(operateAreaLabel);
        operateAreaLabel.setLayoutData(getLeftFormDataFromPre(wStepname, middle, margin));
        operateAreaText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        operateAreaText.setEditable(false);
        operateAreaText.addModifyListener(lsMod);
        props.setLook(operateAreaText);
        operateAreaText.setLayoutData(getRightFormDataFromPre(wStepname, middle, margin));
        operateButton = new Button(shell, SWT.PUSH);
        operateButton.setText("浏览");
        props.setLook(operateButton);
        operateButton.setLayoutData(getFormDataFromPre(wStepname, operateAreaText, margin));

        platformLabel = new Label(shell, SWT.RIGHT);
        platformLabel.setText("平台");
        props.setLook(platformLabel);
        platformLabel.setLayoutData(getLeftFormDataFromPre(operateAreaLabel, middle, margin));
        platformText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        platformText.setEditable(false);
        platformText.addModifyListener(lsMod);
        props.setLook(platformText);
        platformText.setLayoutData(getRightFormDataFromPre(operateAreaLabel, middle, margin));
        platformButton = new Button(shell, SWT.PUSH);
        platformButton.setText("浏览");
        props.setLook(platformButton);
        platformButton.setLayoutData(getFormDataFromPre(operateAreaLabel, platformText, margin));

        machineLabel = new Label(shell, SWT.RIGHT);
        machineLabel.setText("机采设备");
        props.setLook(machineLabel);
        machineLabel.setLayoutData(getLeftFormDataFromPre(platformLabel, middle, margin));
        machineText = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        machineText.setEditable(false);
        machineText.addModifyListener(lsMod);
        props.setLook(machineText);
        machineText.setLayoutData(getRightFormDataFromPre(platformLabel, middle, margin));
        machineButton = new Button(shell, SWT.PUSH);
        machineButton.setText("浏览");
        props.setLook(machineButton);
        machineButton.setLayoutData(getFormDataFromPre(platformLabel, machineText, margin));

        final int fieldsCols = 3;
        final int fieldsRows = 2;
        Control lastControl = machineButton;

        ColumnInfo[] columnInfos;
        columnInfos = new ColumnInfo[fieldsCols];
        columnInfos[0] = new ColumnInfo("字段", ColumnInfo.COLUMN_TYPE_TEXT, new String[]{""}, false);
        //columnInfos[1] = new ColumnInfo("类型", ColumnInfo.COLUMN_TYPE_CCOMBO,new String[]{"Number","String","Date","Boolean","Integer","BigNumber","Binary","Timestamp"},false);
        columnInfos[1] = new ColumnInfo("类型", ColumnInfo.COLUMN_TYPE_CCOMBO, ValueMetaFactory.getValueMetaNames(), false);
        //ValueMetaFactory.getValueMetaNames();
        columnInfos[2] = new ColumnInfo("选中", ColumnInfo.COLUMN_TYPE_CCOMBO, new String[]{"true", "false"}, false);

        changeTableView = new TableView(transMeta, shell, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI, columnInfos, fieldsRows, lsMod, props);

        FormData fd = new FormData();
        fd.left = new FormAttachment(0, 0);
        fd.top = new FormAttachment(lastControl, margin);
        fd.right = new FormAttachment(100, 0);
        fd.bottom = new FormAttachment(100, -50);
        changeTableView.setLayoutData(fd);
        lastControl = changeTableView;

        // OK and cancel buttons
        wOK = new Button(shell, SWT.PUSH);
        wOK.setText(BaseMessages.getString(PKG, "System.Button.OK"));
        wCancel = new Button(shell, SWT.PUSH);
        wCancel.setText(BaseMessages.getString(PKG, "System.Button.Cancel"));
        wGet = new Button(shell, SWT.PUSH);
        wGet.setText("获取字段");
        setButtonPositions(new Button[]{wGet, wOK, wCancel}, margin, null);

        // Add listeners for cancel and OK
        lsCancel = new Listener() {
            @Override
            public void handleEvent(Event e) {
                cancel();
            }
        };
        lsOK = new Listener() {
            @Override
            public void handleEvent(Event e) {
                ok();
            }
        };
        lsGet = new Listener() {
            @Override
            public void handleEvent(Event event) {
                get();
            }
        };
        Listener getOperate = new Listener() {
            @Override
            public void handleEvent(Event event) {
                getOperate();
            }
        };
        Listener getPlatform = new Listener() {
            @Override
            public void handleEvent(Event event) {
                getPlatform();
            }
        };
        Listener getMachine = new Listener() {
            @Override
            public void handleEvent(Event event) {
                getMachine();
            }
        };
        wCancel.addListener(SWT.Selection, lsCancel);
        wOK.addListener(SWT.Selection, lsOK);
        wGet.addListener(SWT.Selection, lsGet);
        operateButton.addListener(SWT.Selection, getOperate);
        platformButton.addListener(SWT.Selection, getPlatform);
        machineButton.addListener(SWT.Selection, getMachine);
        // 默认监听器，响应回车
        lsDef = new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                ok();
            }
        };
        wStepname.addSelectionListener(lsDef);
        operateAreaText.addSelectionListener(lsDef);
        platformText.addSelectionListener(lsDef);
        machineText.addSelectionListener(lsDef);

        // Detect X or ALT-F4 or something that kills this window and cancel the dialog properly
        shell.addShellListener(new ShellAdapter() {
            @Override
            public void shellClosed(ShellEvent e) {
                cancel();
            }
        });

        // Set/Restore the dialog size based on last position on screen
        // The setSize() method is inherited from BaseStepDialog
        setSize();

        // populate the dialog with the values from the meta object
        populateDialog();

        // restore the changed flag to original value, as the modify listeners fire during dialog population
        meta.setChanged(changed);

        // 打开对话框，进去事件循环
        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        // ok或者cancel被执行时，调用dispose，此时对话框跳出事件循环，马上关闭。
        // stepname从BaseStepDialog类继承获得
        return stepname;
    }

    /**
     * 在打开插件Dialog前进行数据填充，填充后显示Dialog，一般在shell.open()之前被open()调用。
     */
    private void populateDialog() {
//        wStepname.selectAll();
//        String operateArea=meta.getOilManagementModels().get(0).getName();
//        String platform=meta.getOilManagementModels().get(1).getName();
//        String machine=meta.getOilManagementModels().get(2).getName();
//        int row = 0;
//        if (operateArea != null) {
//            operateAreaText.setText(operateArea);
//        }
//        if (platform != null) {
//            platformText.setText(platform);
//        }
//        if (machine != null) {
//            machineText.setText(machine);
//        }
//        if (meta.getEffFieldMetas() != null) {
//            for (ValueMetaInterface valueMeta : meta.getEffFieldMetas()) {
//                if (valueMeta.getName() != null) {
//                    changeTableView.setText(valueMeta.getName(), 1, row);
//                }
//                changeTableView.setText(valueMeta.getTypeDesc(), 2, row);
//            }
//        }
    }

    /**
     * 点击获取变量按钮调用
     */
    private void get() {
        DatabaseMeta databaseMeta = new DatabaseMeta("matterhorn", "PostgreSQL", "JNDI", "172.17.6.121", "matterhorn", "5432", "postgres", "y6fqdy");
        Database database = new Database(loggingObject, databaseMeta);
        try {
            RowMetaInterface rowMetaInterface = database.getTableFields("operationarea");
            String[] str = rowMetaInterface.getFieldNames();
            int r = 0;
            for (String s : str) {
                changeTableView.setText(s, 1, r++);
            }
        } catch (Exception e) {

        }


    }

    /**
     * 解析返回参数
     *
     * @param rs 查询数据库返回的字节集
     * @return list集合
     */
    private List<Map<String, Object>> getRow(ResultSet rs) {
        List<Map<String, Object>> rows = new ArrayList<>();
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
                    row = new HashMap<>();
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
        } catch (Exception e) {

        }
        return rows;
    }

    /**
     * 获取工作区
     */
    private void getOperate() {
        String db = "matterhorn";
        String sql = "select * from operationarea  order by id";
        String shellText = "工作区";
        String message = "请选择工作区";
        getdialog(db, sql, shellText, message);
    }

    /**
     * 获取平台
     */
    private void getPlatform() {
        String db = "matterhorn";
        String sql = "select * from platform  order by id";
        String shellText = "平台";
        String message = "请选择平台";
        getdialog(db, sql, shellText, message);
    }

    /**
     * 获取机采设备
     */
    private void getMachine() {
        String db = "matterhorn";
        String sql = "select * from mechanicalminingmachine  order by id";
        String shellText = "设备";
        String message = "请选择设备";
        getdialog(db, sql, shellText, message);
    }

    /**
     * 从数据库查询信息并且返回一个选择框
     *
     * @param db        数据库名
     * @param sql       sql语句
     * @param shellText 选择框标题
     * @param message   选择框提示信息
     */
    private void getdialog(String db, String sql, String shellText, String message) {
        DatabaseMeta databaseMeta = new DatabaseMeta("matterhorn", "PostgreSQL", "JNDI", "172.17.6.121", "matterhorn", "5432", "postgres", "y6fqdy");
        // DatabaseMeta databaseMeta=transMeta.findDatabase(db);
        Database database = new Database(loggingObject, databaseMeta);
        List<Map<String, Object>> rows = null;
        try {
            database.connect();
            PreparedStatement ps = database.prepareSQL(sql);
            ResultSet set = ps.executeQuery();
            rows = getRow(set);
        } catch (Exception ignored) {

        }
        assert rows != null;
        List list = rows.stream().map(e -> e.get("id") + "_" + e.get("name")).collect(Collectors.toList());
        String[] string = new String[list.size()];
        list.toArray(string);

        EnterSelectionDialog dialog = new EnterSelectionDialog(shell, string, shellText, message);
        String selection = dialog.open();
        if (selection != null) {
            machineText.setText(selection);
        }
    }


    /**
     * 点击取消按钮调用，需要在open中绑定控件监听器。
     */
    private void cancel() {
        // stepname 变量由open方法返回，取消时必须返回null
        stepname = null;
        // 恢复元数据对象changed标志到原来的样子
        meta.setChanged(changed);
        // 释放关掉对话框
        dispose();
    }

    /**
     * 点击确定按按钮调用，需要在open中绑定控件的监听器,设置参数
     */
    private void ok() {
        //确认时必须返回步骤名
        stepname = wStepname.getText();
//        List<OilManagementModel> oilManagementModels = new ArrayList<>();
//        OilManagementModel oilModel=new OilManagementModel();
//        String [] oText=operateAreaText.getText().split("_",2);
//        oilModel.setName(oText[1]);
//        oilManagementModels.add(oilModel);
//        oilModel=new OilManagementModel();
//        String [] pText=platformText.getText().split("_",2);
//        oilModel.setName(pText[1]);
//        oilManagementModels.add(oilModel);
//        oilModel=new OilManagementModel();
//        String [] mText = machineText.getText().split("_",2);
//        oilModel.setName(mText[1]);
//        meta.setOilManagementModels(oilManagementModels);
//
//        int count = changeTableView.nrNonEmpty();
//        List<ValueMetaInterface> valueMetas=new ArrayList<>();
//        ValueMetaInterface valueMeta=new ValueMetaBase();
//        for (int i = 0; i < count; i++) {
//            TableItem item = changeTableView.getNonEmpty(i);
//            valueMeta=new ValueMetaBase();
//            valueMeta.setName(item.getText(1));
//            boolean bool = Arrays.asList(ValueMetaFactory.getValueMetaNames()).contains(item.getText(2));
//            if (bool) {
//                valueMeta.setsType(1);
//            }
//            if ("true".equals(item.getText(3)) || "false".equals(item.getText(3))) {
//                data.setSelected(item.getText(3));
//            }
//            efficiencyFields.add(data);
//        }
//        meta.setEfficiencyFields(efficiencyFields);
        dispose();
    }

    /**
     * 返回标签布局
     *
     * @param topControl 上一行控件
     * @param middle     布局样式
     * @param margin     间隔
     * @return 标签布局
     */
    private FormData getLeftFormDataFromPre(Control topControl, int middle, int margin) {
        FormData valName = new FormData();
        valName.left = new FormAttachment(0, 0);
        valName.right = new FormAttachment(middle, -margin);
        valName.top = new FormAttachment(topControl, margin * 2);
        return valName;
    }

    /**
     * 返回文本框布局
     *
     * @param preControl 上一行控件
     * @param middle     布局样式
     * @param margin     间隔
     * @return 文本框布局
     */
    private FormData getRightFormDataFromPre(Control preControl, int middle, int margin) {
        FormData valName = new FormData();
        valName.left = new FormAttachment(middle, 0);
        valName.right = new FormAttachment(100, -70);
        valName.top = new FormAttachment(preControl, margin * 2);
        return valName;
    }

    /**
     * 返回按钮布局
     *
     * @param topControl  上一行控件
     * @param leftControl 左侧控件
     * @param margin      间隔
     * @return 按钮布局
     */
    private FormData getFormDataFromPre(Control topControl, Control leftControl, int margin) {
        FormData valName = new FormData();
        valName.left = new FormAttachment(leftControl, margin);
        valName.right = new FormAttachment(100, 0);
        valName.top = new FormAttachment(topControl, margin * 2);
        return valName;
    }

}
