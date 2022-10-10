package utils;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.win32.StdCallLibrary;

import java.awt.*;
import java.awt.datatransfer.*;

public final class ClipboardManager implements ClipboardOwner {

    private static ClipboardManager clipboardManager = null;

    public static ClipboardManager getInstance() {
        if (clipboardManager == null) clipboardManager = new ClipboardManager();

        return clipboardManager;
    }

    public void lostOwnership(Clipboard clipboard, Transferable contents) {
        // dummy: needed for `ClipboardOwner`
    }

    void controlC(CustomUser32 customUser32) {
        customUser32.keybd_event((byte) 0x11 /* VK_CONTROL*/, (byte) 0, 0, 0);
        customUser32.keybd_event((byte) 0x43 /* 'C' */, (byte) 0, 0, 0);
        customUser32.keybd_event((byte) 0x43 /* 'C' */, (byte) 0, 2 /* KEYEVENTF_KEYUP */, 0);
        customUser32.keybd_event((byte) 0x11 /* VK_CONTROL*/, (byte) 0, 2 /* KEYEVENTF_KEYUP */, 0);// 'Left Control Up
    }

    public String getClipboardText() throws Exception {
        return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
    }

    public void setClipboardText(String data) throws Exception {
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(data), this);
    }

    public String getSelectedText(User32 user32, CustomUser32 customUser32) throws Exception {
        WinDef.HWND hwnd = customUser32.GetForegroundWindow();
        char[] windowText = new char[512];
        user32.GetWindowText(hwnd, windowText, 512);
        String windowTitle = Native.toString(windowText);
        System.out.println("Will take selected text from the following window: [" + windowTitle + "]");
        String before = getClipboardText();
        controlC(customUser32); // emulate Ctrl C
        Thread.sleep(100); // give it some time
        String text = getClipboardText();
        System.out.println("Currently in clipboard: " + text);
        // restore what was previously in the clipboard
        setClipboardText(before);
        return text;
    }

    public interface CustomUser32 extends StdCallLibrary {
        CustomUser32 INSTANCE = Native.loadLibrary("user32", CustomUser32.class);

        WinDef.HWND GetForegroundWindow();

        void keybd_event(byte bVk, byte bScan, int dwFlags, int dwExtraInfo);
    }
}
