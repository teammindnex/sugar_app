package com.sugarcane.erp.utils;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Locale;
import java.util.ResourceBundle;
import java.text.MessageFormat;

public class LanguageManager {

    private static final ObjectProperty<Locale> locale;
    private static ResourceBundle bundle;

    static {
        // Load default language from DB in the future, for now English
        locale = new SimpleObjectProperty<>(new Locale("en"));
        locale.addListener((observable, oldValue, newValue) -> loadResourceBundle());
        loadResourceBundle();
    }

    private static void loadResourceBundle() {
        bundle = ResourceBundle.getBundle("i18n.messages", locale.get());
    }

    public static void setLocale(Locale newLocale) {
        locale.set(newLocale);
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static ObjectProperty<Locale> localeProperty() {
        return locale;
    }

    public static String get(String key, Object... args) {
        try {
            String value = bundle.getString(key);
            if (args.length > 0) {
                return MessageFormat.format(value, args);
            }
            return value;
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }

    public static StringBinding createStringBinding(final String key, Object... args) {
        return Bindings.createStringBinding(() -> get(key, args), locale);
    }
}
