package com.campus.lostfound.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/** One message inside a private conversation between the reporter and one responder. */
@Entity
@Getter @Setter @NoArgsConstructor
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private ItemResponse thread;

    @ManyToOne(optional = false)
    private AppUser sender;

    @Column(nullable = false, length = 500)
    private String body;

    private LocalDateTime createdAt = LocalDateTime.now();
}
