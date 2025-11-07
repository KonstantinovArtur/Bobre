// src/main/java/ru/forge/blacksmith_shop/audit/domain/DummyLogEntity.java
package ru.forge.blacksmith_shop.audit.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "logs")
public class DummyLogEntity {
    @Id
    @Column(name = "log_id")
    private Integer id;
}
