package com.sportflow.gestor_reservas.user.dto;

import java.util.List;

public record PaginatedResponse<T>(
        List<T> content,
        int currentPage,
        int pageSize,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <T> PaginatedResponse<T> of(org.springframework.data.domain.Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }
}
