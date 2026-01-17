package com.ablsoft.pima.service.impl;

import com.ablsoft.pima.dto.InventorySummaryDto;
import com.ablsoft.pima.model.ProductInventory;
import com.ablsoft.pima.repo.InventoryRepository;
import com.ablsoft.pima.service.InventoryService;
import com.ablsoft.pima.util.ExcelParser;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryRepository repository;
    private final ExcelParser excelParser;

    @Override
    public void importExcel(MultipartFile file) {
        List<ProductInventory> products = excelParser.parse(file);
        repository.saveAll(products);
    }

    @Override
    public Page<ProductInventory> findAll(int page, int size, String sort, String direction) {
        Sort.Direction dir =
                direction.equalsIgnoreCase("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        return repository.findAll(pageable);
    }

    @Override
    public InventorySummaryDto summary() {
        List<ProductInventory> list = repository.findAll();

        long totalProducts = list.stream()
                .mapToLong(ProductInventory::getQuantity)
                .sum();

        BigDecimal totalValue = list.stream()
                .map(p -> p.getUnitPrice().multiply(
                        BigDecimal.valueOf(p.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double avgAge = list.stream()
                .mapToLong(p ->
                        ChronoUnit.DAYS.between(p.getPurchaseDate(), LocalDate.now()))
                .average()
                .orElse(0);

        return new InventorySummaryDto(totalProducts, totalValue, avgAge);
    }
}
