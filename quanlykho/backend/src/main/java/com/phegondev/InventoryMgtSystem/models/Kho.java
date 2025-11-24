package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "warehouses")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Kho {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên kho là bắt buộc")
    private String name;

    private String address;
    private String manager;
}

