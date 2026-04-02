package com.sobready.backend.dto;

import com.sobready.backend.enums.*;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO = Data Transfer Object — a class that carries data between layers.
 *
 * NestJS equivalent:
 *
 *   export class CreateProductDto {
 *     @IsString()
 *     productName: string;
 *
 *     @IsNumber()
 *     productPrice: number;
 *     ...
 *   }
 *
 * Instead of passing 10 separate arguments, we pass ONE object.
 * Much cleaner, easier to read, and easier to validate.
 */
@Getter
@Setter
public class ProductCreateDto {
    private String productName;
    private String productBrand;
    private Integer productPrice;
    private Integer productStock;
    private ProductType productType;
    private ProductFragrance productFragrance;
    private ProductGender productGender;
    private ProductVolume productVolume;
    private String productDesc;
}
