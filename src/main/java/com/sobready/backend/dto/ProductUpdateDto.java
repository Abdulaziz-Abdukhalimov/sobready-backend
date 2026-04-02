package com.sobready.backend.dto;

import com.sobready.backend.enums.*;
import lombok.Getter;
import lombok.Setter;

/**
 * All fields are optional (nullable) because update = partial update.
 * Only non-null fields get updated.
 *
 * NestJS equivalent:
 *   export class UpdateProductDto extends PartialType(CreateProductDto) {}
 *   PartialType makes all fields optional — same idea here.
 */
@Getter
@Setter
public class ProductUpdateDto {
    private Long productId;
    private String productName;
    private String productBrand;
    private Integer productPrice;
    private Integer productStock;
    private ProductType productType;
    private ProductFragrance productFragrance;
    private ProductGender productGender;
    private ProductVolume productVolume;
    private String productDesc;
    private ProductStatus productStatus;
}
