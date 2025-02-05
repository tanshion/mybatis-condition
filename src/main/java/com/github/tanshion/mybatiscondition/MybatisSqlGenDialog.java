/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.tanshion.mybatiscondition;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.CommonClassNames;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.github.tanshion.mybatiscondition.MybatisSqlGen.isList;
import static com.github.tanshion.mybatiscondition.MybatisSqlGen.isListOfType;
import static com.github.tanshion.mybatiscondition.OpenMybatisSqlGenDialogAction.project;

public class MybatisSqlGenDialog extends DialogWrapper {
    private JTextField tableAliasField;
    private JTextField paramPrefixField;
    private JTextArea codeArea;
    private final PsiClass psiClass;
    Map<String, String> fieldOperation;
    private ComboBox<String> dbTypeComboBox;


    public MybatisSqlGenDialog(Project project, PsiClass psiClass) {
        super(project);
        this.psiClass = psiClass;
        fieldOperation = new HashMap<>();
        init();
        setTitle("MyBatis Condition Generator");
        setResizable(true);
    }

    @Override
    protected String getDimensionServiceKey() {
        return "MybatisSqlGenDialog";
    }

    @Override
    protected void init() {
        super.init();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int width = (int) (screenSize.width * 0.8);
        int height = (int) (screenSize.height * 0.8);
        setSize(width, height);
    }

    @Override
    protected JComponent createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Left Panel
        JPanel leftPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = JBUI.insets(5);

        // DB Type Label
        JLabel dbTypeLabel = new JLabel("DB Type:");
        gbc.gridx = 0;
        gbc.gridy = 0;
        leftPanel.add(dbTypeLabel, gbc);

        // DB Type ComboBox
        String[] dbTypes = {MybatisSqlGen.MYSQL, MybatisSqlGen.POSTGRESQL, MybatisSqlGen.ORACLE};
        dbTypeComboBox = new ComboBox<>(dbTypes);
        dbTypeComboBox.addActionListener(e -> {
            String selectedDbType = (String) dbTypeComboBox.getSelectedItem();
            // 保存配置
            MybatisConditionSettings.getInstance(project).setDbType(selectedDbType);
            updateCode();
        });
        gbc.gridx = 1;
        gbc.gridy = 0;
        leftPanel.add(dbTypeComboBox, gbc);

        JLabel tableAliasLabel = new JLabel("Table Alias:");
        gbc.gridx = 0;
        gbc.gridy = 1;
        leftPanel.add(tableAliasLabel, gbc);

        tableAliasField = new JTextField("t");
        gbc.gridx = 1;
        gbc.gridy = 1;
        leftPanel.add(tableAliasField, gbc);

        JLabel paramPrefixLabel = new JLabel("Param Prefix:");
        gbc.gridx = 0;
        gbc.gridy = 2;
        leftPanel.add(paramPrefixLabel, gbc);

        paramPrefixField = new JTextField("param");
        gbc.gridx = 1;
        gbc.gridy = 2;
        leftPanel.add(paramPrefixField, gbc);

        JLabel fieldsLabel = new JLabel("Fields:");
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        leftPanel.add(fieldsLabel, gbc);

        JPanel fieldsPanel = new JPanel(new GridBagLayout());
        GridBagConstraints fieldsGbc = new GridBagConstraints();
        fieldsGbc.fill = GridBagConstraints.HORIZONTAL;
        fieldsGbc.insets = JBUI.insets(5);
        fieldsGbc.gridx = 0;
        fieldsGbc.gridy = 0;

        DefaultListModel<String> fieldListModel = new DefaultListModel<>();
        for (PsiField field : psiClass.getAllFields()) {
            fieldListModel.addElement(field.getName());
            String[] operations;
            if (field.getType().getCanonicalText().equals(CommonClassNames.JAVA_LANG_STRING)) {
                operations = new String[]{MybatisSqlGen.EQ, MybatisSqlGen.LIKE};
                fieldOperation.put(field.getName(), MybatisSqlGen.EQ);
            } else if (isListOfType(field, LocalDateTime.class, Date.class)) {
                operations = new String[]{MybatisSqlGen.BETWEEN};
                fieldOperation.put(field.getName(), MybatisSqlGen.BETWEEN);
            } else if (isList(field)) {
                operations = new String[]{MybatisSqlGen.IN, MybatisSqlGen.NOT_IN, MybatisSqlGen.BETWEEN};
                fieldOperation.put(field.getName(), MybatisSqlGen.IN);
            } else if (field.getType().getCanonicalText().equals(CommonClassNames.JAVA_LANG_BOOLEAN) || field.getType().getCanonicalText().equals("boolean")) {
                operations = new String[]{MybatisSqlGen.EQ};
                fieldOperation.put(field.getName(), MybatisSqlGen.EQ);
            } else {
                operations = new String[]{MybatisSqlGen.EQ, MybatisSqlGen.NOT_EQ, MybatisSqlGen.GT, MybatisSqlGen.LT, MybatisSqlGen.GTE, MybatisSqlGen.LTE};
                fieldOperation.put(field.getName(), MybatisSqlGen.EQ);
            }
            ComboBox<String> operationComboBox = new ComboBox<>(operations);
            operationComboBox.addActionListener(e -> {
                fieldOperation.put(field.getName(), (String) operationComboBox.getSelectedItem());
                updateCode();
            });

            JLabel fieldNameLabel = new JLabel(field.getName());
            fieldsGbc.gridx = 0;
            fieldsPanel.add(operationComboBox, fieldsGbc);
            fieldsGbc.gridx = 1;
            fieldsPanel.add(fieldNameLabel, fieldsGbc);
            fieldsGbc.gridx = 0;
            fieldsGbc.gridy++;
        }

        JBScrollPane fieldsScrollPane = new JBScrollPane(fieldsPanel);
        gbc.gridx = 0;
        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        leftPanel.add(fieldsScrollPane, gbc);

        // Right Panel
        JPanel rightPanel = new JPanel(new BorderLayout());
        codeArea = new JTextArea();
        codeArea.setEditable(false);
        rightPanel.add(new JScrollPane(codeArea), BorderLayout.CENTER);

        // Split Panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel);
        splitPane.setDividerLocation(300);
        panel.add(splitPane, BorderLayout.CENTER);

        // Add listeners
        tableAliasField.addActionListener(e -> updateCode());
        paramPrefixField.addActionListener(e -> updateCode());

        // 设置默认值
        String savedDbType = MybatisConditionSettings.getInstance(project).getDbType();
        if (savedDbType != null) {
            dbTypeComboBox.setSelectedItem(savedDbType);
        }
        updateCode();
        return panel;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        copyCodeToClipboard();
    }

    private void copyCodeToClipboard() {
        String code = codeArea.getText();
        if (code != null && !code.isEmpty()) {
            // 使用 Toolkit 获取系统剪贴板
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            // 创建一个新的字符串选择对象
            StringSelection stringSelection = new StringSelection(code);
            // 将字符串选择对象设置到剪贴板
            clipboard.setContents(stringSelection, null);
        }
    }

    private void updateCode() {
        String dbType = (String) dbTypeComboBox.getSelectedItem();
        String tableAlias = tableAliasField.getText();
        String paramPrefix = paramPrefixField.getText();
        String generatedCode = MybatisSqlGen.generateMyBatisConditions(psiClass, tableAlias, paramPrefix, fieldOperation, dbType);
        codeArea.setText(generatedCode);
    }

    public static void showDialog(Project project, PsiClass psiClass) {
        MybatisSqlGenDialog dialog = new MybatisSqlGenDialog(project, psiClass);
        dialog.show();


    }

}
