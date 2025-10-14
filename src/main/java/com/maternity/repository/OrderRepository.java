package com.maternity.repository;

import com.maternity.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByMotherId(Long motherId);
    List<Order> findByMatronProfileId(Long matronProfileId);
    List<Order> findByMotherIdAndStatus(Long motherId, Order.OrderStatus status);
    List<Order> findByMatronProfileIdAndStatus(Long matronProfileId, Order.OrderStatus status);
    List<Order> findByStatus(Order.OrderStatus status);
    long countByStatus(Order.OrderStatus status);
}
