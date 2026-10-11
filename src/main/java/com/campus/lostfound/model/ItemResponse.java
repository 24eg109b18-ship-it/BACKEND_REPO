package com.campus.lostfound.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A private conversation about one item between the reporter and ONE responder.
 * "Owner side" = the person who lost the item, "finder side" = the person who has it.
 * The case closes only when both sides have confirmed the handover.
 */
@Entity
@Getter @Setter @NoArgsConstructor
public class ItemResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Item item;

    @ManyToOne(optional = false)
    private AppUser responder;

    /** The opening message. Kept because older rows have it and the column is NOT NULL. */
    @Column(nullable = false, length = 500)
    private String message;

    @Column(columnDefinition = "boolean default false not null")
    private boolean ownerConfirmed;      // the owner says "I got my item back"

    @Column(columnDefinition = "boolean default false not null")
    private boolean finderConfirmed;     // the finder says "I handed it over"

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "thread", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<ChatMessage> messages = new ArrayList<>();

    public boolean isParticipant(AppUser user) {
        return user.getId().equals(responder.getId()) || user.getId().equals(item.getReporter().getId());
    }

    /** LOST item: the reporter is the owner. FOUND item: the responder is the owner. */
    public boolean isOwnerSide(AppUser user) {
        boolean userIsReporter = item.getReporter().getId().equals(user.getId());
        return userIsReporter == (item.getType() == ItemType.LOST);
    }
}
