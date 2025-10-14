package com.maternity.dto;

import com.maternity.model.Order;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class AdminOrderDTO {

    private Long id;
    private Long motherId;
    private String motherName;
    private String motherPhone;
    private String motherEmail;
    private Long matronProfileId;
    private String matronName;
    private String matronPhone;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPrice;
    private String status;
    private String address;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime confirmedAt;
    private LocalDateTime completedAt;

    // Constructors
    public AdminOrderDTO() {
    }

    public AdminOrderDTO(Order order) {
        this.id = order.getId();
        this.motherId = order.getMother().getId();
        this.motherName = order.getMother().getName();
        this.motherPhone = order.getMother().getPhone();
        this.motherEmail = order.getMother().getEmail();
        this.matronProfileId = order.getMatronProfile().getId();
        this.matronName = order.getMatronProfile().getUser().getName();
        this.matronPhone = order.getMatronProfile().getUser().getPhone();
        this.startDate = order.getStartDate();
        this.endDate = order.getEndDate();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus().name();
        this.address = order.getAddress();
        this.notes = order.getNotes();
        this.createdAt = order.getCreatedAt();
        this.updatedAt = order.getUpdatedAt();
        this.confirmedAt = order.getConfirmedAt();
        this.completedAt = order.getCompletedAt();
    }

    // Getters and Setters
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

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getMotherPhone() {
        return motherPhone;
    }

    public void setMotherPhone(String motherPhone) {
        this.motherPhone = motherPhone;
    }

    public String getMotherEmail() {
        return motherEmail;
    }

    public void setMotherEmail(String motherEmail) {
        this.motherEmail = motherEmail;
    }

    public Long getMatronProfileId() {
        return matronProfileId;
    }

    public void setMatronProfileId(Long matronProfileId) {
        this.matronProfileId = matronProfileId;
    }

    public String getMatronName() {
        return matronName;
    }

    public void setMatronName(String matronName) {
        this.matronName = matronName;
    }

    public String getMatronPhone() {
        return matronPhone;
    }

    public void setMatronPhone(String matronPhone) {
        this.matronPhone = matronPhone;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
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

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
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
}
