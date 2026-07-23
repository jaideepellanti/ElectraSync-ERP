package com.electrasync.model;

import jakarta.persistence.*;

@Entity
@Table(name = "vendors")
public class Vendor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 100)
    private String contactPerson;

    // Phone is unique - no two vendors can have the same number
    @Column(nullable = false, unique = true, length = 15)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String address;

    @Column(length = 50)
    private String city;

    @Column(length = 50)
    private String state;

    @Column(nullable = false)
    private boolean active = true;

    // Getters
    public Long getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getContactPerson() { return contactPerson; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getState() { return state; }
    public boolean isActive() { return active; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setEmail(String email) { this.email = email; }
    public void setAddress(String address) { this.address = address; }
    public void setCity(String city) { this.city = city; }
    public void setState(String state) { this.state = state; }
    public void setActive(boolean active) { this.active = active; }
}
