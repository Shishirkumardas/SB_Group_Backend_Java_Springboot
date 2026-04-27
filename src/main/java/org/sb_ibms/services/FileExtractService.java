package org.sb_ibms.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sb_ibms.dto.CashbackDetailsDTO;
import org.springframework.stereotype.Component;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Component
public class FileExtractService {

//    public ByteArrayInputStream generate(List<CashbackDetailsDTO> list) {
//
//        try (Workbook workbook = new XSSFWorkbook()) {
//
//            Sheet sheet = workbook.createSheet("Cashback Due");
//
//            // Header row
//            Row header = sheet.createRow(0);
//            String[] columns = {
//                    "Name",
//                    "Quantity",
//                    "Purchase Date",
//                    "Total Purchase",
//                    "Phone Number",
//                    "Payment Method",
//                    "Monthly Cashback",
//                    "Cashback Start Date",
//                    "Earliest Missed Date",
//                    "Missed Month",
//                    "Missed Amount",
//                    "Next Due Date",
//                    "Status"
//            };
//
//            for (int i = 0; i < columns.length; i++) {
//                header.createCell(i).setCellValue(columns[i]);
//            }
//
//            int rowIdx = 1;
//            for (CashbackDetailsDTO dto : list) {
//                Row row = sheet.createRow(rowIdx++);
//
//                row.createCell(0).setCellValue(dto.getName() != null ? dto.getName() : "N/A");
//
//                row.createCell(1).setCellValue(dto.getQuantity() != null ? dto.getQuantity().toString() : "N/A");
//
//                row.createCell(2).setCellValue(
//                        dto.getPurchaseDate() != null ? dto.getPurchaseDate().toString() : "N/A"
//                );
//
//                row.createCell(3).setCellValue(
//                        dto.getTotalPurchase() != null ? dto.getTotalPurchase().doubleValue() : 0.0
//                );
//
//                row.createCell(4).setCellValue(
//                        dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "N/A"
//                );
//
//                row.createCell(5).setCellValue(
//                        dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "N/A"
//                );
//
//                row.createCell(6).setCellValue(
//                        dto.getExpectedMonthlyCashbackAmount() != null
//                                ? dto.getExpectedMonthlyCashbackAmount().toString()
//                                : "0"
//                );
//
//                row.createCell(7).setCellValue(
//                        dto.getCashbackStartDate() != null ? dto.getCashbackStartDate().toString() : "N/A"
//                );
//
//                row.createCell(8).setCellValue(
//                        dto.getEarliestMissedDueDate() != null
//                                ? dto.getEarliestMissedDueDate().toString()
//                                : "No missed due date"
//                );
//
//                row.createCell(9).setCellValue(
//                        dto.getEarliestDueMonth() != null ? dto.getEarliestDueMonth().toString() : "N/A"
//                );
//
//                row.createCell(10).setCellValue(
//                        dto.getMissedCashbackAmount() != null
//                                ? dto.getMissedCashbackAmount().toString()
//                                : "0"
//                );
//
//                row.createCell(11).setCellValue(
//                        dto.getNextDueDate() != null ? dto.getNextDueDate().toString() : "N/A"
//                );
//
//                row.createCell(12).setCellValue(
//                        dto.getCashbackStatus() != null ? dto.getCashbackStatus() : "N/A"
//                );
//            }
//
//            ByteArrayOutputStream out = new ByteArrayOutputStream();
//            workbook.write(out);
//            return new ByteArrayInputStream(out.toByteArray());
//
//        } catch (IOException e) {
//            throw new RuntimeException("Excel generation failed", e);
//        }
//    }

    private void setStringCell(Row row, int columnIndex, String value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value != null ? value : "N/A");
    }

    private void setNumericCell(Row row, int columnIndex, double value) {
        Cell cell = row.createCell(columnIndex);
        cell.setCellValue(value);
    }

    public ByteArrayInputStream generate(List<CashbackDetailsDTO> list) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Cashback Due");

            // Header Row
            Row header = sheet.createRow(0);
            String[] columns = {
                    "Name", "Quantity", "Purchase Date", "Total Purchase",
                    "Phone Number", "Payment Method", "Monthly Cashback",
                    "Cashback Start Date", "Earliest Missed Date", "Missed Month",
                    "Missed Amount", "Next Due Date", "Status","Area"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
                // Optional: Make header bold
                CellStyle headerStyle = workbook.createCellStyle();
                Font font = workbook.createFont();
                font.setBold(true);
                headerStyle.setFont(font);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (CashbackDetailsDTO dto : list) {
                Row row = sheet.createRow(rowIdx++);

                // 0: Name (String)
                setStringCell(row, 0, dto.getName());

                // 1: Quantity (Number)
                setNumericCell(row, 1, dto.getQuantity() != null ? dto.getQuantity().doubleValue() : 0.0);

                // 2: Purchase Date (String or Date)
                setStringCell(row, 2, dto.getPurchaseDate() != null ? dto.getPurchaseDate().toString() : "N/A");

                // 3: Total Purchase (BigDecimal → Number)
                setNumericCell(row, 3, dto.getTotalPurchase() != null ? dto.getTotalPurchase().doubleValue() : 0.0);

                // 4: Phone Number (String)
                setStringCell(row, 4, dto.getPhoneNumber());

                // 5: Payment Method (String)
                setStringCell(row, 5, dto.getPaymentMethod());

                // 6: Monthly Cashback → FIXED (Now as Number!)
                setNumericCell(row, 6,
                        dto.getExpectedMonthlyCashbackAmount() != null
                                ? dto.getExpectedMonthlyCashbackAmount().doubleValue()
                                : 0.0);

                // 7: Cashback Start Date
                setStringCell(row, 7, dto.getCashbackStartDate() != null ? dto.getCashbackStartDate().toString() : "N/A");

                // 8: Earliest Missed Date
                setStringCell(row, 8,
                        dto.getEarliestMissedDueDate() != null
                                ? dto.getEarliestMissedDueDate().toString()
                                : "No missed due date");

                // 9: Missed Month
                setStringCell(row, 9, dto.getEarliestDueMonth() != null ? dto.getEarliestDueMonth().toString() : "N/A");

                // 10: Missed Amount (Number)
                setNumericCell(row, 10,
                        dto.getMissedCashbackAmount() != null
                                ? dto.getMissedCashbackAmount().doubleValue()
                                : 0.0);

                // 11: Next Due Date
                setStringCell(row, 11, dto.getNextDueDate() != null ? dto.getNextDueDate().toString() : "N/A");

                // 12: Status
                setStringCell(row, 12, dto.getCashbackStatus());
                // 13: Area
                setStringCell(row, 13, dto.getAreaName());
            }

            // Auto size columns for better readability
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}


