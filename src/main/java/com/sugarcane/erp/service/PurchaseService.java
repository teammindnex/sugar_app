package com.sugarcane.erp.service;

import com.sugarcane.erp.dao.PurchaseDAO;
import com.sugarcane.erp.model.Purchase;

import java.sql.SQLException;
import java.util.List;

public class PurchaseService {

    private final PurchaseDAO purchaseDAO;

    public PurchaseService() {
        this.purchaseDAO = new PurchaseDAO();
    }

    public void addPurchase(Purchase purchase) throws SQLException {
        // Here we could add business logic to also update farmer's ledger automatically
        // For now, just insert the purchase. Ledger calculations can be done via views or service calls.
        purchaseDAO.addPurchase(purchase);
    }

    public List<Purchase> getAllPurchases() throws SQLException {
        return purchaseDAO.getAllPurchases();
    }

    public void deletePurchase(int id) throws SQLException {
        purchaseDAO.deletePurchase(id);
    }
}
