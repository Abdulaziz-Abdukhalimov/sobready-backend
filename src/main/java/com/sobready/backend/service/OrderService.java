package com.sobready.backend.service;

import com.sobready.backend.dto.OrderItemDto;
import com.sobready.backend.dto.OrderUpdateDto;
import com.sobready.backend.entity.Member;
import com.sobready.backend.entity.Order;
import com.sobready.backend.entity.OrderItem;
import com.sobready.backend.entity.Product;
import com.sobready.backend.enums.OrderStatus;
import com.sobready.backend.repository.MemberRepository;
import com.sobready.backend.repository.OrderRepository;
import com.sobready.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * NestJS equivalent:
 *
 *   @Injectable()
 *   export class OrderService {
 *     constructor(
 *       @InjectRepository(Order) private orderRepo: Repository<Order>,
 *       @InjectRepository(Product) private productRepo: Repository<Product>,
 *     ) {}
 *   }
 */
@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private MemberRepository memberRepository;

    /**
     * Create a new order from cart items.
     *
     * @Transactional = if anything fails, ALL database changes are rolled back.
     *   Like a database transaction in NestJS:
     *     await queryRunner.startTransaction();
     *     try { ... await queryRunner.commitTransaction(); }
     *     catch { await queryRunner.rollbackTransaction(); }
     *
     *   In Spring, the annotation does all of that automatically.
     *
     * Flow:
     * 1. Calculate total from items
     * 2. Calculate delivery fee (free if total > 1000)
     * 3. Create order + order items in one transaction
     * 4. Decrease product stock, increase sold count
     * 5. Add points to member
     */
    @Transactional
    public Order createOrder(Member member, List<OrderItemDto> items) {
        // Validate items
        if (items == null || items.isEmpty()) {
            throw new RuntimeException("Order must have at least one item");
        }

        // Calculate items subtotal
        int itemsTotal = 0;
        for (OrderItemDto item : items) {
            itemsTotal += item.getItemPrice() * item.getItemQuantity();
        }

        // Free delivery for orders over $1000
        int delivery = itemsTotal >= 1000 ? 0 : 50;

        // orderTotal includes delivery — frontend shows:
        //   Subtotal = orderTotal - orderDelivery (items only)
        //   Total = orderTotal (items + delivery)
        int orderTotal = itemsTotal + delivery;

        // Build order
        Order order = Order.builder()
                .memberId(member.getId())
                .orderTotal(orderTotal)
                .orderDelivery(delivery)
                .build();

        // Build order items and link to order
        List<OrderItem> orderItems = new ArrayList<>();
        for (OrderItemDto dto : items) {
            OrderItem item = OrderItem.builder()
                    .itemQuantity(dto.getItemQuantity())
                    .itemPrice(dto.getItemPrice())
                    .productId(dto.getProductId())
                    .order(order)
                    .build();
            orderItems.add(item);
        }
        order.setOrderItems(orderItems);

        // Save order (cascade saves order items too)
        order = orderRepository.save(order);

        // Update product stock and sold count
        for (OrderItemDto dto : items) {
            Product product = productRepository.findById(dto.getProductId()).orElse(null);
            if (product != null) {
                product.setProductStock(product.getProductStock() - dto.getItemQuantity());
                product.setProductSoldCount(product.getProductSoldCount() + dto.getItemQuantity());
                productRepository.save(product);
            }
        }

        // Add points to member (1 point per $10 spent)
        int points = itemsTotal / 10;
        member.setMemberPoints(member.getMemberPoints() + points);
        memberRepository.save(member);

        // Load product data for response
        populateProductsData(order);

        return order;
    }

    /**
     * Get orders for a member, filtered by status with pagination.
     * Your React calls: GET /order/all?orderStatus=PENDING&page=1&limit=5
     */
    public Page<Order> getMyOrders(Long memberId, OrderStatus orderStatus, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Order> orders = orderRepository.findByMemberIdAndOrderStatus(memberId, orderStatus, pageable);

        // Populate product data for each order
        orders.getContent().forEach(this::populateProductsData);

        return orders;
    }

    /**
     * Update order status (e.g., PENDING → PROCESSING → DELIVERED).
     */
    @Transactional
    public Order updateOrder(OrderUpdateDto dto) {
        Order order = orderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setOrderStatus(dto.getOrderStatus());
        order = orderRepository.save(order);

        populateProductsData(order);
        return order;
    }

    /**
     * Load product details for each order item.
     * Your React frontend expects: order.productsData = [Product, Product, ...]
     *
     * This is like a "virtual populate" in Mongoose or
     * "eager loading" in TypeORM: { relations: ["orderItems", "orderItems.product"] }
     */
    /**
     * Get all orders across all users (ADMIN).
     */
    public List<Order> getAllOrders(OrderStatus orderStatus, int page, int limit) {
        Pageable pageable = PageRequest.of(page - 1, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orders;

        if (orderStatus != null) {
            orders = orderRepository.findByOrderStatus(orderStatus, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        orders.getContent().forEach(this::populateProductsData);
        return orders.getContent();
    }

    private void populateProductsData(Order order) {
        List<Product> products = new ArrayList<>();
        for (OrderItem item : order.getOrderItems()) {
            productRepository.findById(item.getProductId()).ifPresent(products::add);
        }
        order.setProductsData(products);
    }
}
