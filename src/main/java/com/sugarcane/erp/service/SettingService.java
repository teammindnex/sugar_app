package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.SettingDAO;

import java.sql.SQLException;

public class SettingService {

    private final SettingDAO settingDAO;

    public SettingService() {
        this.settingDAO = new SettingDAO();
    }

    public String getSetting(String key) throws SQLException {
        return settingDAO.getSetting(key);
    }

    public void updateSetting(String key, String value) throws SQLException {
        settingDAO.updateSetting(key, value);
    }
}
