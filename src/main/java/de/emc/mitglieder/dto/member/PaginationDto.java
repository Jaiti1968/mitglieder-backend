package de.emc.mitglieder.dto.member;

public record PaginationDto(
        int page,
        int pageSize,
        long totalItems,
        int totalPages
) {
}