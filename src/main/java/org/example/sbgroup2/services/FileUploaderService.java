package org.example.sbgroup2.services;

import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.example.sbgroup2.enums.PaymentMethod;
import org.example.sbgroup2.models.Area;
import org.example.sbgroup2.models.CashbackPayment;
import org.example.sbgroup2.models.MasterData;
import org.example.sbgroup2.repositories.CashbackPaymentRepository;
import org.example.sbgroup2.repositories.MasterDataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.*;
@Service
public class FileUploaderService {
    @Autowired
    private MasterDataService masterDataService;
    @Autowired
    private MasterDataRepository masterDataRepository;
    @Autowired
    private AreaService areaService;
    @Autowired
    private CashbackService cashbackService;
    @Autowired
    private CashbackPaymentRepository cashbackPaymentRepository;


    // 🔥 New: Complete Excel Reading Functionality (Using Apache POI for XLSX)
    // Incorporates the cashback column logic from your query
    // Assumes columns: 0=consumerName, 1=areaCode, 2=purchaseAmount, 3=paidAmount, 4=purchaseDate
    // Then dynamic columns like "Oct-25", "Nov-25" for cashback payments
    @Transactional
    public List<MasterData> importFromExcel(MultipartFile file) {
        List<MasterData> importedData = new ArrayList<>();

        if (!file.getOriginalFilename().endsWith(".xlsx")) {
            throw new IllegalArgumentException("Only XLSX files allowed");
        }

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new IllegalArgumentException("No sheet found in Excel");
            }


            Map<Integer, YearMonth> cashbackColumns = new HashMap<>();

            Row header = sheet.getRow(1);
            if (header == null) {
                throw new IllegalArgumentException("Header row not found");
            }

            DateTimeFormatter[] formatters = new DateTimeFormatter[]{
                    DateTimeFormatter.ofPattern("MMM-yy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MMM-yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH),
                    DateTimeFormatter.ofPattern("MM-yyyy"),
                    DateTimeFormatter.ofPattern("MM-yy"),
                    DateTimeFormatter.ofPattern("yyyy-MM"),
                    DateTimeFormatter.ofPattern("yyyy/MM"),
                    DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                    DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                    DateTimeFormatter.ofPattern("MM/dd/yyyy"),
                    DateTimeFormatter.ofPattern("MM-dd-yyyy")
            };

            for (int col = 12; col < 24; col++) {
                Cell cell = header.getCell(col);
                if (cell == null) continue;

                try {
                    // ✅ CASE 1: Real Excel Date
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                        cashbackColumns.put(col, YearMonth.from(date));
                        continue;
                    }

                    // ✅ CASE 2: Text date
                    cell.setCellType(CellType.STRING);
                    String raw = cell.getStringCellValue();

                    if (raw == null || raw.isBlank()) continue;

                    String text = raw
                            .trim()
                            .replace("/", "-")
                            .replaceAll("\\s+", " ");

                    // Try parsing with multiple formats
                    for (DateTimeFormatter formatter : formatters) {
                        try {
                            TemporalAccessor parsed = formatter.parse(text);

                            YearMonth ym;
                            if (parsed.isSupported(ChronoField.MONTH_OF_YEAR)) {
                                int year = parsed.isSupported(ChronoField.YEAR)
                                        ? parsed.get(ChronoField.YEAR)
                                        : Year.now().getValue();

                                int month = parsed.get(ChronoField.MONTH_OF_YEAR);
                                ym = YearMonth.of(year, month);
                            } else {
                                continue;
                            }

                            cashbackColumns.put(col, ym);
                            break; // stop once parsed
                        } catch (Exception ignored) {
                        }
                    }

                } catch (Exception ignored) {
                }
            }

            if (cashbackColumns.isEmpty()) {
                throw new IllegalStateException("No cashback month columns detected");
            }
//            int lastCol = header.getLastCellNum();
//
//            for (int col = 0; col < lastCol; col++) {
//                Cell cell = header.getCell(col);
//                if (cell == null) continue;
//
//                try {
//                    // CASE 1: Excel date header
//                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
//                        LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
//                        cashbackColumns.put(col, YearMonth.from(date));
//                        continue;
//                    }
//
//                    // CASE 2: Text header
//                    String raw = cell.toString().trim();
//                    if (raw.isEmpty()) continue;
//
//                    raw = raw.replace("/", "-").replaceAll("\\s+", " ");
//
//                    for (DateTimeFormatter formatter : formatters) {
//                        try {
//                            TemporalAccessor parsed = formatter.parse(raw);
//
//                            if (parsed.isSupported(ChronoField.MONTH_OF_YEAR)) {
//                                int year = parsed.isSupported(ChronoField.YEAR)
//                                        ? parsed.get(ChronoField.YEAR)
//                                        : Year.now().getValue();
//
//                                int month = parsed.get(ChronoField.MONTH_OF_YEAR);
//                                cashbackColumns.put(col, YearMonth.of(year, month));
//                                break;
//                            }
//                        } catch (Exception ignored) {}
//                    }
//                } catch (Exception ignored) {}
//            }



            // Step B: Process Each Data Row
            for (int r = 2; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                MasterData master = new MasterData();

                // Fixed columns (adjust indices as per your Excel structure)
                int CONSUMER_NAME_COL = 1;
                int QUANTITY_COL = 2;
                int DATE_COL = 3;
                int NID_COL = 4;
                int BKASH_COL = 5;
                int ROCKET_COL = 6;
                int NAG_COL = 7;
                int REMARKS_COL = 8;
                int AREA_CODE_COL = 9;
                int PURCHASE_COL = 10;
                int PAID_COL = 11;



                BigDecimal quantity = getCellBigDecimalValue(row.getCell(QUANTITY_COL));
                LocalDate purchaseDate = getCellLocalDateValue(row.getCell(DATE_COL));
                if (purchaseDate == null) {
                    System.out.println(
                            "⛔ Skipping row " + (r + 1) + " → Purchase date missing or invalid"
                    );
                    continue; // 🚨 VERY IMPORTANT
                }
                BigDecimal nid = getCellBigDecimalValue(row.getCell(NID_COL));
                //Payment numbers (keep as BigDecimal)
                BigDecimal bkashNumber  = getCellBigDecimalValue(row.getCell(BKASH_COL));
                BigDecimal rocketNumber = getCellBigDecimalValue(row.getCell(ROCKET_COL));
                BigDecimal nagodNumber  = getCellBigDecimalValue(row.getCell(NAG_COL));

                String areaCode = getCellStringValue(row.getCell(AREA_CODE_COL));
                BigDecimal purchaseAmount = getCellBigDecimalValue(row.getCell(PURCHASE_COL));
                BigDecimal amountBackFromPurchase = getCellBigDecimalValue(row.getCell(PAID_COL));

                Area area = areaService.getOrCreateArea(areaCode); // Or get by code/name
                master.setArea(area);

                // Priority selection for main phone
                BigDecimal selectedPhone = null;

                if (bkashNumber != null && bkashNumber.compareTo(BigDecimal.ZERO) > 0) {
                    selectedPhone = bkashNumber;
                    master.setPaymentMethod(PaymentMethod.BKASH);
                } else if (rocketNumber != null && rocketNumber.compareTo(BigDecimal.ZERO) > 0) {
                    selectedPhone = rocketNumber;
                    master.setPaymentMethod(PaymentMethod.ROCKET);
                } else if (nagodNumber != null && nagodNumber.compareTo(BigDecimal.ZERO) > 0) {
                    selectedPhone = nagodNumber;
                    master.setPaymentMethod(PaymentMethod.NAGAD);
                }

                master.setName(getCellStringValue(row.getCell(CONSUMER_NAME_COL)));
                master.setPhone(selectedPhone);
                master.setBkashNumber(bkashNumber);
                master.setRocketNumber(rocketNumber);
                master.setNogodNumber(nagodNumber);
                master.setRemarks(getCellStringValue(row.getCell(REMARKS_COL)));
                master.setNid(nid);
                master.setQuantity(quantity);
                if (purchaseAmount == null) purchaseAmount = BigDecimal.ZERO;
                if (amountBackFromPurchase == null) amountBackFromPurchase = BigDecimal.ZERO;
                master.setPurchaseAmount(purchaseAmount);

                master.setPaidAmount(purchaseAmount);
                master.setAmountBackFromPurchase(amountBackFromPurchase);


                master.setDueAmount(amountBackFromPurchase);

                master.setDate(purchaseDate);

                // Save MasterData first
                MasterData savedMaster = masterDataRepository.save(master);
                System.out.println("Saved MasterData ID = " + savedMaster.getId());

                importedData.add(savedMaster);
//                cashbackService.calculateCashback(master);

                // Calculate monthly cashback (10% of purchase)
                BigDecimal monthlyCashback = purchaseAmount.divide(
                        BigDecimal.TEN, 2, RoundingMode.HALF_UP
                );


//                // Process Cashback Payments from dynamic columns
//                BigDecimal totalCashbackPaid = BigDecimal.ZERO;
//                for (Map.Entry<Integer, YearMonth> entry : cashbackColumns.entrySet()) {
//                    int col = entry.getKey();
//                    YearMonth month = entry.getValue();
//
//                    Cell cell = row.getCell(entry.getKey());
//                    if (cell == null || cell.getCellType() != CellType.NUMERIC) continue;
//
//                    BigDecimal paymentAmount = BigDecimal.valueOf(cell.getNumericCellValue());
//                    if (paymentAmount.compareTo(BigDecimal.ZERO) > 0) {
//                        CashbackPayment payment = new CashbackPayment();
//                        payment.setMasterData(savedMaster);
//                        payment.setAmount(paymentAmount);
//                        payment.setPaymentDate(entry.getValue().atDay(1)); // First day of month
//
//
//                        cashbackPaymentRepository.save(payment);
////                        cashbackService.calculateCashback(master);
//                        totalCashbackPaid = totalCashbackPaid.add(paymentAmount);
//                    }
//                }
                // Process Cashback Payments - only for detected month columns
                BigDecimal totalCashbackPaid = BigDecimal.ZERO;
                LocalDate nextCashbackDate = purchaseDate.plusDays(30);

                for (Map.Entry<Integer, YearMonth> entry : cashbackColumns.entrySet()) {
                    int col = entry.getKey();
                    YearMonth month = entry.getValue();

                    Cell cell = row.getCell(col);
                    if (cell == null) continue; // skip empty cells

                    BigDecimal amount = getCellBigDecimalValue(cell); // your safe method handles - / blank as 0

                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        CashbackPayment payment = new CashbackPayment();
                        payment.setMasterData(savedMaster);
                        payment.setAmount(amount);
                        payment.setPaymentDate(nextCashbackDate); // 10th of month as per your Excel note


                        cashbackPaymentRepository.save(payment);


                        totalCashbackPaid = totalCashbackPaid.add(amount);
                        nextCashbackDate = nextCashbackDate.plusDays(30);

                        // Debug log - MUST SEE THIS IN CONSOLE AFTER UPLOAD
                        System.out.println(String.format(
                                "SAVED CASHBACK → Customer: %s | Month: %s | Amount: %s | Payment ID: %d",
                                savedMaster.getName(), month, amount, payment.getId()
                        ));
                    }
                }
//                cashbackService.calculateCashback2(master);

                // Optional: Validate total cashback <= purchaseAmount or some business rule
                BigDecimal maxCashback = purchaseAmount
                        .multiply(BigDecimal.valueOf(0.10))
                        .setScale(2, RoundingMode.HALF_UP);

//                if (totalCashbackPaid.compareTo(maxCashback) > 0) {
//                    throw new IllegalArgumentException("Cashback exceeds allowed limit");
//                }


                // Recalculate area
                areaService.recalculateArea(area.getId());

                // 🔥 Bonus: Calculate missed months, status, next due (if needed)
                // Assuming purchaseDate is start, and monthly payments expected
//                long expectedMonths = 0;
//
//                if (purchaseDate != null) {
//                    expectedMonths = ChronoUnit.MONTHS.between(purchaseDate, LocalDate.now()) + 1;
//                }
//
//                long paidMonths = cashbackColumns.entrySet().stream()
//                        .filter(e -> {
//                            Cell c = row.getCell(e.getKey());
//                            return c != null
//                                    && c.getCellType() == CellType.NUMERIC
//                                    && c.getNumericCellValue() > 0;
//                        })
//                        .count();

//                long missedMonths = expectedMonths - paidMonths;
//                // Update master status based on missedMonths (e.g., if missed > 0 → OVERDUE)
//                // master.setStatus(missedMonths > 0 ? OrderStatus.OVERDUE : OrderStatus.ACTIVE);
//                // masterRepository.save(master); // If needed
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return importedData;
    }

    // Helper methods for safe cell reading (from previous response)
    private boolean isRowEmpty(Row row) {
        for (int c = row.getFirstCellNum(); c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                return false;
            }
        }
        return true;
    }

    private String getCellStringValue(Cell cell) {
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            default -> null;
        };
    }


    private BigDecimal getCellBigDecimalValue(Cell cell) {
        if (cell == null) return BigDecimal.ZERO;

        switch (cell.getCellType()) {
            case NUMERIC:
                // For numeric cells, directly convert to BigDecimal
                return BigDecimal.valueOf(cell.getNumericCellValue());

            case STRING:
                String str = cell.getStringCellValue().trim();
                if (str.isEmpty()) return BigDecimal.ZERO;
                try {
                    return new BigDecimal(str);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }

            case BOOLEAN:
                // Convert boolean to 1 or 0
                return cell.getBooleanCellValue() ? BigDecimal.ONE : BigDecimal.ZERO;

            case FORMULA:
                // Evaluate the formula and then parse the result
                // Note: You need FormulaEvaluator for this
                // For simplicity, treat as numeric or string
                try {
                    double value = cell.getNumericCellValue();
                    return BigDecimal.valueOf(value);
                } catch (Exception e) {
                    String formulaResult = cell.getCellFormula();
                    try {
                        return new BigDecimal(formulaResult);
                    } catch (NumberFormatException ex) {
                        return BigDecimal.ZERO;
                    }
                }

            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return BigDecimal.ZERO;
        }
    }

    private LocalDate getCellLocalDateValue(Cell cell) {
        if (cell == null) return null;

        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return DateUtil.getLocalDateTime(cell.getNumericCellValue())
                        .toLocalDate();
            }

            if (cell.getCellType() == CellType.STRING) {
                String value = cell.getStringCellValue().trim();
                if (value.isEmpty()) return null;

                List<DateTimeFormatter> formats = List.of(
                        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
                        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
                        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                        DateTimeFormatter.ofPattern("MM/dd/yyyy")
                );

                for (DateTimeFormatter f : formats) {
                    try {
                        return LocalDate.parse(value, f);
                    } catch (Exception ignored) {}
                }
            }
        } catch (Exception ignored) {}

        return null;
    }

}
