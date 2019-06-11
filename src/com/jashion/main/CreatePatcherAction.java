package com.jashion.main;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;

public class CreatePatcherAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        PatcherDialog dialog = new PatcherDialog(e);
        dialog.setSize(600, 400);
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        dialog.requestFocus();
    }
}
