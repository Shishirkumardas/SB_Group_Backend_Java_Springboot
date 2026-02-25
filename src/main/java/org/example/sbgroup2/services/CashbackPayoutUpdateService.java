package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.example.sbgroup2.dto.CashbackDetailsDTO;
import org.example.sbgroup2.enums.OrderStatus;
import org.example.sbgroup2.enums.PaymentMethod;
import org.example.sbgroup2.models.CashbackPayment;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.CashbackPaymentRepository;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
    private MasterDataRepository masterDataRepository;
    @Autowired
    private CashbackPaymentRepository cashbackPaymentRepository;

    // In real app: replace with @Autowired repository
    // For demo: assume you inject or have access to master list
    private final List<CashbackDetailsDTO> masterCashbackList; // load from DB in real impl

//    public CashbackPayoutUpdateService() {
//        // In production: load from DB in constructor or via method param
//        this.masterCashbackList = loadMasterDataFromDB(); // placeholder
//    }


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

        if (!file.getOriginalFilename().toLowerCase().endsWith(".xls") &&
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
                    // ─── 1. Extract Date (Column B - index 1) ───────────────────────
                    Cell dateCell = row.getCell(1);
                    LocalDate payoutDate = extractPayoutDate(dateCell);
                    if (payoutDate == null) {
                        errors.add("Row " + (rowIndex + 1) + ": Invalid or missing date");
                        skippedCount++;
                        skippedCountForDate++;
                        continue;
                    }

                    // ─── 2. Extract Phone (Column D - index 3) ───────────────────────
                    Cell oppositeCell = row.getCell(3);
                    String oppositeText = getCellStringValue(oppositeCell);
                    String phone = extractPhoneFromOppositeParty(oppositeText);

                    if (phone.isEmpty()) {
                        errors.add("Row " + (rowIndex + 1) + ": No valid phone found in Opposite Party");
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }

                    // ─── 3. Extract Amount (Column G - index 6) ──────────────────────
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

                    // ─── 4. Find matching MasterData by phone ────────────────────────
                    String normalizedPhone = normalizePhone(phone);
//                    BigDecimal phoneBigDecimal = new BigDecimal(normalizedPhone);
//                    Optional<MasterData> masterOpt = masterDataRepository.findByPhone(phoneBigDecimal);
//
//                    if (masterOpt.isEmpty()) {
//                        errors.add("Row " + (rowIndex + 1) + ": No MasterData found for phone " + phone);
//                        skippedCount++;
//                        skippedCountForPhone++;
//                        continue;
//                    }
//
//                    MasterData master = masterOpt.get();



                    // Calculate the acceptable range for the LAST payment date
                    LocalDate payoutMinus35 = payoutDate.minusDays(35);
                    LocalDate payoutMinus30 = payoutDate.minusDays(30);

                    // Step 1: Get ALL MasterData records with this phone
                    BigDecimal phoneBigDecimal = new BigDecimal(normalizedPhone);
                    List<MasterData> allMatches = masterDataRepository.findByPhone(phoneBigDecimal);

                    if (allMatches.isEmpty()) {
                        errors.add("Row " + (rowIndex + 1) + ": No MasterData found for phone " + normalizedPhone);
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }

// Step 2: Find candidates where the MOST RECENT payment falls in 30–35 days window
                    MasterData selectedMaster = null;
                    LocalDate mostRecentValidPayment = LocalDate.MIN;


                    for (MasterData master : allMatches) {
                        // Get the latest payment date for this master (if any payments exist)

//                        CashbackPayment selectedCashbackPayment = cashbackPaymentRepository.findByMasterDataId(master.getId())
//                                    .stream()
//                                    .filter(p -> p.getPaymentDate() != null)
//                                    .max(Comparator.comparing(CashbackPayment::getPaymentDate))
//                                    .orElse(null);
//                        }
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
                        // If no payments at all → skip (or handle separately if needed)
                    }

                    if (selectedMaster == null) {
                        errors.add("Row " + (rowIndex + 1) + ": No MasterData with last cashback payment 30–35 days before "
                                + payoutDate + " for phone " + normalizedPhone
                                + " (found " + allMatches.size() + " matches but none in date range)");
                        skippedCount++;
                        skippedCountForPhone++;
                        continue;
                    }

// Optional: Log if multiple were possible but we picked one
                    if (allMatches.size() > 1) {
                        System.out.println("Multiple matches for phone " + normalizedPhone
                                + " → selected ID " + selectedMaster.getId()
                                + " with last payment on " + mostRecentValidPayment);
                    }

// Now use selectedMaster
                    MasterData master = selectedMaster;



                    // ─── 5. Create CashbackPayment record ────────────────────────────
                    CashbackPayment payment = new CashbackPayment();
                    payment.setMasterData(master);
                    payment.setAmount(cashbackPaid);
                    payment.setPaymentDate(payoutDate);
//                    payment.setPaymentMethod(PaymentMethod.BKASH); // or detect from text if needed
//                    payment.setTransactionId(getCellStringValue(row.getCell(0))); // Receipt No.
//                    payment.setRemarks("Imported from payout Excel: " + oppositeText);

                    cashbackPaymentRepository.save(payment);
                    createdPaymentsCount++;

                    // ─── 6. Update MasterData due/missed amount & status ─────────────
                    BigDecimal currentDue = master.getDueAmount() != null ? master.getDueAmount() : BigDecimal.ZERO;
                    BigDecimal newDue = currentDue.subtract(cashbackPaid).max(BigDecimal.ZERO);
                    master.setDueAmount(newDue);

                    // Optional: update paid amount or total cashback received
                    BigDecimal currentPaid = master.getPaidAmount() != null ? master.getPaidAmount() : BigDecimal.ZERO;
                    master.setPaidAmount(currentPaid.add(cashbackPaid));

                    // Status logic (customize as per your business rules)
                    if (newDue.compareTo(BigDecimal.ZERO) <= 0) {
                        master.setStatus(OrderStatus.PAID); // or your enum/status field
                    } else {
                        master.setStatus(OrderStatus.PARTIALLY_PAID);
                    }

                    // Optional: move next cashback due date forward (example)
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

// ─── Helper Methods ───────────────────────────────────────────────────────

    private LocalDate extractPayoutDate(Cell cell) {
        if (cell == null) return null;

        String text = getCellStringValue(cell).trim();
        if (text.isEmpty()) return null;

        // Take only date part (before space)
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

        // Match BD phone patterns: +88017xxxxxxxx, 88017xxxxxxxx, 017xxxxxxxx, 013xxxxxxxx, etc.
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

        // Step 1: Remove all non-digits
        String digitsOnly = phone.replaceAll("[^0-9]", "");

        // Step 2: Remove country code prefix (880 or +880) if present
        if (digitsOnly.startsWith("880")) {
            digitsOnly = digitsOnly.substring(3);  // remove "880"
        } else if (digitsOnly.startsWith("88")) {
            digitsOnly = digitsOnly.substring(2);  // rare case, but safe
        }

        // Step 3: If it starts with 0 already (local format), keep it
        // If it doesn't start with 0 or 1, it's invalid → return empty
        if (!digitsOnly.startsWith("0") && !digitsOnly.startsWith("1")) {
            return "";
        }

        // Step 4: Ensure it's 11 digits (01xxxxxxxxx)
        if (digitsOnly.length() == 11 && digitsOnly.startsWith("01")) {
            return digitsOnly;           // perfect: 01771272525
        } else if (digitsOnly.length() == 10 && digitsOnly.startsWith("1")) {
            return "0" + digitsOnly;     // convert 1771272525 → 01771272525
        }

        // If length or format is wrong, return empty
        return "";
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        cell.setCellType(CellType.STRING); // force string to avoid numeric issues
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