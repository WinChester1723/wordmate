package gui;

import gui.frames.GUIFrame;
import gui.panels.TranslateUIPanel;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;

public final class UICore {
    private static UICore singleton = null;

    public GUIFrame<JFrame> mainUIFrame = null;

    public TranslateUIPanel mainUIPanel = null;

    private static Map<String, GUIFrame<JFrame>> frameList = null;

    public void appendFrame(String frameName, GUIFrame<JFrame> frame) {
        frameList.put(frameName, frame);
    }

    public Map<String, GUIFrame<JFrame>> getFrameList() {
        return frameList;
    }

    public Object getFrame(String frameName) {
        for (var frame : frameList.entrySet())
            if (frame.getKey().equals(frameName))
                return frame.getValue();

        return null;
    }

    public String getFrame(Object _frame) {
        for (var frame : frameList.entrySet())
            if (frame.getValue() == _frame) // TODO: Try .equals()
                return frame.getKey();

        return null;
    }

    public GUIFrame<JFrame> getMainUIFrame() {
        return mainUIFrame;
    }

    public void setMainUIFrame(GUIFrame<JFrame> mainUIFrame) {
        this.mainUIFrame = mainUIFrame;
    }

    public TranslateUIPanel getMainUIPanel() {
        return mainUIPanel;
    }

    public void setMainUIPanel(TranslateUIPanel mainUIPanel) {
        this.mainUIPanel = mainUIPanel;
    }

    public static UICore getInstance() {
        if (singleton == null) {
            singleton = new UICore();
            frameList = new HashMap<>();
        }
        return singleton;
    }
}
