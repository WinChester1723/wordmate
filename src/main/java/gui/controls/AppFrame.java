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

public final class AppFrame extends JFrame{
    private BufferedImage translateUIIcon = null;
    private TrayIcon translateUITrayIcon = null;
    public AppFrame(String frameTitle, JPanel childPanel) {
        super(frameTitle);

        setUndecorated(true);
        add(new OutsidePanel(this, childPanel));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public AppFrame getAppFrame(){
        return this;
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

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(this, "App Tray icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

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

        if (!(new File(Constants.APP_TRAY_ICON_PATH).exists())) {
            JOptionPane.showMessageDialog(this, "App icon could not be found!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            translateUIIcon = ImageIO.read(file);
            this.setIconImage(translateUIIcon);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final class BorderPanel extends JPanel {

        private JLabel label = null;
        private int pointX = 0, pointY = 0;

        public BorderPanel(AppFrame parentFrame) {
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

                    parentFrame.setLocation(parentFrame.getLocation().x + me.getX() - pointX,
                            parentFrame.getLocation().y + me.getY() - pointY);
                }
            });

            addMouseMotionListener(new MouseMotionAdapter() {
                public void mouseDragged(MouseEvent me) {

                    parentFrame.setLocation(parentFrame.getLocation().x + me.getX() - pointX,
                            parentFrame.getLocation().y + me.getY() - pointY);
                }
            });
        }
    }

    private final class OutsidePanel extends JPanel {
        public OutsidePanel(AppFrame parentFrame, JPanel childPanel) {
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