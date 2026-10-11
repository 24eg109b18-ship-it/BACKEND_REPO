package com.campus.lostfound.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter @NoArgsConstructor
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ItemType type;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String description;

    private String category;
    private String location;
    private LocalDate eventDate;
    private boolean hasImage;

    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private ItemStatus status = ItemStatus.OPEN;

    @ManyToOne(optional = false)
    private AppUser reporter;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime closedAt;

    @Column(length = 500)
    private String closingNote;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC")
    private List<ItemResponse> responses = new ArrayList<>();
}
