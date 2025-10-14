package com.maternity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.maternity.model.Order;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public class AdminCreateOrderRequest {

    @NotNull(message = "Mother ID is required")
    private Long motherId;

    @NotNull(message = "Matron profile ID is required")
    private Long matronProfileId;

    @NotNull(message = "Start date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private Double totalPrice;

    private String address;

    private String notes;

    private Order.OrderStatus status;

    // Getters and Setters
    public Long getMotherId() {
        return motherId;
    }

    public void setMotherId(Long motherId) {
        this.motherId = motherId;
    }

    public Long getMatronProfileId() {
        return matronProfileId;
    }

    public void setMatronProfileId(Long matronProfileId) {
        this.matronProfileId = matronProfileId;
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

    public Order.OrderStatus getStatus() {
        return status;
    }

    public void setStatus(Order.OrderStatus status) {
        this.status = status;
    }
}
