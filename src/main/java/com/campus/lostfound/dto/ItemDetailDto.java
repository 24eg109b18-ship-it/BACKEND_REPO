package com.campus.lostfound.dto;

import java.util.List;

/** reporter = the viewer posted this item. canRespond = the viewer may start a new conversation. */
public record ItemDetailDto(ItemDto item, boolean reporter, boolean canRespond, List<ThreadDto> threads) {}
