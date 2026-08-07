package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.ReportDAO;
import com.sugarcane.erp.model.DailyReportItem;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {
    
    private final ReportDAO reportDAO;

    public ReportService() {
        this.reportDAO = new ReportDAO();
    }

    public List<DailyReportItem> getDailyBuySellReport(LocalDate startDate, LocalDate endDate) throws SQLException {
        return reportDAO.getDailyBuySellReport(startDate, endDate);
    }
}
