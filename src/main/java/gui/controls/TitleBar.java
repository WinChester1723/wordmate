package gui.controls;

import com.formdev.flatlaf.FlatDarkLaf;
import utils.Constants;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static gui.controls.TitleBar.FrameType.*;

public final class TitleBar<E>{
    enum FrameType{
        FT_NULL,
        FT_JFRAME,
        FT_JDIALOG
    }
    FrameType frameType = FT_NULL;
    private E attachedObject = null;
    private JPanel childPanel = null;

    private BufferedImage translateUIIcon = null;
    private TrayIcon translateUITrayIcon = null;
    public TitleBar(E parentType, JPanel childPanel) {
        if(parentType instanceof JFrame){
            frameType = FT_JFRAME;
        }else if(parentType instanceof JDialog){
            frameType = FT_JDIALOG;
        }else{
            throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",parentType.getClass().getName()));
        }
        attachedObject = parentType;
        this.childPanel = childPanel;
        Initialize();
    }
    public void Initialize(){
        if(frameType.equals(FT_JFRAME)){
            ((JFrame)attachedObject).setUndecorated(true);
            ((JFrame)attachedObject).add(new OutsidePanel(this, childPanel));
            ((JFrame)attachedObject).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ((JFrame)attachedObject).pack();
            ((JFrame)attachedObject).setLocationRelativeTo(null);
            ((JFrame)attachedObject).setVisible(true);
        }else if(frameType.equals(FT_JDIALOG)){
            ((JDialog)attachedObject).setUndecorated(true);
            ((JDialog)attachedObject).add(new OutsidePanel(this, childPanel));
            ((JDialog)attachedObject).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ((JDialog)attachedObject).pack();
            ((JDialog)attachedObject).setLocationRelativeTo(null);
            ((JDialog)attachedObject).setVisible(true);
        }else{
            throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",attachedObject.getClass().getName()));
        }
    }

    public E getAppFrame(){
        return attachedObject;
    }

    public BufferedImage getIcon(){
        return translateUIIcon;
    }

    public TrayIcon getTrayIcon(){
        return translateUITrayIcon;
    }

    private void addTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        // TODO: Enable
//        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
//            JOptionPane.showMessageDialog(this, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        Image image = Toolkit.getDefaultToolkit().getImage(Constants.APP_TRAY_ICON_PATH);
        final PopupMenu popup = new PopupMenu();
        translateUITrayIcon = new TrayIcon(image, Constants.APP_NAME, popup);
        translateUITrayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(1));
        popup.add(exitItem);

        translateUITrayIcon.setPopupMenu(popup);

        try {
            tray.add(translateUITrayIcon);
        } catch (AWTException e) {
            System.out.println("TrayIcon could not be added.");
        }
    }

    private void setIcon() {
        File file = new File(Constants.APP_ICON_PATH);

//        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
//            JOptionPane.showMessageDialog(this, "App icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
//            return;
//        }

        try {
            translateUIIcon = ImageIO.read(file);

            if(frameType.equals(FT_JFRAME))
                ((JFrame)attachedObject).setIconImage(translateUIIcon);
            else if (frameType.equals(FT_JDIALOG))
                ((JDialog)attachedObject).setIconImage(translateUIIcon);
            else
                throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",attachedObject.getClass().getName()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final class BorderPanel extends JPanel {

        private JLabel label = null;
        private int pointX = 0, pointY = 0;

        public BorderPanel(TitleBar parentFrame) {
            label = new JLabel(" X ");

            setLayout(new FlowLayout(FlowLayout.RIGHT));
            add(label);

            label.addMouseListener(new MouseAdapter() {
                public void mouseReleased(MouseEvent e) {
                    System.exit(0);
                }
            });
            addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent me) {
                    // Get x,y and store them
                    pointX = me.getX();
                    pointY = me.getY();

                }

                public void mouseDragged(MouseEvent me) {

                    if (frameType.equals(FT_JFRAME))
                    ((JFrame)parentFrame.attachedObject).setLocation(((JFrame)parentFrame.attachedObject).getLocation().x + me.getX() - pointX,
                            ((JFrame)parentFrame.attachedObject).getLocation().y + me.getY() - pointY);
                    else if(frameType.equals(FT_JDIALOG))
                        ((JDialog)parentFrame.attachedObject).setLocation(((JDialog)parentFrame.attachedObject).getLocation().x + me.getX() - pointX,
                                ((JDialog)parentFrame.attachedObject).getLocation().y + me.getY() - pointY);
                    else
                        throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",attachedObject.getClass().getName()));
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent me) {
                    if (frameType.equals(FT_JFRAME))
                        ((JFrame)parentFrame.attachedObject).setLocation(((JFrame)parentFrame.attachedObject).getLocation().x + me.getX() - pointX,
                                ((JFrame)parentFrame.attachedObject).getLocation().y + me.getY() - pointY);
                    else if(frameType.equals(FT_JDIALOG))
                        ((JDialog)parentFrame.attachedObject).setLocation(((JDialog)parentFrame.attachedObject).getLocation().x + me.getX() - pointX,
                                ((JDialog)parentFrame.attachedObject).getLocation().y + me.getY() - pointY);
                    else
                        throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",attachedObject.getClass().getName()));
                }
            });
        }
    }

    private final class OutsidePanel extends JPanel {
        public OutsidePanel(TitleBar parentFrame, JPanel childPanel) {
            setLayout(new BorderLayout());
            add(childPanel, BorderLayout.CENTER);
            add(new BorderPanel(parentFrame), BorderLayout.PAGE_START);

            try {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } catch (UnsupportedLookAndFeelException e) {
                throw new RuntimeException(e);
            }

        }
    }
}