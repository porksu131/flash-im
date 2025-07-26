package com.szr.flashim.example.gui.menu;

import java.awt.event.ActionListener;

public class PopupMenuItemInfo {
    private String text;
    private ActionListener actionListener;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public ActionListener getActionListener() {
        return actionListener;
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }
}
