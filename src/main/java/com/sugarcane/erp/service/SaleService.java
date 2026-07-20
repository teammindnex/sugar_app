package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.SaleDAO;
import com.sugarcane.erp.model.Sale;

import java.sql.SQLException;
import java.util.List;

public class SaleService {

    private final SaleDAO saleDAO;

    public SaleService() {
        this.saleDAO = new SaleDAO();
    }

    public void addSale(Sale sale) throws SQLException {
        saleDAO.addSale(sale);
    }

    public List<Sale> getAllSales() throws SQLException {
        return saleDAO.getAllSales();
    }

    public void deleteSale(int id) throws SQLException {
        saleDAO.deleteSale(id);
    }
}
