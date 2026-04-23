package de.emc.mitglieder.dto.member;

import java.util.List;

public record MemberListResponse(
        List<MemberListItemDto> items,
        PaginationDto pagination
) {
}