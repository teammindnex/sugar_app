package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.FarmerDAO;
import com.sugarcane.erp.model.Farmer;

import java.sql.SQLException;
import java.util.List;

public class FarmerService {

    private final FarmerDAO farmerDAO;

    public FarmerService() {
        this.farmerDAO = new FarmerDAO();
    }

    public void addFarmer(Farmer farmer) throws SQLException {
        farmerDAO.addFarmer(farmer);
    }

    public List<Farmer> getAllFarmers() throws SQLException {
        return farmerDAO.getAllFarmers();
    }

    public void updateFarmer(Farmer farmer) throws SQLException {
        farmerDAO.updateFarmer(farmer);
    }

    public void deleteFarmer(int id) throws SQLException {
        farmerDAO.deleteFarmer(id);
    }
}
