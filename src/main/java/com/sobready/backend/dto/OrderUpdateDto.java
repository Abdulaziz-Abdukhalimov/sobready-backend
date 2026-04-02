package com.sobready.backend.dto;

import com.sobready.backend.enums.OrderStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * Matches your React OrderUpdateInput interface:
 *
 *   interface OrderUpdateInput {
 *     orderId: string;
 *     orderStatus: OrderStatus;
 *   }
 */
@Getter
@Setter
public class OrderUpdateDto {
    private Long orderId;
    private OrderStatus orderStatus;
}
