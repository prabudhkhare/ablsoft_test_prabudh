package com.ablsoft.pima.service;

import com.ablsoft.pima.dto.InventorySummaryDto;
import com.ablsoft.pima.model.ProductInventory;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface InventoryService {
    void importExcel(MultipartFile file);

    InventorySummaryDto summary();

    Page<ProductInventory> findAll(int page, int size, String sort, String direction);
}
