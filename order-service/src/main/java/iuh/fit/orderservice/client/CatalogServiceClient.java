package iuh.fit.orderservice.client;

import iuh.fit.orderservice.dto.ProductResponse;
import iuh.fit.orderservice.dto.ProductSoldUpdateRequest;
import iuh.fit.orderservice.dto.ProductVariantResponse;
import iuh.fit.orderservice.dto.StockAdjustmentRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@FeignClient(name = "catalog-service")
public interface CatalogServiceClient {

    @GetMapping("/api/v1/catalog/variants/{variantId}")
    ProductVariantResponse getVariantById(@PathVariable("variantId") UUID variantId);

    @GetMapping("/api/v1/catalog/products/{productId}")
    ProductResponse getProductById(@PathVariable("productId") UUID productId);

    @PostMapping("/api/v1/catalog/products/total-sold/increment")
    void incrementProductTotalSold(@RequestBody List<ProductSoldUpdateRequest> requests);

    @PostMapping("/internal/catalog/variants/stock-adjustments")
    void applyStockAdjustment(
            @RequestHeader Map<String, String> headers,
            @RequestBody StockAdjustmentRequest request
    );
}
