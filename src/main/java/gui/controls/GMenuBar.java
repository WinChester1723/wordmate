package gui.controls;

import gui.panels.dialogs.AppearanceDialog;
import gui.utils.icons.ApplicationIcons;
import gui.utils.icons.IconManager;

import javax.swing.*;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GMenuBar extends JMenuBar {
    final String settingsMenuName = "Settings";
    final String preferencesMenuName = "Preferences";

    Map<String, JMenu> menus = null;
    Map<String, JMenuItem> menu_items = null;
    private ImageIcon settingsIcon = null;
    private ImageIcon preferencesIcon = null;

    public GMenuBar() {
        settingsIcon = IconManager.getInstance().getIcon(ApplicationIcons.ICON_MENU_OPTIONS);
        preferencesIcon = IconManager.getInstance().getIcon(ApplicationIcons.ICON_MENU_THEME);

        InitMenus();

        for (var i : menus.entrySet())
            this.add(i.getValue());
    }

    private void InitMenus() {
        menus = new LinkedHashMap<>();
        menu_items = new LinkedHashMap<>();

        menu_items.put(settingsMenuName + "Account", new JMenuItem("Account"));
        menu_items.put(settingsMenuName + "History", new JMenuItem("History"));
        menu_items.put(settingsMenuName + "Shortcuts", new JMenuItem("Shortcuts"));

        menu_items.put(preferencesMenuName + "Themes", new JMenuItem("Themes"));
        menu_items.put(preferencesMenuName + "Voices", new JMenuItem("Voices"));
        menu_items.put(preferencesMenuName + "Favorites", new JMenuItem("Favorites"));
        menu_items.put(preferencesMenuName + "Contribute", new JMenuItem("Contribute"));

        menus.put(settingsMenuName, new JMenu(settingsMenuName));
        menus.get(settingsMenuName).setIcon(settingsIcon);
        for (var i : menu_items.entrySet()) {
            i.getValue().addActionListener(e -> {onMenuItemClick(i.getKey());});
            if (i.getKey().contains(settingsMenuName)) menus.get(settingsMenuName).add(i.getValue());
        }
        menus.put(preferencesMenuName, new JMenu(preferencesMenuName));
        menus.get(preferencesMenuName).setIcon(preferencesIcon);
        for (var i : menu_items.entrySet())
            if (i.getKey().contains(preferencesMenuName)) menus.get(preferencesMenuName).add(i.getValue());
    }

    private void onMenuItemClick(String itemId){
        if(itemId.contains("Themes")){
            new AppearanceDialog();
        }
    }
}

