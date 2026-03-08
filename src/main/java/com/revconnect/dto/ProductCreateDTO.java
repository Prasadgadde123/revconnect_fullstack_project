package com.revconnect.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductCreateDTO {
    @NotBlank(message = "Product name cannot be empty")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;

    @PositiveOrZero(message = "Price cannot be negative")
    private BigDecimal price;

    private String imageUrl;

    @Size(max = 50)
    private String category;

    private String productUrl;

    @Builder.Default
    private boolean active = true;
}
