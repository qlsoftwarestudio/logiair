package com.sportflow.gestor_reservas.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "venues")
public class Venue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;

    private String name;
    private String address;

    @Enumerated(EnumType.STRING)
    private VenueStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Venue() {}

    public Long getTenantId() {
        return tenantId;
    }

    public Long getId() {
        return id;
    }
}
