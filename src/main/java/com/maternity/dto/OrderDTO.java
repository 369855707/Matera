package com.maternity.dto;

import com.maternity.model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrderDTO {
    private Long id;
    private Long motherId;
    private Long matronId;
    private String matronName;
    private String matronAvatar;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private Order.OrderStatus status;
    private String address;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    public OrderDTO() {
    }

    public OrderDTO(Long id, Long motherId, Long matronId, String matronName, String matronAvatar, LocalDate startDate, LocalDate endDate, Double totalPrice, Order.OrderStatus status, String address, String notes, LocalDateTime createdAt, LocalDateTime confirmedAt, LocalDateTime completedAt) {
        this.id = id;
        this.motherId = motherId;
        this.matronId = matronId;
        this.matronName = matronName;
        this.matronAvatar = matronAvatar;
        this.startDate = startDate;
        this.endDate = endDate;
        this.totalPrice = totalPrice;
        this.status = status;
        this.address = address;
        this.notes = notes;
        this.createdAt = createdAt;
        this.confirmedAt = confirmedAt;
        this.completedAt = completedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getMotherId() {
        return motherId;
    }

    public void setMotherId(Long motherId) {
        this.motherId = motherId;
    }

    public Long getMatronId() {
        return matronId;
    }

    public void setMatronId(Long matronId) {
        this.matronId = matronId;
    }

    public String getMatronName() {
        return matronName;
    }

    public void setMatronName(String matronName) {
        this.matronName = matronName;
    }

    public String getMatronAvatar() {
        return matronAvatar;
    }

    public void setMatronAvatar(String matronAvatar) {
        this.matronAvatar = matronAvatar;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getConfirmedAt() {
        return confirmedAt;
    }

    public void setConfirmedAt(LocalDateTime confirmedAt) {
        this.confirmedAt = confirmedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public static OrderDTO fromEntity(Order order) {
        return new OrderDTO(
                order.getId(),
                order.getMother().getId(),
                order.getMatronProfile().getId(),
                order.getMatronProfile().getUser().getName(),
                order.getMatronProfile().getUser().getAvatar(),
                order.getStartDate(),
                order.getEndDate(),
                order.getTotalPrice(),
                order.getStatus(),
                order.getAddress(),
                order.getNotes(),
                order.getCreatedAt(),
                order.getConfirmedAt(),
                order.getCompletedAt()
        );
    }
}
