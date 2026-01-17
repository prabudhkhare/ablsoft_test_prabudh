package com.ablsoft.pima.util;

import com.ablsoft.pima.exception.InvalidExcelException;
import com.ablsoft.pima.model.ProductInventory;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ExcelParser {
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,              // 2024-03-01
            DateTimeFormatter.ofPattern("dd-MM-yyyy"),     // 01-03-2024
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),     // 01/03/2024
            DateTimeFormatter.ofPattern("MM/dd/yyyy"),     // 03/01/2024
            DateTimeFormatter.ofPattern("dd MMM yyyy"),    // 01 Mar 2024
            DateTimeFormatter.ofPattern("dd-MMM-yyyy"),    // 01-Mar-2024
            DateTimeFormatter.ofPattern("MMM dd, yyyy"),   // Mar 01, 2024
            DateTimeFormatter.ofPattern("d-M-yyyy"),
            DateTimeFormatter.ofPattern("d/M/yyyy")
    );

    public List<ProductInventory> parse(MultipartFile file) {
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {

            FormulaEvaluator evaluator = workbook.getCreationHelper().createFormulaEvaluator();
            Sheet sheet = workbook.getSheetAt(0);
            List<ProductInventory> list = new ArrayList<>();

            // 1. Read header row (row 0)
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new InvalidExcelException("Excel file has no header row");
            }

            // 2. Map header names to column indexes
            java.util.Map<String, Integer> colIndex = new java.util.HashMap<>();
            for (Cell cell : headerRow) {
                String header = getString(cell, evaluator);
                if (header != null) {
                    colIndex.put(header.trim(), cell.getColumnIndex());
                }
            }

            // 3. Parse data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (isRowEmpty(row, evaluator)) continue;

                ProductInventory p = new ProductInventory();
                p.setProductSku(getString(row.getCell(colIndex.getOrDefault("Product SKU", 0)), evaluator));
                p.setProductName(getString(row.getCell(colIndex.getOrDefault("Product Name", 1)), evaluator));
                p.setCategory(getString(row.getCell(colIndex.getOrDefault("Category", 2)), evaluator));
                p.setPurchaseDate(getDate(row.getCell(colIndex.getOrDefault("Purchase Date", 3)), evaluator));
                p.setUnitPrice(getDecimal(row.getCell(colIndex.getOrDefault("Unit Price", 4)), evaluator));
                p.setQuantity(getDecimal(row.getCell(colIndex.getOrDefault("Quantity", 5)), evaluator).intValue());

                if (p.getProductSku() == null || p.getProductSku().isBlank()) {
                    throw new InvalidExcelException(
                            "Product SKU is mandatory (row " + (i + 1) + ")"
                    );
                }

                if (p.getPurchaseDate() == null) {
                    throw new InvalidExcelException(
                            "Purchase Date is mandatory (row " + (i + 1) + ")"
                    );
                }

                list.add(p);
            }
            return list;

        } catch (InvalidExcelException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel parsing error", e);
            throw new InvalidExcelException("Invalid Excel format or data");
        }
    }

    private String getString(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        CellValue value = evaluator.evaluate(cell);
        if (value == null) return null;

        return switch (value.getCellType()) {
            case STRING -> value.getStringValue().trim();
            case NUMERIC -> {
                // Check if it's effectively an integer
                double num = value.getNumberValue();
                if (DateUtil.isCellDateFormatted(cell)) {
                    yield cell.getLocalDateTimeCellValue().toLocalDate().toString();
                } else {
                    // Convert numeric to string without scientific notation
                    yield BigDecimal.valueOf(num).toPlainString();
                }
            }
            case BOOLEAN -> String.valueOf(value.getBooleanValue());
            default -> null;
        };
    }

    private BigDecimal getDecimal(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return BigDecimal.ZERO;

        CellValue value = evaluator.evaluate(cell);
        if (value == null) return BigDecimal.ZERO;

        return switch (value.getCellType()) {
            case NUMERIC -> BigDecimal.valueOf(value.getNumberValue());
            case STRING -> new BigDecimal(value.getStringValue().trim());
            default -> BigDecimal.ZERO;
        };
    }

    private LocalDate getDate(Cell cell, FormulaEvaluator evaluator) {
        if (cell == null) return null;

        CellValue value = evaluator.evaluate(cell);
        if (value == null) return null;

        if (value.getCellType() == CellType.NUMERIC) {
            return DateUtil
                    .getLocalDateTime(value.getNumberValue())
                    .toLocalDate();
        }

        if (value.getCellType() == CellType.STRING) {
            String text = value.getStringValue().trim();

            for (DateTimeFormatter formatter : DATE_FORMATTERS) {
                try {
                    return LocalDate.parse(text, formatter);
                } catch (DateTimeParseException ignored) {
                }
            }
            throw new InvalidExcelException(
                    "Invalid date format: '" + text + "'"
            );
        }
        return null;
    }

    private boolean isRowEmpty(Row row, FormulaEvaluator evaluator) {
        if (row == null) return true;
        for (int i = 0; i <= 5; i++) { // columns 0 to 5
            Cell cell = row.getCell(i);
            if (cell != null) {
                CellValue value = evaluator.evaluate(cell);
                if (value != null) {
                    switch (value.getCellType()) {
                        case STRING -> {
                            if (!value.getStringValue().trim().isEmpty()) return false;
                        }
                        case NUMERIC, BOOLEAN -> {
                            return false;
                        }
                        default -> {
                        }
                    }
                }
            }
        }
        return true;
    }


}
