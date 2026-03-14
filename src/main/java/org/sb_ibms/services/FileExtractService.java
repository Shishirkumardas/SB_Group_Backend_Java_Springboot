package org.sb_ibms.services;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sb_ibms.dto.CashbackDetailsDTO;
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
                    "Quantity",
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

                row.createCell(0).setCellValue(dto.getName() != null ? dto.getName() : "N/A");

                row.createCell(1).setCellValue(dto.getQuantity() != null ? dto.getQuantity().toString() : "N/A");

                row.createCell(2).setCellValue(
                        dto.getPurchaseDate() != null ? dto.getPurchaseDate().toString() : "N/A"
                );

                row.createCell(3).setCellValue(
                        dto.getTotalPurchase() != null ? dto.getTotalPurchase().doubleValue() : 0.0
                );

                row.createCell(4).setCellValue(
                        dto.getPhoneNumber() != null ? dto.getPhoneNumber() : "N/A"
                );

                row.createCell(5).setCellValue(
                        dto.getPaymentMethod() != null ? dto.getPaymentMethod() : "N/A"
                );

                row.createCell(6).setCellValue(
                        dto.getExpectedMonthlyCashbackAmount() != null
                                ? dto.getExpectedMonthlyCashbackAmount().toString()
                                : "0"
                );

                row.createCell(7).setCellValue(
                        dto.getCashbackStartDate() != null ? dto.getCashbackStartDate().toString() : "N/A"
                );

                row.createCell(8).setCellValue(
                        dto.getEarliestMissedDueDate() != null
                                ? dto.getEarliestMissedDueDate().toString()
                                : "No missed due date"
                );

                row.createCell(9).setCellValue(
                        dto.getEarliestDueMonth() != null ? dto.getEarliestDueMonth().toString() : "N/A"
                );

                row.createCell(10).setCellValue(
                        dto.getMissedCashbackAmount() != null
                                ? dto.getMissedCashbackAmount().toString()
                                : "0"
                );

                row.createCell(11).setCellValue(
                        dto.getNextDueDate() != null ? dto.getNextDueDate().toString() : "N/A"
                );

                row.createCell(12).setCellValue(
                        dto.getCashbackStatus() != null ? dto.getCashbackStatus() : "N/A"
                );
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());

        } catch (IOException e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }
}


