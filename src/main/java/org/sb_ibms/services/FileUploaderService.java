package org.sb_ibms.services;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.sb_ibms.enums.PaymentMethod;
import org.sb_ibms.models.Area;
import org.sb_ibms.models.CashbackPayment;
import org.sb_ibms.models.MasterData;
import org.sb_ibms.repositories.CashbackPaymentRepository;
import org.sb_ibms.repositories.MasterDataRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.*;
@Service
@AllArgsConstructor
public class FileUploaderService {

    private MasterDataRepository masterDataRepository;
    private AreaService areaService;
    private CashbackPaymentRepository cashbackPaymentRepository;

    @Transactional
    public List<MasterData> importFromExcel(MultipartFile file) {
        List<MasterData> importedData = new ArrayList<>();

        if (!Objects.requireNonNull(file.getOriginalFilename()).endsWith(".xlsx")) {
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
                    if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                        LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                        cashbackColumns.put(col, YearMonth.from(date));
                        continue;
                    }


                    cell.setCellType(CellType.STRING);
                    String raw = cell.getStringCellValue();

                    if (raw == null || raw.isBlank()) continue;

                    String text = raw
                            .trim()
                            .replace("/", "-")
                            .replaceAll("\\s+", " ");


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
                            break;
                        } catch (Exception ignored) {
                        }
                    }

                } catch (Exception ignored) {
                }
            }

            if (cashbackColumns.isEmpty()) {
                throw new IllegalStateException("No cashback month columns detected");
            }

            for (int r = 2; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null || isRowEmpty(row)) continue;

                MasterData master = new MasterData();
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
                    );
                    continue;
                }
                BigDecimal nid = getCellBigDecimalValue(row.getCell(NID_COL));
                BigDecimal bkashNumber  = getCellBigDecimalValue(row.getCell(BKASH_COL));
                BigDecimal rocketNumber = getCellBigDecimalValue(row.getCell(ROCKET_COL));
                BigDecimal nagodNumber  = getCellBigDecimalValue(row.getCell(NAG_COL));

                String areaCode = getCellStringValue(row.getCell(AREA_CODE_COL));
                BigDecimal purchaseAmount = getCellBigDecimalValue(row.getCell(PURCHASE_COL));
                BigDecimal amountBackFromPurchase = getCellBigDecimalValue(row.getCell(PAID_COL));

                Area area = areaService.getOrCreateArea(areaCode);
                master.setArea(area);

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

                MasterData savedMaster = masterDataRepository.save(master);
                System.out.println("Saved MasterData ID = " + savedMaster.getId());

                importedData.add(savedMaster);
                BigDecimal monthlyCashback = purchaseAmount.divide(
                        BigDecimal.TEN, 2, RoundingMode.HALF_UP
                );

                BigDecimal totalCashbackPaid = BigDecimal.ZERO;
                LocalDate nextCashbackDate = purchaseDate.plusDays(30);

                for (Map.Entry<Integer, YearMonth> entry : cashbackColumns.entrySet()) {
                    int col = entry.getKey();
                    YearMonth month = entry.getValue();

                    Cell cell = row.getCell(col);
                    if (cell == null) continue;

                    BigDecimal amount = getCellBigDecimalValue(cell);

                    if (amount.compareTo(BigDecimal.ZERO) > 0) {
                        CashbackPayment payment = new CashbackPayment();
                        payment.setMasterData(savedMaster);
                        payment.setAmount(amount);
                        payment.setPaymentDate(nextCashbackDate);


                        cashbackPaymentRepository.save(payment);


                        totalCashbackPaid = totalCashbackPaid.add(amount);
                        nextCashbackDate = nextCashbackDate.plusDays(30);

                        System.out.printf(
                                "SAVED CASHBACK → Customer: %s | Month: %s | Amount: %s | Payment ID: %d%n",
                                savedMaster.getName(), month, amount, payment.getId()
                        );
                    }
                }
                BigDecimal maxCashback = purchaseAmount
                        .multiply(BigDecimal.valueOf(0.10))
                        .setScale(2, RoundingMode.HALF_UP);

                areaService.recalculateArea(area.getId());
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel file", e);
        }

        return importedData;
    }



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
                return cell.getBooleanCellValue() ? BigDecimal.ONE : BigDecimal.ZERO;

            case FORMULA:

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
