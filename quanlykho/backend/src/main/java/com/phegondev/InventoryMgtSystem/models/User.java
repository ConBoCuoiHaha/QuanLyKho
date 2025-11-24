package com.phegondev.InventoryMgtSystem.models;

import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
@Data
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên là bắt buộc")
    private String name;

    @Column(unique = true)
    @NotBlank(message = "Email là bắt buộc")
    private String email;

    @NotBlank(message = "Mật khẩu là bắt buộc")
    private String password;

    @NotBlank(message = "Số điện thoại là bắt buộc")
    @Column(name = "phone_number")
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    private VaiTroNguoiDung role;

    @OneToMany(mappedBy = "user")
    private List<GiaoDich> giaoDichs;

    @Column(name = "created_at")
    private final LocalDateTime createdAt = LocalDateTime.now();

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", role=" + role +
                ", createdAt=" + createdAt +
                '}';
    }
}
