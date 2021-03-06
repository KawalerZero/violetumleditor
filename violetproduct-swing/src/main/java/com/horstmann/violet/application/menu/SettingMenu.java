/*
 Violet - A program for editing UML diagrams.

 Copyright (C) 2007 Cay S. Horstmann (http://horstmann.com)
 Alexandre de Pellegrin (http://alexdp.free.fr);

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.horstmann.violet.application.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import javax.swing.*;

import com.horstmann.violet.application.gui.*;
import com.horstmann.violet.framework.injection.bean.ManiocFramework;
import com.horstmann.violet.framework.injection.resources.ResourceBundleInjector;
import com.horstmann.violet.framework.injection.resources.annotation.ResourceBundleBean;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.InjectedBean;
import com.horstmann.violet.framework.injection.bean.ManiocFramework.BeanInjector;
import com.horstmann.violet.framework.language.Language;
import com.horstmann.violet.framework.language.LanguageManager;
import com.horstmann.violet.framework.userpreferences.UserPreferencesService;

/**
 * Represents the setting menu on the editor frame
 */
@ResourceBundleBean(resourceReference = MenuFactory.class)
public class SettingMenu extends JMenu
{

    @ResourceBundleBean(key = "setting")
    public SettingMenu(final MainFrame mainFrame)
    {
        BeanInjector.getInjector().inject(this);
        ResourceBundleInjector.getInjector().inject(this);
        this.mainFrame = mainFrame;
        this.createMenu();
    }

    /**
     * Initialize the menu
     */
    private void createMenu()
    {
        ButtonGroup group = new ButtonGroup();
        settingItemMenuLanguage.setIcon(languageIcon);
        for (final Language language : languageManager.getLanguages())
        {
            JCheckBoxMenuItem menuLangSelect = new JCheckBoxMenuItem(language.getName());
            group.add(menuLangSelect);

            if (language.getShortcut().equals(Locale.getDefault().toString()))
            {
                menuLangSelect.setSelected(true);
            }

            settingItemMenuLanguage.add(menuLangSelect);
            menuLangSelect.addActionListener(new ActionListener()
                                             {
                                                 @Override
                                                 public void actionPerformed(ActionEvent e)
                                                 {
                                                     changeLanguage(language.getShortcut());
                                                 }
                                             }
            );

        }

        this.add(settingItemMenuLanguage);
    }

    private void changeLanguage(String languageName)
    {
        languageChangeAlert();
        languageManager.setPreferedLanguage(languageName);
    }

    /**
     * Initialize alert
     */
    private void languageChangeAlert()
    {
        Object[] options = {"OK"};
        JOptionPane.showOptionDialog(null, changeLanguageDialogMessage, changeLanguageDialogTitle,
                JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    }

    /**
     * Application frame
     */
    private MainFrame mainFrame;

    LanguageManager languageManager = new LanguageManager();

    @InjectedBean
    private UserPreferencesService userPreferencesServices;

    @ResourceBundleBean(key = "setting.language")
    private JMenu settingItemMenuLanguage;

    @ResourceBundleBean(key = "dialog.change_laf.title")
    private String changeLanguageDialogTitle;

    @ResourceBundleBean(key = "setting.dialog.change_language")
    private String changeLanguageDialogMessage;

    @ResourceBundleBean(key = "setting.language.active.icon")
    private ImageIcon languageIcon;

}
