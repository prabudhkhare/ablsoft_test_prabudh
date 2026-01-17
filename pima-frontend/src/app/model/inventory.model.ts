export interface ProductInventory {
  id: string;
  productSku: string;
  productName: string;
  category: string;
  purchaseDate: string;
  unitPrice: number;
  quantity: number;
}

export interface InventorySummary {
  totalProducts: number;
  totalInventoryValue: number;
  averageStockAge: number;
}
