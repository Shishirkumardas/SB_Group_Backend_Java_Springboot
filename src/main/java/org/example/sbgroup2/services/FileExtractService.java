package org.example.sbgroup2.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.sbgroup2.dto.CashbackDetailsDTO;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;


@Component
public class FileExtractService {

    public ByteArrayInputStream generate(List<CashbackDetailsDTO> list) {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Cashback Due");

            // Header row
            Row header = sheet.createRow(0);
            String[] columns = {
                    "Name",
                    "Purchase Date",
                    "Total Purchase",
                    "Phone Number",
                    "Payment Method",
                    "Monthly Cashback",
                    "Cashback Start Date",
                    "Earliest Missed Date",
                    "Missed Month",
                    "Missed Amount",
                    "Next Due Date",
                    "Status"
            };

            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (CashbackDetailsDTO dto : list) {
                Row row = sheet.createRow(rowIdx++);

                // Column 0: Name (String – usually safe, but null-safe)
                row.createCell(0).setCellValue(dto.getName() != null ? dto.getName() : "N/A");

                // Column 1: Purchase Date
                row.createCell(1).setCellValue(
                        dto.getPurchaseDate() != null ? dto.getPurchaseDate().toString() : "N/A"
                );

                // Column 2: Total Purchase (BigDecimal)
                row.createCell(2).setCellValue(
                        dto.getTotalPurchase() != null ? dto.getTotalPurchase().doubleValue() : 0.0
                );

                // Column 3: Phone Number (assuming String – adjust if BigDecimal)
                row.createCell(3).setCellValue(
                        dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "N/A"
                );

                // Column 4: Payment Method
                row.createCell(4).setCellValue(
                        dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "N/A"
                );

                // Column 5: Monthly Cashback (BigDecimal)
                row.createCell(5).setCellValue(
                        dto.getExpectedMonthlyCashbackAmount() != null
                                ? dto.getExpectedMonthlyCashbackAmount().toString()
                                : "0"
                );

                // Column 6: Cashback Start Date
                row.createCell(6).setCellValue(
                        dto.getCashbackStartDate() != null ? dto.getCashbackStartDate().toString() : "N/A"
                );

                // Column 7: Earliest Missed Date – this was the crashing line
                row.createCell(7).setCellValue(
                        dto.getEarliestMissedDueDate() != null
                                ? dto.getEarliestMissedDueDate().toString()
                                : "No missed due date"
                );

                // Column 8: Missed Month
                row.createCell(8).setCellValue(
                        dto.getEarliestDueMonth() != null ? dto.getEarliestDueMonth().toString() : "N/A"
                );

                // Column 9: Missed Amount (BigDecimal)
                row.createCell(9).setCellValue(
                        dto.getMissedCashbackAmount() != null
                                ? dto.getMissedCashbackAmount().toString()
                                : "0"
                );

                // Column 10: Next Due Date (already had null check – improved readability)
                row.createCell(10).setCellValue(
                        dto.getNextDueDate() != null ? dto.getNextDueDate().toString() : "N/A"
                );

                // Column 11: Status
                row.createCell(11).setCellValue(
                        dto.getCashbackStatus() != null ? dto.getCashbackStatus() : "N/A"
                );
            }

            // Write to byte stream
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}


