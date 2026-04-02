package com.sobready.backend.controller;

import com.sobready.backend.dto.OrderItemDto;
import com.sobready.backend.dto.OrderUpdateDto;
import com.sobready.backend.entity.Member;
import com.sobready.backend.entity.Order;
import com.sobready.backend.enums.OrderStatus;
import com.sobready.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * NestJS equivalent:
 *
 *   @Controller('order')
 *   @UseGuards(JwtAuthGuard)       ← all endpoints require auth
 *   export class OrderController {
 *     @Post('create')
 *     async create(@Req() req, @Body() items: OrderItemInput[]) { ... }
 *
 *     @Get('all')
 *     async getMyOrders(@Req() req, @Query() query: OrderInquiry) { ... }
 *   }
 *
 * All order endpoints require authentication (SecurityConfig: anyRequest().authenticated()).
 */
@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * POST /order/create
     * Body: OrderItemDto[] (array of items from the cart)
     *
     * Your React sends the cart items array directly as the request body.
     */
    @PostMapping("/create")
    public ResponseEntity<Order> createOrder(
            @AuthenticationPrincipal Member currentMember,
            @RequestBody List<OrderItemDto> items
    ) {
        Order order = orderService.createOrder(currentMember, items);
        return ResponseEntity.ok(order);
    }

    /**
     * GET /order/all?orderStatus=PENDING&page=1&limit=5
     */
    @GetMapping("/all")
    public ResponseEntity<Map<String, Object>> getMyOrders(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam OrderStatus orderStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        Page<Order> orderPage = orderService.getMyOrders(
                currentMember.getId(), orderStatus, page, limit
        );

        Map<String, Object> response = new HashMap<>();
        response.put("list", orderPage.getContent());
        response.put("total", orderPage.getTotalElements());
        response.put("page", page);
        response.put("limit", limit);

        return ResponseEntity.ok(response);
    }

    /**
     * POST /order/update
     * Body: { orderId, orderStatus }
     */
    @PostMapping("/update")
    public ResponseEntity<Order> updateOrder(
            @AuthenticationPrincipal Member currentMember,
            @RequestBody OrderUpdateDto dto
    ) {
        Order order = orderService.updateOrder(dto);
        return ResponseEntity.ok(order);
    }
}
