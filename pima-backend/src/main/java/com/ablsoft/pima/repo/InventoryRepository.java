package com.ablsoft.pima.repo;

import com.ablsoft.pima.model.ProductInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<ProductInventory, UUID> {
}
