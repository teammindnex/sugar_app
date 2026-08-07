package com.sugarcane.erp.controller;

import com.sugarcane.erp.model.DailyReportItem;
import com.sugarcane.erp.service.ReportService;
import com.sugarcane.erp.utils.ExcelExporter;
import com.sugarcane.erp.utils.PdfReportExporter;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportController {

    @FXML private ComboBox<String> periodComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private TableView<DailyReportItem> reportTable;
    @FXML private TableColumn<DailyReportItem, LocalDate> colDate;

    private ReportService reportService;
    private ObservableList<DailyReportItem> reportList;

    @FXML
    public void initialize() {
        reportService = new ReportService();
        reportList = FXCollections.observableArrayList();
        reportTable.setItems(reportList);

        periodComboBox.getItems().addAll("चालू महिना (This Month)", "मागील महिना (Last Month)", "मागील २ महिने (Last 2 Months)", "चालू वर्ष (This Year)", "कस्टम (Custom)");
        periodComboBox.getSelectionModel().select(0);

        periodComboBox.setOnAction(e -> handlePeriodChange());
        
        // Setup date formatter for the table column
        colDate.setCellFactory(column -> new TableCell<DailyReportItem, LocalDate>() {
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
                }
            }
        });

        handlePeriodChange();
        loadReports();
    }

    private void handlePeriodChange() {
        String selected = periodComboBox.getValue();
        LocalDate now = LocalDate.now();
        if (selected == null) return;

        switch (selected) {
            case "चालू महिना (This Month)":
                startDatePicker.setValue(now.withDayOfMonth(1));
                endDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
                break;
            case "मागील महिना (Last Month)":
                LocalDate lastMonth = now.minusMonths(1);
                startDatePicker.setValue(lastMonth.withDayOfMonth(1));
                endDatePicker.setValue(lastMonth.withDayOfMonth(lastMonth.lengthOfMonth()));
                break;
            case "मागील २ महिने (Last 2 Months)":
                LocalDate twoMonthsAgo = now.minusMonths(2);
                startDatePicker.setValue(twoMonthsAgo.withDayOfMonth(1));
                endDatePicker.setValue(now.withDayOfMonth(now.lengthOfMonth()));
                break;
            case "चालू वर्ष (This Year)":
                startDatePicker.setValue(now.withDayOfYear(1));
                endDatePicker.setValue(now.withDayOfYear(now.lengthOfYear()));
                break;
        }
    }

    @FXML
    private void loadReports() {
        LocalDate start = startDatePicker.getValue();
        LocalDate end = endDatePicker.getValue();

        if (start == null || end == null) {
            showAlert("कृपया सुरुवातीची आणि शेवटची तारीख निवडा.");
            return;
        }

        try {
            List<DailyReportItem> data = reportService.getDailyBuySellReport(start, end);
            reportList.setAll(data);
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert("डेटा लोड करताना त्रुटी: " + e.getMessage());
        }
    }

    @FXML
    private void exportToPdf() {
        if (reportList.isEmpty()) {
            showAlert("रिपोर्टमध्ये डेटा उपलब्ध नाही!");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save PDF Report");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        fileChooser.setInitialFileName("Daily_Report.pdf");

        File file = fileChooser.showSaveDialog(reportTable.getScene().getWindow());
        if (file != null) {
            try {
                String title = "रोजचा अहवाल (Daily Report) - " + startDatePicker.getValue() + " ते " + endDatePicker.getValue();
                PdfReportExporter.generateReportPdf(file, reportList, title);
                showAlert("PDF यशस्वीरित्या जतन केली!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert("PDF तयार करताना त्रुटी: " + e.getMessage());
            }
        }
    }

    @FXML
    private void exportToExcel() {
        if (reportList.isEmpty()) {
            showAlert("रिपोर्टमध्ये डेटा उपलब्ध नाही!");
            return;
        }
        ExcelExporter.exportTableToExcel(reportTable, "Daily_Report", reportTable.getScene().getWindow());
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("माहिती");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
