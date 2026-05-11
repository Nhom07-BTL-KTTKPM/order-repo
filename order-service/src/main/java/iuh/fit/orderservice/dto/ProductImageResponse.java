package iuh.fit.orderservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ProductImageResponse(
        UUID id,
        String url,
        Boolean isPrimary
) {
}
