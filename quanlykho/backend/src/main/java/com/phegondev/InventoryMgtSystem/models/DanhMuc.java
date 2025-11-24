package com.phegondev.InventoryMgtSystem.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "categories")
@Data
@Builder
public class DanhMuc {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên là bắt buộc")
    private String name;

    @OneToMany(mappedBy = "category")
    private List<SanPham> sanPhams;

    @Override
    public String toString() {
        return "DanhMuc{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
