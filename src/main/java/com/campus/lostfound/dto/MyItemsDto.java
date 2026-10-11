package com.campus.lostfound.dto;

import java.util.List;

public record MyItemsDto(List<ItemDto> reported, List<ItemDto> replied) {}
