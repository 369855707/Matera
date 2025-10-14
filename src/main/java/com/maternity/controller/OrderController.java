package com.maternity.controller;

import com.maternity.dto.CreateOrderRequest;
import com.maternity.dto.OrderDTO;
import com.maternity.model.Order;
import com.maternity.model.User;
import com.maternity.repository.UserRepository;
import com.maternity.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Booking order management")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final UserRepository userRepository;

    public OrderController(OrderService orderService, UserRepository userRepository) {
        this.orderService = orderService;
        this.userRepository = userRepository;
    }

    @Operation(summary = "Create a new order",
               description = "Create a booking order for a maternity matron")
    @PostMapping
    public ResponseEntity<OrderDTO> createOrder(
            @Valid @RequestBody CreateOrderRequest request,
            Authentication authentication) {
        // Get current user from authentication
        String identifier = authentication.getName();
        User mother = userRepository.findByEmail(identifier)
                .or(() -> userRepository.findByPhone(identifier))
                .or(() -> userRepository.findByWechatOpenId(identifier))
                .orElseThrow(() -> new RuntimeException("User not found"));

        OrderDTO createdOrder = orderService.createOrder(mother.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @Operation(summary = "Get orders by mother",
               description = "Retrieve all orders for a specific mother")
    @GetMapping("/mother/{motherId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByMother(@PathVariable Long motherId) {
        return ResponseEntity.ok(orderService.getOrdersByMother(motherId));
    }

    @Operation(summary = "Get orders by matron",
               description = "Retrieve all orders for a specific matron")
    @GetMapping("/matron/{matronId}")
    public ResponseEntity<List<OrderDTO>> getOrdersByMatron(@PathVariable Long matronId) {
        return ResponseEntity.ok(orderService.getOrdersByMatron(matronId));
    }

    @Operation(summary = "Get order by ID",
               description = "Retrieve detailed information about a specific order")
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @Operation(summary = "Update order status",
               description = "Change the status of an order (PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED)")
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
