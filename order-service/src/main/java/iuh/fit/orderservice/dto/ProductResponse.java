package iuh.fit.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductResponse(
        UUID id,
        String name,
        List<ProductImageResponse> images
) {
}
