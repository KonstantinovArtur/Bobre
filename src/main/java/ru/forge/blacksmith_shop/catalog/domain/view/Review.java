package ru.forge.blacksmith_shop.catalog.domain.view;

import java.time.OffsetDateTime;

public record Review(
        Integer reviewId,
        String userLogin,
        Integer rating,
        String comment,
        OffsetDateTime createdAt
) {}
