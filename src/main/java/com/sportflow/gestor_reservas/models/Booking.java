package com.sportflow.gestor_reservas.models;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "bookings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"slotId", "userId"}))
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long tenantId;
    private Long slotId;
    private Long userId;

    @Enumerated(EnumType.STRING)
    private BookingStatus status;

    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    protected Booking() {}

    public static Booking create(Long tenantId, Long slotId, Long userId) {
        Booking booking = new Booking();
        booking.tenantId = tenantId;
        booking.slotId = slotId;
        booking.userId = userId;
        booking.status = BookingStatus.CONFIRMED;
        booking.createdAt = LocalDateTime.now();
        return booking;
    }
}
