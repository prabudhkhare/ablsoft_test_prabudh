package com.ablsoft.pima.controller;

import com.ablsoft.pima.dto.InventorySummaryDto;
import com.ablsoft.pima.dto.PageResponse;
import com.ablsoft.pima.model.ProductInventory;
import com.ablsoft.pima.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@CrossOrigin
public class InventoryController {
    private final InventoryService service;

    @PostMapping("/import")
    public ResponseEntity<?> importExcel(
            @RequestParam("file") MultipartFile file) {
        service.importExcel(file);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public PageResponse<ProductInventory> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "purchaseDate") String sort,
            @RequestParam(defaultValue = "desc") String direction
    ) {

        Page<ProductInventory> result =
                service.findAll(page, size, sort, direction);

        return new PageResponse<>(
                result.getContent(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.getNumber(),
                result.getSize()
        );
    }

    @GetMapping("/summary")
    public InventorySummaryDto summary() {
        return service.summary();
    }
}