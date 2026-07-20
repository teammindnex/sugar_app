package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.WorkerDAO;
import com.sugarcane.erp.model.Worker;

import java.sql.SQLException;
import java.util.List;

public class WorkerService {

    private final WorkerDAO workerDAO;

    public WorkerService() {
        this.workerDAO = new WorkerDAO();
    }

    public void addWorker(Worker worker) throws SQLException {
        workerDAO.addWorker(worker);
    }

    public List<Worker> getAllWorkers() throws SQLException {
        return workerDAO.getAllWorkers();
    }

    public void updateWorker(Worker worker) throws SQLException {
        workerDAO.updateWorker(worker);
    }

    public void deleteWorker(int id) throws SQLException {
        workerDAO.deleteWorker(id);
    }
}
