package com.sugarcane.erp.utils;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;

public class ExcelExporter {

    public static <T> void exportTableToExcel(TableView<T> table, String sheetName, Window ownerWindow) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Excel File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fileChooser.setInitialFileName(sheetName + ".xlsx");
        
        File file = fileChooser.showSaveDialog(ownerWindow);
        if (file == null) {
            return; // User cancelled
        }

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);
            Row headerRow = sheet.createRow(0);

            // Create Headers
            int colIndex = 0;
            for (TableColumn<T, ?> col : table.getColumns()) {
                Cell cell = headerRow.createCell(colIndex++);
                cell.setCellValue(col.getText());
                
                // Header style
                CellStyle style = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                style.setFont(font);
                cell.setCellStyle(style);
            }

            // Create Data Rows
            int rowIndex = 1;
            for (T item : table.getItems()) {
                Row row = sheet.createRow(rowIndex++);
                colIndex = 0;
                for (TableColumn<T, ?> col : table.getColumns()) {
                    Cell cell = row.createCell(colIndex++);
                    Object cellData = col.getCellData(item);
                    if (cellData != null) {
                        if (cellData instanceof Number) {
                            cell.setCellValue(((Number) cellData).doubleValue());
                        } else {
                            cell.setCellValue(cellData.toString());
                        }
                    } else {
                        cell.setCellValue("");
                    }
                }
            }

            // Auto-size columns
            for (int i = 0; i < table.getColumns().size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }

            // Alert user success
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
            alert.setHeaderText(null);
            alert.setContentText("Exported successfully to " + file.getAbsolutePath());
            alert.showAndWait();

        } catch (Exception e) {
            e.printStackTrace();
            javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
            alert.setHeaderText("Export Failed");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }
}
