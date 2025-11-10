package ru.forge.blacksmith_shop.api.items;

import jakarta.validation.constraints.*;

public record ItemDto(
        Long id,
        @NotBlank @Size(max = 150) String name,
        @Positive double price,
        @PositiveOrZero int stock
) {}
