package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "nha_cung_cap")
@Data
@Builder
public class NhaCungCap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Ten la bat buoc")
    private String name;

    @NotBlank(message = "Thong tin lien he la bat buoc")
    private String contactInfo;

    private String phone;

    private String email;

    private String address;
}
