package com.electrasync.model;

import jakarta.persistence.*;

@Entity
@Table(name = "brands")
public class Brand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(length = 100)
    private String country;

    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCountry() { return country; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setCountry(String country) { this.country = country; }
}
