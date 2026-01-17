package com.ablsoft.pima.dto;

import java.math.BigDecimal;

public record InventorySummaryDto(
        long totalProducts,
        BigDecimal totalInventoryValue,
        double averageStockAge
) {
}
