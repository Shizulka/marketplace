package com.cosmocats.marketplace.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "categories",
        uniqueConstraints = @UniqueConstraint(name = "uq_categories_name", columnNames = "name"))
public class CategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "categories_seq_gen")
    @SequenceGenerator(name = "categories_seq_gen", sequenceName = "categories_seq", allocationSize = 1)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    protected CategoryEntity() {
    }

    public CategoryEntity(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public Long getId() { return id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }
}
