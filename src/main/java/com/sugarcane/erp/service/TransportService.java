package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.TransportDAO;
import com.sugarcane.erp.model.Transport;

import java.sql.SQLException;
import java.util.List;

public class TransportService {

    private final TransportDAO transportDAO;

    public TransportService() {
        this.transportDAO = new TransportDAO();
    }

    public void addTransport(Transport transport) throws SQLException {
        transportDAO.addTransport(transport);
    }

    public List<Transport> getAllTransports() throws SQLException {
        return transportDAO.getAllTransports();
    }

    public void updateTransport(Transport transport) throws SQLException {
        transportDAO.updateTransport(transport);
    }

    public void deleteTransport(int id) throws SQLException {
        transportDAO.deleteTransport(id);
    }
}
