package com.maternity.controller;

import com.maternity.dto.AdminCreateOrderRequest;
import com.maternity.dto.AdminOrderDTO;
import com.maternity.model.Order;
import com.maternity.service.AdminOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Admin Order Management", description = "Admin endpoints for managing orders")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders with pagination")
    public ResponseEntity<Page<AdminOrderDTO>> getAllOrders(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(adminOrderService.getAllOrdersPaginated(pageable));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    public ResponseEntity<AdminOrderDTO> getOrderById(@PathVariable Long orderId) {
        return ResponseEntity.ok(adminOrderService.getOrderById(orderId));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve all orders with a specific status")
    public ResponseEntity<List<AdminOrderDTO>> getOrdersByStatus(@PathVariable Order.OrderStatus status) {
        return ResponseEntity.ok(adminOrderService.getOrdersByStatus(status));
    }

    @GetMapping("/mother/{motherId}")
    @Operation(summary = "Get orders by mother ID", description = "Retrieve all orders for a specific mother")
    public ResponseEntity<List<AdminOrderDTO>> getOrdersByMotherId(@PathVariable Long motherId) {
        return ResponseEntity.ok(adminOrderService.getOrdersByMotherId(motherId));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status", description = "Update the status of an order")
    public ResponseEntity<AdminOrderDTO> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestBody Map<String, String> statusUpdate) {
        Order.OrderStatus newStatus = Order.OrderStatus.valueOf(statusUpdate.get("status"));
        return ResponseEntity.ok(adminOrderService.updateOrderStatus(orderId, newStatus));
    }

    @DeleteMapping("/{orderId}")
    @Operation(summary = "Delete order", description = "Delete an order by ID")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        adminOrderService.deleteOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    @Operation(summary = "Get order statistics", description = "Get statistics about orders")
    public ResponseEntity<Map<String, Object>> getOrderStats() {
        Map<String, Object> stats = Map.of(
            "totalOrders", adminOrderService.getTotalOrdersCount(),
            "pendingOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.PENDING),
            "confirmedOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.CONFIRMED),
            "inProgressOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.IN_PROGRESS),
            "completedOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.COMPLETED),
            "cancelledOrders", adminOrderService.getOrdersCountByStatus(Order.OrderStatus.CANCELLED)
        );
        return ResponseEntity.ok(stats);
    }

    @PostMapping
    @Operation(summary = "Create order", description = "Admin creates an order for a mother")
    public ResponseEntity<AdminOrderDTO> createOrder(@Valid @RequestBody AdminCreateOrderRequest request) {
        return ResponseEntity.ok(adminOrderService.createOrder(request));
    }

    @PutMapping("/{orderId}")
    @Operation(summary = "Update order", description = "Update an existing order")
    public ResponseEntity<AdminOrderDTO> updateOrder(
            @PathVariable Long orderId,
            @Valid @RequestBody AdminCreateOrderRequest request) {
        return ResponseEntity.ok(adminOrderService.updateOrder(orderId, request));
    }
}
