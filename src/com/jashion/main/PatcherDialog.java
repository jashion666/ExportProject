package com.jashion.main;

import b.c.P;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.CompilerModuleExtension;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.ToolbarDecorator;
import com.intellij.ui.components.JBList;

import javax.swing.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.LinkedList;
import java.util.List;

public class PatcherDialog extends JDialog {

    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;

    private JTextField textField;
    private JButton fileChooseBtn;
    private JPanel filePanel;
    private AnActionEvent event;
    private JBList fieldList;

    PatcherDialog(final AnActionEvent event) {
        this.event = event;
        setTitle("Create Patcher Dialog");

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(e -> onOK());

        buttonCancel.addActionListener(e -> onCancel());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(e -> onCancel(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        // 保存路径按钮事件
        fileChooseBtn.addActionListener(e -> {
            String userDir = System.getProperty("user.home");
            JFileChooser fileChooser = new JFileChooser(userDir + "/Desktop");
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            int flag = fileChooser.showOpenDialog(null);
            if (flag == JFileChooser.APPROVE_OPTION) {
                textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

    }

    private void onOK() {
        // 条件校验
        if (null == textField.getText() || "".equals(textField.getText())) {
            Messages.showErrorDialog(this, "Please Select Save Path!", "Error");
            return;
        }

        ListModel<VirtualFile> model = fieldList.getModel();
        if (model.getSize() == 0) {
            Messages.showErrorDialog(this, "Please Select Export File!", "Error");
            return;
        }

        try {
            String exportPath = textField.getText() + "\\";
            // 获取工程名
            String projectName = this.event.getRequiredData(LangDataKeys.PROJECT).getName();
            // 遍历选择的多个文件或者文件夹
            for (int i = 0; i < model.getSize(); i++) {
                VirtualFile element = model.getElementAt(i);
                // 获取的每个路径
                String elementPath = element.getPath();
                List<Path> result = new LinkedList<>();
                // 遍历路径中每一个文件
                Files.walkFileTree(Paths.get(elementPath), new FindJavaVisitor(result));
                for (Path path : result) {
                    String finalPath = path.toString();
                    String subPath = finalPath.substring(finalPath.indexOf(projectName));
                    String realPath = exportPath + projectName+ "\\" + subPath;
                    // 创建多级目录
                    Files.createDirectories(Paths.get(realPath).getParent());
                    // 替换文件
                    Files.copy(path, Paths.get(realPath), StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } catch (Exception e) {
            Messages.showErrorDialog(this, "Create Patcher Error!", "Error");
            e.printStackTrace();
        }
        dispose();
    }

    private void onCancel() {
        // add your code here if necessary
        dispose();
    }

    private void createUIComponents() {
        VirtualFile[] data = event.getData(DataKeys.VIRTUAL_FILE_ARRAY);
        fieldList = new JBList(data);
        fieldList.setEmptyText("No File Selected!");
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        filePanel = decorator.createPanel();
    }


    private static class FindJavaVisitor extends SimpleFileVisitor<Path> {
        private List<Path> result;

        FindJavaVisitor(List<Path> result) {
            this.result = result;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            result.add(file.toAbsolutePath());
            return FileVisitResult.CONTINUE;
        }
    }
}
