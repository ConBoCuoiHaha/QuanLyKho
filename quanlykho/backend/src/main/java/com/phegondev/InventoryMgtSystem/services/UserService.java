package com.phegondev.InventoryMgtSystem.services;

import com.phegondev.InventoryMgtSystem.dtos.DoiMatKhauYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.LoginRequest;
import com.phegondev.InventoryMgtSystem.dtos.RegisterRequest;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.UserDTO;
import com.phegondev.InventoryMgtSystem.models.User;

public interface UserService {
    PhanHoi registerUser(RegisterRequest registerRequest);

    PhanHoi loginUser(LoginRequest loginRequest);

    PhanHoi getAllUsers();

    User getCurrentLoggedInUser();

    PhanHoi getUserById(Long id);

    PhanHoi updateUser(Long id, UserDTO userDTO);

    PhanHoi deleteUser(Long id);

    PhanHoi layGiaoDichNguoiDung(Long id);

    PhanHoi doiMatKhau(DoiMatKhauYeuCau yeuCau);
}
