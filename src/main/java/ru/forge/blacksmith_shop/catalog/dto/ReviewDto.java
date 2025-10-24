package ru.forge.blacksmith_shop.catalog.dto;

import java.time.OffsetDateTime;

public record ReviewDto(
        Integer reviewId,
        String userLogin,
        Integer rating,
        String comment,
        OffsetDateTime createdAt
) {}
