package com.sobready.backend.repository;

import com.sobready.backend.entity.Order;
import com.sobready.backend.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Find orders by member ID and status, with pagination.
     * Spring generates: WHERE member_id = ? AND order_status = ? ORDER BY ... LIMIT ? OFFSET ?
     *
     * Your React calls: GET /order/all?orderStatus=PENDING&page=1&limit=5
     */
    Page<Order> findByMemberIdAndOrderStatus(Long memberId, OrderStatus orderStatus, Pageable pageable);

    /**
     * Find all orders by status (ADMIN — no member filter).
     */
    Page<Order> findByOrderStatus(OrderStatus orderStatus, Pageable pageable);
}
