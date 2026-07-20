package com.sugarcane.erp.service;

import com.sugarcane.erp.model.Farmer;
import com.sugarcane.erp.model.Customer;
import com.sugarcane.erp.model.LedgerEntry;
import com.sugarcane.erp.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LedgerService {

    public List<LedgerEntry> getFarmerLedger(Farmer farmer) throws SQLException {
        List<LedgerEntry> entries = new ArrayList<>();
        
        // Add Opening Balance
        entries.add(new LedgerEntry(
            LocalDate.now().minusYears(10), // Treat opening balance as oldest
            "Opening Balance",
            farmer.getOpeningBalance() > 0 ? farmer.getOpeningBalance() : 0,
            farmer.getOpeningBalance() < 0 ? Math.abs(farmer.getOpeningBalance()) : 0
        ));
        
        String sqlPurchases = "SELECT purchase_date, net_amount FROM Sugarcane_Purchases WHERE farmer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlPurchases)) {
            pstmt.setInt(1, farmer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                entries.add(new LedgerEntry(
                    rs.getDate("purchase_date").toLocalDate(),
                    "Sugarcane Purchase",
                    rs.getDouble("net_amount"), // We owe farmer -> Debit (Increase balance)
                    0
                ));
            }
        }
        
        String sqlPayments = "SELECT payment_date, amount FROM Farmer_Payments WHERE farmer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlPayments)) {
            pstmt.setInt(1, farmer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                entries.add(new LedgerEntry(
                    rs.getDate("payment_date").toLocalDate(),
                    "Payment Given",
                    0,
                    rs.getDouble("amount") // We paid farmer -> Credit (Decrease balance)
                ));
            }
        }
        
        // Sort by Date
        entries.sort(Comparator.comparing(LedgerEntry::getDate));
        
        // Calculate Running Balance
        double balance = 0;
        for (LedgerEntry e : entries) {
            balance = balance + e.getDebit() - e.getCredit();
            e.setBalance(balance);
        }
        
        return entries;
    }

    public List<LedgerEntry> getCustomerLedger(Customer customer) throws SQLException {
        List<LedgerEntry> entries = new ArrayList<>();
        
        // Add Opening Balance
        entries.add(new LedgerEntry(
            LocalDate.now().minusYears(10), // Treat opening balance as oldest
            "Opening Balance",
            customer.getOpeningBalance() > 0 ? customer.getOpeningBalance() : 0,
            customer.getOpeningBalance() < 0 ? Math.abs(customer.getOpeningBalance()) : 0
        ));
        
        String sqlSales = "SELECT sale_date, total_amount, received_amount FROM Sugarcane_Sales WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSales)) {
            pstmt.setInt(1, customer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                double total = rs.getDouble("total_amount");
                double rec = rs.getDouble("received_amount");
                
                // Debit what they owe us for the sale
                entries.add(new LedgerEntry(
                    rs.getDate("sale_date").toLocalDate(),
                    "Sugarcane Sale",
                    total, // They owe us -> Debit
                    0
                ));
                
                // If they paid on the spot (received_amount)
                if (rec > 0) {
                    entries.add(new LedgerEntry(
                        rs.getDate("sale_date").toLocalDate(),
                        "Payment Received (On Sale)",
                        0,
                        rec // They paid us -> Credit
                    ));
                }
            }
        }
        
        String sqlCol = "SELECT collection_date, amount FROM Customer_Collections WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCol)) {
            pstmt.setInt(1, customer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                entries.add(new LedgerEntry(
                    rs.getDate("collection_date").toLocalDate(),
                    "Collection Received",
                    0,
                    rs.getDouble("amount") // They paid us -> Credit
                ));
            }
        }
        
        // Sort by Date
        entries.sort(Comparator.comparing(LedgerEntry::getDate));
        
        // Calculate Running Balance
        double balance = 0;
        for (LedgerEntry e : entries) {
            balance = balance + e.getDebit() - e.getCredit();
            e.setBalance(balance);
        }
        
        return entries;
    }
}
