package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.DashboardDAO;
import com.sugarcane.erp.model.DashboardMetrics;

import java.sql.SQLException;
import java.time.LocalDate;

public class DashboardService {
    
    private final DashboardDAO dashboardDAO;

    public DashboardService() {
        this.dashboardDAO = new DashboardDAO();
    }

    public DashboardMetrics getMetrics(LocalDate selectedDate) throws SQLException {
        return dashboardDAO.getMetrics(selectedDate);
    }
}
