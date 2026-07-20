package com.sugarcane.erp.utils;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;

public class ThemeManager {

    public static final String LIGHT_THEME = "light";
    public static final String DARK_THEME = "dark";
    
    private static final StringProperty currentTheme = new SimpleStringProperty(LIGHT_THEME);
    
    public static void applyTheme(Scene scene) {
        scene.getStylesheets().clear();
        String cssFile = currentTheme.get().equals(DARK_THEME) ? "/css/dark-theme.css" : "/css/light-theme.css";
        scene.getStylesheets().add(ThemeManager.class.getResource(cssFile).toExternalForm());
    }
    
    public static void setTheme(String theme) {
        currentTheme.set(theme);
    }
    
    public static String getTheme() {
        return currentTheme.get();
    }
    
    public static StringProperty themeProperty() {
        return currentTheme;
    }
}
