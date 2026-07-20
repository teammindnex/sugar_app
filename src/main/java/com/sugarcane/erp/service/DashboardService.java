package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.DashboardDAO;
import com.sugarcane.erp.model.DashboardMetrics;

import java.sql.SQLException;

public class DashboardService {
    
    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    public DashboardMetrics getMetrics() throws SQLException {
        return dashboardDAO.getMetrics();
    }
}
