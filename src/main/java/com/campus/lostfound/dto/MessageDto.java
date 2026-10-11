package com.campus.lostfound.dto;

import java.time.LocalDateTime;

public record MessageDto(Long id, String senderName, boolean mine, String text, LocalDateTime createdAt) {}
