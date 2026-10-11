package com.campus.lostfound.dto;

import com.campus.lostfound.model.Item;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ItemDto(Long id, String type, String title, String description, String category, String location,
                      LocalDate eventDate, boolean hasImage, String status, String reporterName,
                      LocalDateTime createdAt, LocalDateTime closedAt, String closingNote, int responseCount) {

    public static ItemDto from(Item i) {
        return new ItemDto(i.getId(), i.getType().name(), i.getTitle(), i.getDescription(), i.getCategory(),
                i.getLocation(), i.getEventDate(), i.isHasImage(), i.getStatus().name(), i.getReporter().getName(),
                i.getCreatedAt(), i.getClosedAt(), i.getClosingNote(), i.getResponses().size());
    }
}
