package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.WorkerDailyEntryDAO;
import com.sugarcane.erp.model.WorkerDailyEntry;

import java.sql.SQLException;
import java.util.List;

public class WorkerDailyEntryService {

    private final WorkerDailyEntryDAO entryDAO;

    public WorkerDailyEntryService() {
        this.entryDAO = new WorkerDailyEntryDAO();
    }

    public void addEntry(WorkerDailyEntry entry) throws SQLException {
        entryDAO.addEntry(entry);
    }

    public List<WorkerDailyEntry> getAllEntries() throws SQLException {
        return entryDAO.getAllEntries();
    }

    public void deleteEntry(int id) throws SQLException {
        entryDAO.deleteEntry(id);
    }
}
