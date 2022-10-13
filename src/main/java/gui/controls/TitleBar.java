package gui.controls;

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
        FT_JFRAME,
        FT_JDIALOG
    }
    FrameType frameType;
    private E attachedObject = null;
    private final JPanel childPanel;


    // Only JFrame can use frame and tray icon
    private BufferedImage frameIcon = null;
    private TrayIcon frameTrayIcon = null;


    public TitleBar(Class<E> parentType, JPanel childPanel) {
        if(parentType == JFrame.class){
            frameType = FT_JFRAME;
        }else if(parentType == JDialog.class){
            frameType = FT_JDIALOG;
        }else{
            throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",parentType.getClass().getName()));
        }
        this.childPanel = childPanel;
    }

    public void Initialize(){
        if(frameType.equals(FT_JFRAME)){
            attachedObject = ((E)new JFrame());
            ((JFrame)attachedObject).setUndecorated(true);
            ((JFrame)attachedObject).add(new OutsidePanel(this, childPanel));
            ((JFrame)attachedObject).setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ((JFrame)attachedObject).pack();
            ((JFrame)attachedObject).setLocationRelativeTo(null);
            ((JFrame)attachedObject).setVisible(true);
        }else if(frameType.equals(FT_JDIALOG)){
            attachedObject = ((E)new JDialog());
            ((JDialog)attachedObject).setUndecorated(true);
            ((JDialog)attachedObject).add(new OutsidePanel(this, childPanel));
            ((JDialog)attachedObject).setModal(true);
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
        return frameIcon;
    }

    public TrayIcon getFrameTrayIcon(){
        return frameTrayIcon;
    }

    private void addTrayIcon() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(null, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Image image = Toolkit.getDefaultToolkit().getImage(Constants.APP_TRAY_ICON_PATH);
        final PopupMenu popup = new PopupMenu();
        frameTrayIcon = new TrayIcon(image, Constants.APP_NAME, popup);
        frameTrayIcon.setImageAutoSize(true);
        final SystemTray tray = SystemTray.getSystemTray();

        MenuItem exitItem = new MenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(1));
        popup.add(exitItem);

        frameTrayIcon.setPopupMenu(popup);

        try {
            tray.add(frameTrayIcon);
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
            frameIcon = ImageIO.read(file);

            if(frameType.equals(FT_JFRAME))
                ((JFrame)attachedObject).setIconImage(frameIcon);
            else if (frameType.equals(FT_JDIALOG))
                ((JDialog)attachedObject).setIconImage(frameIcon);
            else
                throw new IllegalArgumentException(String.format("%s is not an acceptable parameter!",attachedObject.getClass().getName()));

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final class BorderPanel extends JPanel {

        private int pointX = 0, pointY = 0;

        public BorderPanel(TitleBar parentFrame) {
            JLabel label = new JLabel(" X ");

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
        }
    }
}