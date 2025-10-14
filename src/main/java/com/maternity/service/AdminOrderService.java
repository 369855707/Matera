package com.maternity.service;

import com.maternity.dto.AdminCreateOrderRequest;
import com.maternity.dto.AdminOrderDTO;
import com.maternity.model.MatronProfile;
import com.maternity.model.Order;
import com.maternity.model.User;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.OrderRepository;
import com.maternity.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final MatronProfileRepository matronProfileRepository;

    public AdminOrderService(OrderRepository orderRepository,
                            UserRepository userRepository,
                            MatronProfileRepository matronProfileRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.matronProfileRepository = matronProfileRepository;
    }

    public List<AdminOrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
            .map(AdminOrderDTO::new)
            .collect(Collectors.toList());
    }

    public Page<AdminOrderDTO> getAllOrdersPaginated(Pageable pageable) {
        return orderRepository.findAll(pageable)
            .map(AdminOrderDTO::new);
    }

    public AdminOrderDTO getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));
        return new AdminOrderDTO(order);
    }

    public List<AdminOrderDTO> getOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status).stream()
            .map(AdminOrderDTO::new)
            .collect(Collectors.toList());
    }

    public List<AdminOrderDTO> getOrdersByMotherId(Long motherId) {
        return orderRepository.findByMotherId(motherId).stream()
            .map(AdminOrderDTO::new)
            .collect(Collectors.toList());
    }

    @Transactional
    public AdminOrderDTO updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        order.setStatus(newStatus);

        if (newStatus == Order.OrderStatus.CONFIRMED && order.getConfirmedAt() == null) {
            order.setConfirmedAt(java.time.LocalDateTime.now());
        }

        if (newStatus == Order.OrderStatus.COMPLETED && order.getCompletedAt() == null) {
            order.setCompletedAt(java.time.LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        return new AdminOrderDTO(updatedOrder);
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (!orderRepository.existsById(orderId)) {
            throw new RuntimeException("Order not found with id: " + orderId);
        }
        orderRepository.deleteById(orderId);
    }

    public long getTotalOrdersCount() {
        return orderRepository.count();
    }

    public long getOrdersCountByStatus(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    @Transactional
    public AdminOrderDTO createOrder(AdminCreateOrderRequest request) {
        // Verify mother exists
        User mother = userRepository.findById(request.getMotherId())
            .orElseThrow(() -> new RuntimeException("Mother not found with id: " + request.getMotherId()));

        if (mother.getRole() != User.UserRole.MOTHER) {
            throw new RuntimeException("User is not a MOTHER");
        }

        // Verify matron profile exists
        MatronProfile matronProfile = matronProfileRepository.findById(request.getMatronProfileId())
            .orElseThrow(() -> new RuntimeException("Matron profile not found with id: " + request.getMatronProfileId()));

        // Create order
        Order order = new Order();
        order.setMother(mother);
        order.setMatronProfile(matronProfile);
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setTotalPrice(request.getTotalPrice());
        order.setAddress(request.getAddress());
        order.setNotes(request.getNotes());
        order.setStatus(request.getStatus() != null ? request.getStatus() : Order.OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        return new AdminOrderDTO(savedOrder);
    }

    @Transactional
    public AdminOrderDTO updateOrder(Long orderId, AdminCreateOrderRequest request) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new RuntimeException("Order not found with id: " + orderId));

        // Update mother if changed
        if (request.getMotherId() != null && !request.getMotherId().equals(order.getMother().getId())) {
            User mother = userRepository.findById(request.getMotherId())
                .orElseThrow(() -> new RuntimeException("Mother not found with id: " + request.getMotherId()));
            if (mother.getRole() != User.UserRole.MOTHER) {
                throw new RuntimeException("User is not a MOTHER");
            }
            order.setMother(mother);
        }

        // Update matron profile if changed
        if (request.getMatronProfileId() != null && !request.getMatronProfileId().equals(order.getMatronProfile().getId())) {
            MatronProfile matronProfile = matronProfileRepository.findById(request.getMatronProfileId())
                .orElseThrow(() -> new RuntimeException("Matron profile not found with id: " + request.getMatronProfileId()));
            order.setMatronProfile(matronProfile);
        }

        // Update other fields
        if (request.getStartDate() != null) {
            order.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            order.setEndDate(request.getEndDate());
        }
        if (request.getTotalPrice() != null) {
            order.setTotalPrice(request.getTotalPrice());
        }
        if (request.getAddress() != null) {
            order.setAddress(request.getAddress());
        }
        if (request.getNotes() != null) {
            order.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            order.setStatus(request.getStatus());

            if (request.getStatus() == Order.OrderStatus.CONFIRMED && order.getConfirmedAt() == null) {
                order.setConfirmedAt(java.time.LocalDateTime.now());
            }

            if (request.getStatus() == Order.OrderStatus.COMPLETED && order.getCompletedAt() == null) {
                order.setCompletedAt(java.time.LocalDateTime.now());
            }
        }

        Order updatedOrder = orderRepository.save(order);
        return new AdminOrderDTO(updatedOrder);
    }
}
