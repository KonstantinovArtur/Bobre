// src/main/java/ru/forge/blacksmith_shop/catalog/dto/UserRow.java
package ru.forge.blacksmith_shop.users.dto;

import java.time.OffsetDateTime;

public record UserRow(
        Integer userId,
        String login,
        String email,
        String roleName,
        OffsetDateTime createdAt
) {}
