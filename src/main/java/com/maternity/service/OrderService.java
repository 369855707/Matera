package com.maternity.service;

import com.maternity.dto.CreateOrderRequest;
import com.maternity.dto.OrderDTO;
import com.maternity.exception.ResourceNotFoundException;
import com.maternity.model.MatronProfile;
import com.maternity.model.Order;
import com.maternity.model.User;
import com.maternity.repository.MatronProfileRepository;
import com.maternity.repository.OrderRepository;
import com.maternity.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final MatronProfileRepository matronProfileRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                       MatronProfileRepository matronProfileRepository,
                       UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.matronProfileRepository = matronProfileRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByMother(Long motherId) {
        return orderRepository.findByMotherId(motherId).stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByMatron(Long matronProfileId) {
        return orderRepository.findByMatronProfileId(matronProfileId).stream()
                .map(OrderDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + id));
        return OrderDTO.fromEntity(order);
    }

    @Transactional
    public Order createOrder(Order order) {
        return orderRepository.save(order);
    }

    @Transactional
    public OrderDTO createOrder(Long motherId, CreateOrderRequest request) {
        // Verify mother exists
        User mother = userRepository.findById(motherId)
                .orElseThrow(() -> new ResourceNotFoundException("Mother not found with id: " + motherId));

        // Verify matron profile exists
        MatronProfile matronProfile = matronProfileRepository.findById(request.getMatronProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("Matron profile not found with id: " + request.getMatronProfileId()));

        // Calculate total price (price per month * number of months)
        long days = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        double months = days / 30.0;
        double totalPrice = matronProfile.getPricePerMonth() * months;

        // Create order
        Order order = new Order();
        order.setMother(mother);
        order.setMatronProfile(matronProfile);
        order.setStartDate(request.getStartDate());
        order.setEndDate(request.getEndDate());
        order.setAddress(request.getAddress());
        order.setNotes(request.getNotes());
        order.setTotalPrice(totalPrice);
        order.setStatus(Order.OrderStatus.PENDING);

        Order savedOrder = orderRepository.save(order);
        return OrderDTO.fromEntity(savedOrder);
    }

    @Transactional
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with id: " + orderId));

        order.setStatus(status);

        if (status == Order.OrderStatus.CONFIRMED) {
            order.setConfirmedAt(LocalDateTime.now());
        } else if (status == Order.OrderStatus.COMPLETED) {
            order.setCompletedAt(LocalDateTime.now());
        }

        Order updatedOrder = orderRepository.save(order);
        return OrderDTO.fromEntity(updatedOrder);
    }
}
