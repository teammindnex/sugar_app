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

    public List<LedgerEntry> getFarmerLedger(Farmer farmer, LocalDate startDate, LocalDate endDate) throws SQLException {
        List<LedgerEntry> allEntries = new ArrayList<>();
        
        String sqlPurchases = "SELECT id, bill_no, purchase_date, net_amount, cane_type, empty_weight, loaded_weight, weight, created_at FROM Sugarcane_Purchases WHERE farmer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlPurchases)) {
            pstmt.setInt(1, farmer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String bNo = rs.getString("bill_no");
                if (bNo == null || bNo.trim().isEmpty()) {
                    bNo = String.valueOf(rs.getInt("id"));
                }
                String createdAt = rs.getString("created_at");
                allEntries.add(new LedgerEntry(
                    rs.getDate("purchase_date").toLocalDate(),
                    bNo,
                    "ऊस खरेदी",
                    rs.getString("cane_type"),
                    rs.getDouble("empty_weight"),
                    rs.getDouble("loaded_weight"),
                    rs.getDouble("weight"),
                    rs.getDouble("net_amount"), // DEBIT (Purchase = Rakkam)
                    0, // CREDIT
                    createdAt != null ? createdAt : ""
                ));
            }
        }
        
        String sqlPayments = "SELECT id, payment_date, amount, payment_mode, ref_no, created_at FROM Farmer_Payments WHERE farmer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlPayments)) {
            pstmt.setInt(1, farmer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String mode = rs.getString("payment_mode");
                String refNo = rs.getString("ref_no");
                String desc = "पेमेंट";
                if (mode != null && !mode.trim().isEmpty()) {
                    desc += " - " + mode;
                }
                if (refNo != null && !refNo.trim().isEmpty()) {
                    desc += " (" + refNo + ")";
                }
                String createdAt = rs.getString("created_at");
                allEntries.add(new LedgerEntry(
                    rs.getDate("payment_date").toLocalDate(),
                    String.valueOf(rs.getInt("id")),
                    desc,
                    "", 0, 0, 0, 
                    0, // DEBIT
                    rs.getDouble("amount"), // CREDIT (Payment = Jama)
                    createdAt != null ? createdAt : ""
                ));
            }
        }
        
        // Sort by Date, and if same date, by exact chronological order as entered (created_at)
        allEntries.sort((e1, e2) -> {
            int dateCmp = e1.getDate().compareTo(e2.getDate());
            if (dateCmp != 0) return dateCmp;
            
            String c1 = e1.getCreatedAt() != null ? e1.getCreatedAt() : "";
            String c2 = e2.getCreatedAt() != null ? e2.getCreatedAt() : "";
            if (!c1.isEmpty() && !c2.isEmpty()) {
                return c1.compareTo(c2);
            }
            return 0;
        });
        
        double carryForwardBalance = farmer.getOpeningBalance(); // Start with initial opening balance
        List<LedgerEntry> filtered = new ArrayList<>();
        
        for (LedgerEntry entry : allEntries) {
            LocalDate d = entry.getDate();
            if (startDate != null && d.isBefore(startDate)) {
                carryForwardBalance += entry.getDebit() - entry.getCredit();
            } else if (endDate != null && d.isAfter(endDate)) {
                // ignore
            } else {
                filtered.add(entry);
            }
        }
        
        List<LedgerEntry> finalList = new ArrayList<>();
        // Always add the opening balance for the period
        finalList.add(new LedgerEntry(
            LocalDate.now().minusYears(10), 
            "मागील बाकी", 
            "",
            0.0,
            carryForwardBalance < 0 ? Math.abs(carryForwardBalance) : 0, // DEBIT if negative
            carryForwardBalance > 0 ? carryForwardBalance : 0  // CREDIT if positive
        ));
        finalList.addAll(filtered);
        
        double balance = 0;
        for (LedgerEntry e : finalList) {
            // For Farmer: Debit increases balance, Credit decreases balance (based on user Khatawani preference)
            balance = balance + e.getDebit() - e.getCredit();
            e.setBalance(balance);
        }
        return finalList;
    }

    public List<LedgerEntry> getFarmerLedger(Farmer farmer) throws SQLException {
        return getFarmerLedger(farmer, null, null);
    }

    public List<LedgerEntry> getCustomerLedger(Customer customer) throws SQLException {
        List<LedgerEntry> entries = new ArrayList<>();
        
        // Add Opening Balance
        entries.add(new LedgerEntry(
            LocalDate.now().minusYears(10), // Treat opening balance as oldest
            "Opening Balance",
            "",
            0.0,
            customer.getOpeningBalance() > 0 ? customer.getOpeningBalance() : 0,
            customer.getOpeningBalance() < 0 ? Math.abs(customer.getOpeningBalance()) : 0
        ));
        
        String sqlSales = "SELECT sale_date, total_amount, received_amount, created_at FROM Sugarcane_Sales WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlSales)) {
            pstmt.setInt(1, customer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                double total = rs.getDouble("total_amount");
                double rec = rs.getDouble("received_amount");
                String createdAt = rs.getString("created_at");
                
                // Debit what they owe us for the sale
                entries.add(new LedgerEntry(
                    rs.getDate("sale_date").toLocalDate(),
                    "",
                    "Sugarcane Sale",
                    "",
                    0.0, 0.0, 0.0,
                    total, // They owe us -> Debit
                    0,
                    createdAt != null ? createdAt : ""
                ));
                
                // If they paid on the spot (received_amount)
                if (rec > 0) {
                    entries.add(new LedgerEntry(
                        rs.getDate("sale_date").toLocalDate(),
                        "",
                        "Payment Received (On Sale)",
                        "",
                        0.0, 0.0, 0.0,
                        0,
                        rec, // They paid us -> Credit
                        createdAt != null ? createdAt : ""
                    ));
                }
            }
        }
        
        String sqlCol = "SELECT collection_date, amount, created_at FROM Customer_Collections WHERE customer_id = ?";
        try (Connection conn = DatabaseManager.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlCol)) {
            pstmt.setInt(1, customer.getId());
            ResultSet rs = pstmt.executeQuery();
            while(rs.next()) {
                String createdAt = rs.getString("created_at");
                entries.add(new LedgerEntry(
                    rs.getDate("collection_date").toLocalDate(),
                    "",
                    "Collection Received",
                    "",
                    0.0, 0.0, 0.0,
                    0,
                    rs.getDouble("amount"), // They paid us -> Credit
                    createdAt != null ? createdAt : ""
                ));
            }
        }
        
        // Sort by Date and exact creation order (created_at)
        entries.sort((e1, e2) -> {
            int dateCmp = e1.getDate().compareTo(e2.getDate());
            if (dateCmp != 0) return dateCmp;
            String c1 = e1.getCreatedAt() != null ? e1.getCreatedAt() : "";
            String c2 = e2.getCreatedAt() != null ? e2.getCreatedAt() : "";
            if (!c1.isEmpty() && !c2.isEmpty()) {
                return c1.compareTo(c2);
            }
            return 0;
        });
        
        // Calculate Running Balance
        double balance = 0;
        for (LedgerEntry e : entries) {
            balance = balance + e.getDebit() - e.getCredit();
            e.setBalance(balance);
        }
        
        return entries;
    }
}
