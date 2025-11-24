package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.DoiMatKhauYeuCau;
import com.phegondev.InventoryMgtSystem.dtos.LoginRequest;
import com.phegondev.InventoryMgtSystem.dtos.RegisterRequest;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.UserDTO;
import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import com.phegondev.InventoryMgtSystem.exceptions.InvalidCredentialsException;
import com.phegondev.InventoryMgtSystem.exceptions.ValidationException;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.User;
import com.phegondev.InventoryMgtSystem.repositories.UserRepository;
import com.phegondev.InventoryMgtSystem.security.JwtUtils;
import com.phegondev.InventoryMgtSystem.services.UserService;
import com.phegondev.InventoryMgtSystem.utils.PasswordValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final JwtUtils jwtUtils;


    @Override
    public PhanHoi registerUser(RegisterRequest registerRequest) {

        VaiTroNguoiDung role = VaiTroNguoiDung.MANAGER;

        if (registerRequest.getRole() != null) {
            role = registerRequest.getRole();
        }

        User userToSave = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .phoneNumber(registerRequest.getPhoneNumber())
                .role(role)
                .build();

        userRepository.save(userToSave);

        return PhanHoi.builder()
                .status(200)
                .message("Đăng ký người dùng thành công")
                .build();
    }

    @Override
    public PhanHoi loginUser(LoginRequest loginRequest) {

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy email"));

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Mật khẩu không chính xác");
        }
        String token = jwtUtils.generateToken(user.getEmail());

        return PhanHoi.builder()
                .status(200)
                .message("Đăng nhập thành công")
                .role(user.getRole())
                .token(token)
                .expirationTime("6 tháng")
                .build();
    }

    @Override
    public PhanHoi getAllUsers() {

        List<User> users = userRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));

        users.forEach(user -> user.setGiaoDichs(null));

        List<UserDTO> userDTOS = modelMapper.map(users, new TypeToken<List<UserDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .users(userDTOS)
                .build();
    }

    @Override
    public User getCurrentLoggedInUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        String email = authentication.getName();

        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        user.setGiaoDichs(null);

        return user;
    }

    @Override
    public PhanHoi getUserById(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        userDTO.setGiaoDichs(null);

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .user(userDTO)
                .build();
    }

    @Override
    public PhanHoi updateUser(Long id, UserDTO userDTO) {

        User existingUser = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (userDTO.getEmail() != null) existingUser.setEmail(userDTO.getEmail());
        if (userDTO.getPhoneNumber() != null) existingUser.setPhoneNumber(userDTO.getPhoneNumber());
        if (userDTO.getName() != null) existingUser.setName(userDTO.getName());
        if (userDTO.getRole() != null) existingUser.setRole(userDTO.getRole());

        if (userDTO.getPassword() != null && !userDTO.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        }
        userRepository.save(existingUser);

        return PhanHoi.builder()
                .status(200)
                .message("Cập nhật người dùng thành công")
                .build();
    }

    @Override
    public PhanHoi deleteUser(Long id) {
        userRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        userRepository.deleteById(id);

        return PhanHoi.builder()
                .status(200)
                .message("Xóa người dùng thành công")
                .build();

    }

    @Override
    public PhanHoi layGiaoDichNguoiDung(Long id) {

        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        UserDTO userDTO = modelMapper.map(user, UserDTO.class);

        if (userDTO.getGiaoDichs() != null) {
            userDTO.getGiaoDichs().forEach(giaoDichDTO -> {
                giaoDichDTO.setUser(null);
                giaoDichDTO.setNhaCungCap(null);
            });
        }

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .user(userDTO)
                .build();
    }
    @Override
    public PhanHoi doiMatKhau(DoiMatKhauYeuCau yeuCau) {
        if (yeuCau == null) {
            throw new ValidationException("Yeu cau doi mat khau khong hop le");
        }
        String oldPassword = safeTrim(yeuCau.getMatKhauCu());
        String newPassword = safeTrim(yeuCau.getMatKhauMoi());
        String confirmPassword = safeTrim(yeuCau.getXacNhanMatKhau());

        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            throw new ValidationException("Vui long nhap day du thong tin mat khau");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new ValidationException("Mat khau moi va xac nhan khong khop");
        }
        if (newPassword.equals(oldPassword)) {
            throw new ValidationException("Mat khau moi phai khac mat khau cu");
        }

        PasswordValidator.validate(newPassword);

        User currentUser = getCurrentLoggedInUser();
        if (!passwordEncoder.matches(oldPassword, currentUser.getPassword())) {
            throw new InvalidCredentialsException("Mat khau cu khong dung");
        }

        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(currentUser);

        return PhanHoi.builder()
                .status(200)
                .message("Doi mat khau thanh cong")
                .build();
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
