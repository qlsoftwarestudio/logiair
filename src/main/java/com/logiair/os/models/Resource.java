package com.logiair.os.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "resources",
        indexes = @Index(name = "idx_resource_tenant", columnList = "tenantId"))
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private Long venueId;

    private String name;
    private String description;

    private Integer capacity;

    @Enumerated(EnumType.STRING)
    private ResourceStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Resource() {}

    public boolean isActive() {
        return status == ResourceStatus.ACTIVE;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getId() {
        return id;
    }
}
