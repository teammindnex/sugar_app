package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.TransportTripDAO;
import com.sugarcane.erp.model.TransportTrip;

import java.sql.SQLException;
import java.util.List;

public class TransportTripService {

    private final TransportTripDAO tripDAO;

    public TransportTripService() {
        this.tripDAO = new TransportTripDAO();
    }

    public void addTrip(TransportTrip trip) throws SQLException {
        tripDAO.addTrip(trip);
    }

    public List<TransportTrip> getAllTrips() throws SQLException {
        return tripDAO.getAllTrips();
    }

    public void deleteTrip(int id) throws SQLException {
        tripDAO.deleteTrip(id);
    }
}
