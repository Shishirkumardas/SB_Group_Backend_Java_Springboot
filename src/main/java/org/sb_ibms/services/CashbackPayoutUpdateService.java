package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.sb_ibms.enums.OrderStatus;
import org.sb_ibms.models.CashbackPayment;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.CashbackPaymentRepository;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@AllArgsConstructor
public class CashbackPayoutUpdateService {
    private MasterDataRepository masterDataRepository;
    private CashbackPaymentRepository cashbackPaymentRepository;

    @Transactional
    public Map<String, Object> processPayoutExcel(MultipartFile file) throws IOException {

        Map<String, Object> summary = new HashMap<>();
        List<String> errors = new ArrayList<>();
        int updatedMasterCount = 0;
        int createdPaymentsCount = 0;
        int skippedCount = 0;
        int skippedCountForDate = 0;
        int skippedCountForPhone = 0;
        int skippedCountForAmount = 0;
        int processedRows = 0;

        if (!Objects.requireNonNull(file.getOriginalFilename()).toLowerCase().endsWith(".xls") &&
                !file.getOriginalFilename().toLowerCase().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only .xls or .xlsx files allowed");
        }

        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("No sheet found in payout Excel");
            }

            // Data starts from row 7 (0-based index 6) in your sample
            int startRow = 6;

            for (int rowIndex = startRow; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null || isRowEmpty(row)) continue;

                processedRows++;

                try {
                    Cell dateCell = row.getCell(1);
                    LocalDate payoutDate = extractPayoutDate(dateCell);
                    if (payoutDate == null) {
                        errors.add("Row " + (rowIndex + 1) + ": Invalid or missing date");
                        skippedCount++;
                        skippedCountForDate++;
                        continue;
                    }


                    Cell oppositeCell = row.getCell(3);
                    String oppositeText = getCellStringValue(oppositeCell);
                    String phone = extractPhoneFromOppositeParty(oppositeText);

                    if (phone.isEmpty()) {
                        errors.add("Row " + (rowIndex + 1) + ": No valid phone found in Opposite Party");
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }


                    Cell withdrawnCell = row.getCell(6);
                    BigDecimal withdrawn = getCellBigDecimalValue(withdrawnCell);
                    BigDecimal cashbackPaid = (withdrawn != null && withdrawn.compareTo(BigDecimal.ZERO) < 0)
                            ? withdrawn.abs()
                            : BigDecimal.ZERO;

                    if (cashbackPaid.compareTo(BigDecimal.ZERO) <= 0) {
                        skippedCount++;
                        skippedCountForAmount++;
                        continue;
                    }


                    String normalizedPhone = normalizePhone(phone);

                    BigDecimal phoneBigDecimal = new BigDecimal(normalizedPhone);
                    List<MasterData> allMatches = masterDataRepository.findByPhone(phoneBigDecimal);

                    if (allMatches.isEmpty()) {
                        errors.add("Row " + (rowIndex + 1) + ": No MasterData found for phone " + normalizedPhone);
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }

                    MasterData selectedMaster = null;
                    LocalDate mostRecentValidPayment = LocalDate.MIN;


                    for (MasterData master : allMatches) {
                        Optional<LocalDate> latestPaymentOpt = cashbackPaymentRepository.findByMasterDataId(master.getId())
                                .stream()
                                .map(CashbackPayment::getPaymentDate)
                                .filter(Objects::nonNull)
                                .max(LocalDate::compareTo);

                        if (latestPaymentOpt.isPresent()) {
                            LocalDate latestPayment = latestPaymentOpt.get();

                            // Check if this latest payment is within 30–35 days BEFORE payout
                            long daysDiff = ChronoUnit.DAYS.between(latestPayment, payoutDate);

                            if (daysDiff >= 30 && daysDiff <= 55 && latestPayment.isBefore(payoutDate)) {
                                // Valid candidate → keep the one with most recent payment
                                if (latestPayment.isAfter(mostRecentValidPayment)) {
                                    mostRecentValidPayment = latestPayment;
                                    selectedMaster = master;
                                }
                            }
                        }
                    }

                    if (selectedMaster == null) {
                        errors.add("Row " + (rowIndex + 1) + ": No MasterData with last cashback payment 30–35 days before "
                                + payoutDate + " for phone " + normalizedPhone
                                + " (found " + allMatches.size() + " matches but none in date range)");
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }


                    if (allMatches.size() > 1) {
                        System.out.println("Multiple matches for phone " + normalizedPhone
                                + " → selected ID " + selectedMaster.getId()
                                + " with last payment on " + mostRecentValidPayment);
                    }

                    MasterData master = selectedMaster;




                    CashbackPayment payment = new CashbackPayment();
                    payment.setMasterData(master);
                    payment.setAmount(cashbackPaid);
                    payment.setPaymentDate(payoutDate);

                    cashbackPaymentRepository.save(payment);
                    createdPaymentsCount++;

                    BigDecimal currentDue = master.getDueAmount() != null ? master.getDueAmount() : BigDecimal.ZERO;
                    BigDecimal newDue = currentDue.subtract(cashbackPaid).max(BigDecimal.ZERO);
                    master.setDueAmount(newDue);

                    BigDecimal currentPaid = master.getPaidAmount() != null ? master.getPaidAmount() : BigDecimal.ZERO;
                    master.setPaidAmount(currentPaid.add(cashbackPaid));

                    if (newDue.compareTo(BigDecimal.ZERO) <= 0) {
                        master.setStatus(OrderStatus.PAID);
                    } else {
                        master.setStatus(OrderStatus.PARTIALLY_PAID);
                    }

                    if (master.getNextDueDate() != null) {
                        master.setNextDueDate(master.getNextDueDate().plusMonths(1));
                    }

                    masterDataRepository.save(master);
                    updatedMasterCount++;

                } catch (Exception e) {
                    errors.add("Row " + (rowIndex + 1) + " failed: " + e.getMessage());
                    skippedCount++;
                }
            }
        }

        summary.put("processedRows", processedRows);
        summary.put("updatedMasterRecords", updatedMasterCount);
        summary.put("createdPaymentRecords", createdPaymentsCount);
        summary.put("skippedRows", skippedCount);
        summary.put("skippedForDate", skippedCountForDate);
        summary.put("skippedForPhone", skippedCountForPhone);
        summary.put("skippedAmount", skippedCountForAmount);
        summary.put("errors", errors);
        summary.put("message", "Payout Excel processed successfully");
        System.out.println(summary);
        return summary;
    }



    private LocalDate extractPayoutDate(Cell cell) {
        if (cell == null) return null;

        String text = getCellStringValue(cell).trim();
        if (text.isEmpty()) return null;

        String datePart = text.split("\\s+")[0].trim();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try {
            return LocalDate.parse(datePart, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    private String extractPhoneFromOppositeParty(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "";
        }

        Pattern pattern = Pattern.compile("(?:\\+?880|0)?1[3-9]\\d{8}");
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private String normalizePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return "";
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");

        if (digitsOnly.startsWith("880")) {
            digitsOnly = digitsOnly.substring(3);
        } else if (digitsOnly.startsWith("88")) {
            digitsOnly = digitsOnly.substring(2);
        }
        if (!digitsOnly.startsWith("0") && !digitsOnly.startsWith("1")) {
            return "";
        }
        if (digitsOnly.length() == 11 && digitsOnly.startsWith("01")) {
            return digitsOnly;
        } else if (digitsOnly.length() == 10 && digitsOnly.startsWith("1")) {
            return "0" + digitsOnly;
        }
        return "";
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING);
        return cell.getStringCellValue().trim();
    }

    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        if (cell.getCellType() == CellType.NUMERIC) {
            return BigDecimal.valueOf(cell.getNumericCellValue());
        }

        String str = getCellStringValue(cell).replace(",", "");
        try {
            return new BigDecimal(str);
        } catch (NumberFormatException e) {
            return BigDecimal.ZERO;
        }
    }

    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}