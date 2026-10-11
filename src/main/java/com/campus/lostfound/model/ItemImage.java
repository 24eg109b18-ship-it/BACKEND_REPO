package com.campus.lostfound.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Photo bytes kept in a separate table so listing items never loads images. */
@Entity
@Getter @Setter @NoArgsConstructor
public class ItemImage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long itemId;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false, length = 5_242_880)
    private byte[] data;
}
