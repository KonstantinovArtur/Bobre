// src/main/java/ru/forge/blacksmith_shop/audit/repo/LogRow.java
package ru.forge.blacksmith_shop.audit.repo;

import java.time.Instant;

public interface LogRow {
    Integer getId();           // logs.log_id
    java.time.Instant getCreatedAt(); // <-- именно Instant
    Integer getUserId();       // users.user_id
    String  getUserLogin();    // users.login
    String  getOperationName();// operations.operation_name
    String  getDetails();      // logs.details
}
