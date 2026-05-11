package iuh.fit.orderservice.client;

import iuh.fit.orderservice.dto.ProductResponse;
import iuh.fit.orderservice.dto.ProductVariantResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "catalog-service")
public interface CatalogServiceClient {

    @GetMapping("/api/v1/catalog/variants/{variantId}")
    ProductVariantResponse getVariantById(@PathVariable("variantId") UUID variantId);

    @GetMapping("/api/v1/catalog/products/{productId}")
    ProductResponse getProductById(@PathVariable("productId") UUID productId);
}
