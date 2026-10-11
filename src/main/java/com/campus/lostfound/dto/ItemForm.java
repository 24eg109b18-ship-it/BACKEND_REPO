package com.campus.lostfound.dto;

import com.campus.lostfound.model.ItemType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

/** Fields of the multipart "report item" request (the photo is a separate part). */
@Getter @Setter
public class ItemForm {
    @NotNull(message = "Choose lost or found")
    private ItemType type;
    @NotBlank(message = "Say what the item is") @Size(max = 120, message = "Keep it under 120 characters")
    private String title;
    @Size(max = 1000, message = "Keep the description under 1000 characters")
    private String description;
    @NotBlank(message = "Choose a category")
    private String category;
    @NotBlank(message = "Say where it was lost or found") @Size(max = 150, message = "Keep it under 150 characters")
    private String location;
    @NotNull(message = "Pick a date") @PastOrPresent(message = "The date cannot be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;
}
