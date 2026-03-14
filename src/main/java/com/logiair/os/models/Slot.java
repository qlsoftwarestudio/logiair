package com.logiair.os.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "slots",
        indexes = @Index(name = "idx_resource_start", columnList = "resourceId,startTime"))
public class Slot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private Long resourceId;

    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Integer capacity;
    private Integer availableCapacity;

    @Enumerated(EnumType.STRING)
    private SlotStatus status;

    @Version
    private Integer version;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    protected Slot() {}

    public static Slot create(Resource resource,
                              LocalDateTime start,
                              LocalDateTime end) {

        if (!resource.isActive())
            throw new IllegalStateException("Resource inactive");

        if (end.isBefore(start))
            throw new IllegalArgumentException("Invalid time range");

        Slot slot = new Slot();
        slot.tenantId = resource.getTenantId();
        slot.resourceId = resource.getId();
        slot.startTime = start;
        slot.endTime = end;
        slot.capacity = resource.getCapacity();
        slot.availableCapacity = resource.getCapacity();
        slot.status = SlotStatus.OPEN;
        slot.createdAt = LocalDateTime.now();

        return slot;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }
}
