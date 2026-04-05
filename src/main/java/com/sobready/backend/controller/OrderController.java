package com.sobready.backend.controller;

import com.sobready.backend.dto.OrderItemDto;
import com.sobready.backend.dto.OrderUpdateDto;
import com.sobready.backend.entity.Member;
import com.sobready.backend.entity.Order;
import com.sobready.backend.enums.MemberType;
import com.sobready.backend.enums.OrderStatus;
import com.sobready.backend.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Autowired
    private OrderService orderService;

    /**
     * POST /order/create
     * React expects: result.data → Order object directly
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
     * React expects: result.data → Order[] array directly
     */
    @GetMapping("/all")
    public ResponseEntity<List<Order>> getMyOrders(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam OrderStatus orderStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int limit
    ) {
        Page<Order> orderPage = orderService.getMyOrders(
                currentMember.getId(), orderStatus, page, limit
        );

        return ResponseEntity.ok(orderPage.getContent());
    }

    /**
     * POST /order/update
     * React expects: result.data → Order object directly
     */
    @PostMapping("/update")
    public ResponseEntity<Order> updateOrder(
            @AuthenticationPrincipal Member currentMember,
            @RequestBody OrderUpdateDto dto
    ) {
        Order order = orderService.updateOrder(dto);
        return ResponseEntity.ok(order);
    }

    // ===================== ADMIN ENDPOINTS =====================

    /**
     * GET /order/admin/all — get all orders across all users (ADMIN only)
     */
    @GetMapping("/admin/all")
    public ResponseEntity<?> getAllOrders(
            @AuthenticationPrincipal Member currentMember,
            @RequestParam(required = false) OrderStatus orderStatus,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int limit
    ) {
        if (currentMember.getMemberType() != MemberType.ADMIN) {
            return ResponseEntity.status(403).body(java.util.Map.of("message", "Admin access required"));
        }

        return ResponseEntity.ok(orderService.getAllOrders(orderStatus, page, limit));
    }
}
