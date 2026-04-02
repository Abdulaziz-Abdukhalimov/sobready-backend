package com.sobready.backend.dto;

import lombok.Getter;
import lombok.Setter;

/**
 * Matches your React OrderItemInput interface:
 *
 *   interface OrderItemInput {
 *     itemQuantity: number;
 *     itemPrice: number;
 *     productId: string;
 *   }
 */
@Getter
@Setter
public class OrderItemDto {
    private Integer itemQuantity;
    private Integer itemPrice;
    private Long productId;
}
