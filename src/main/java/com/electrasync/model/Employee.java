package com.electrasync.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false, length = 50)
    private String designation;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlySalary;

    @Column(nullable = false)
    private LocalDate joiningDate;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;

    // Optional link to this employee's system login account (if they need to use the software).
    // Not every employee needs a login — e.g. a delivery boy might not need system access.
    // EAGER because we display login status directly on the employees list page.
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public enum EmployeeStatus {
        ACTIVE, INACTIVE, ON_LEAVE
    }

    // Getters
    public Long getId() { return id; }
    public String getFullName() { return fullName; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getDesignation() { return designation; }
    public BigDecimal getMonthlySalary() { return monthlySalary; }
    public LocalDate getJoiningDate() { return joiningDate; }
    public String getAddress() { return address; }
    public EmployeeStatus getStatus() { return status; }
    public User getUser() { return user; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setDesignation(String designation) { this.designation = designation; }
    public void setMonthlySalary(BigDecimal monthlySalary) { this.monthlySalary = monthlySalary; }
    public void setJoiningDate(LocalDate joiningDate) { this.joiningDate = joiningDate; }
    public void setAddress(String address) { this.address = address; }
    public void setStatus(EmployeeStatus status) { this.status = status; }
    public void setUser(User user) { this.user = user; }
}
