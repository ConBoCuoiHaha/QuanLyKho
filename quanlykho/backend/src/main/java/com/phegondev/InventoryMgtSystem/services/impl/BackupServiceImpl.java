package com.phegondev.InventoryMgtSystem.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.phegondev.InventoryMgtSystem.dtos.CategoryDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhachHangDTO;
import com.phegondev.InventoryMgtSystem.dtos.KhoDTO;
import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.backup.*;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.BackupService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupServiceImpl implements BackupService {

    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;
    private final DanhMucRepository danhMucRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final KhachHangRepository khachHangRepository;
    private final KhoRepository khoRepository;
    private final SanPhamRepository sanPhamRepository;
    private final TonKhoRepository tonKhoRepository;
    private final LoHangRepository loHangRepository;
    private final GiaoDichRepository giaoDichRepository;
    private final UserRepository userRepository;

    @Value("${backup.dir:backups}")
    private String backupDir;

    private Path resolveDir() throws IOException {
        Path dir = Paths.get(backupDir);
        if (!Files.exists(dir)) {
            Files.createDirectories(dir);
        }
        return dir;
    }

    @Override
    public PhanHoi taoBanSaoLuu() {
        try {
            Path dir = resolveDir();
            String fileName = "qlk-backup-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".json";
            Path filePath = dir.resolve(fileName);
            DuLieuSaoLuuDTO snapshot = buildSnapshot();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), snapshot);
            return PhanHoi.builder()
                    .status(200)
                    .message("Da tao ban sao luu thanh cong")
                    .tepTin(filePath.toAbsolutePath().toString())
                    .build();
        } catch (IOException ex) {
            log.error("Khong the tao backup: {}", ex.getMessage());
            return PhanHoi.builder()
                    .status(500)
                    .message("Khong the tao backup: " + ex.getMessage())
                    .build();
        }
    }

    @Override
    public List<String> danhSachBanSaoLuu() {
        try {
            Path dir = resolveDir();
            if (!Files.exists(dir)) {
                return List.of();
            }
            return Files.list(dir)
                    .filter(Files::isRegularFile)
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
        } catch (IOException ex) {
            log.error("Khong the doc danh sach backup: {}", ex.getMessage());
            return List.of();
        }
    }

    @Override
    @Transactional
    public PhanHoi khoiPhucDuLieu(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return PhanHoi.builder().status(400).message("Vui long tai len tep JSON backup").build();
        }
        try (InputStream inputStream = file.getInputStream()) {
            DuLieuSaoLuuDTO snapshot = objectMapper.readValue(inputStream, DuLieuSaoLuuDTO.class);
            restoreFromSnapshot(snapshot);
            return PhanHoi.builder()
                    .status(200)
                    .message("Khoi phuc du lieu thanh cong")
                    .build();
        } catch (IOException ex) {
            log.error("Khoi phuc that bai: {}", ex.getMessage());
            return PhanHoi.builder()
                    .status(500)
                    .message("Khoi phuc that bai: " + ex.getMessage())
                    .build();
        }
    }

    private DuLieuSaoLuuDTO buildSnapshot() {
        List<CategoryDTO> danhMucs = modelMapper.map(danhMucRepository.findAll(),
                new TypeToken<List<CategoryDTO>>() {
                }.getType());
        List<NhaCungCapDTO> nhaCungCaps = modelMapper.map(nhaCungCapRepository.findAll(),
                new TypeToken<List<NhaCungCapDTO>>() {
                }.getType());
        List<KhachHangDTO> khachHangs = modelMapper.map(khachHangRepository.findAll(),
                new TypeToken<List<KhachHangDTO>>() {
                }.getType());
        List<KhoDTO> khos = modelMapper.map(khoRepository.findAll(),
                new TypeToken<List<KhoDTO>>() {
                }.getType());
        List<UserBackupDTO> nguoiDungs = userRepository.findAll().stream()
                .map(user -> UserBackupDTO.builder()
                        .name(user.getName())
                        .email(user.getEmail())
                        .password(user.getPassword())
                        .phoneNumber(user.getPhoneNumber())
                        .role(user.getRole())
                        .build())
                .toList();

        List<SanPhamBackupDTO> sanPhams = sanPhamRepository.findAll().stream()
                .map(sp -> SanPhamBackupDTO.builder()
                        .name(sp.getName())
                        .sku(sp.getSku())
                        .price(sp.getPrice())
                        .stockQuantity(sp.getStockQuantity())
                        .minStock(sp.getMinStock())
                        .reservedQuantity(sp.getReservedQuantity())
                        .description(sp.getDescription())
                        .expiryDate(sp.getExpiryDate())
                        .imageUrl(sp.getImageUrl())
                        .categoryName(sp.getCategory() != null ? sp.getCategory().getName() : null)
                        .build())
                .toList();

        List<TonKhoBackupDTO> tonKhos = tonKhoRepository.findAll().stream()
                .map(tk -> TonKhoBackupDTO.builder()
                        .sanPhamSku(tk.getSanPham().getSku())
                        .khoName(tk.getWarehouse().getName())
                        .quantity(tk.getQuantity())
                        .build())
                .toList();

        List<LoHangBackupDTO> loHangs = loHangRepository.findAll().stream()
                .map(lh -> LoHangBackupDTO.builder()
                        .sanPhamSku(lh.getSanPham().getSku())
                        .lotNumber(lh.getLotNumber())
                        .receivedDate(lh.getReceivedDate())
                        .expiryDate(lh.getExpiryDate())
                        .quantityRemaining(lh.getQuantityRemaining())
                        .build())
                .toList();

        List<GiaoDichBackupDTO> giaoDichs = giaoDichRepository.findAll().stream()
                .map(gd -> GiaoDichBackupDTO.builder()
                        .transactionType(gd.getTransactionType())
                        .status(gd.getStatus())
                        .totalProducts(gd.getTotalProducts())
                        .totalPrice(gd.getTotalPrice())
                        .description(gd.getDescription())
                        .note(gd.getNote())
                        .createdAt(gd.getCreatedAt())
                        .sanPhamSku(gd.getSanPham() != null ? gd.getSanPham().getSku() : null)
                        .userEmail(gd.getUser() != null ? gd.getUser().getEmail() : null)
                        .nhaCungCapEmail(gd.getNhaCungCap() != null ? gd.getNhaCungCap().getEmail() : null)
                        .khachHangEmail(gd.getCustomer() != null ? gd.getCustomer().getEmail() : null)
                        .build())
                .toList();

        return DuLieuSaoLuuDTO.builder()
                .danhMucs(danhMucs)
                .nhaCungCaps(nhaCungCaps)
                .khachHangs(khachHangs)
                .khos(khos)
                .sanPhams(sanPhams)
                .tonKhos(tonKhos)
                .loHangs(loHangs)
                .giaoDichs(giaoDichs)
                .nguoiDungs(nguoiDungs)
                .build();
    }

    private void restoreFromSnapshot(DuLieuSaoLuuDTO snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Du lieu backup khong hop le");
        }

        // Xoa theo thu tu tranh rang buoc
        giaoDichRepository.deleteAllInBatch();
        loHangRepository.deleteAllInBatch();
        tonKhoRepository.deleteAllInBatch();
        sanPhamRepository.deleteAllInBatch();
        khoRepository.deleteAllInBatch();
        nhaCungCapRepository.deleteAllInBatch();
        khachHangRepository.deleteAllInBatch();
        danhMucRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        Map<String, DanhMuc> categoryMap = new HashMap<>();
        if (snapshot.getDanhMucs() != null) {
            snapshot.getDanhMucs().forEach(dto -> {
                DanhMuc saved = danhMucRepository.save(DanhMuc.builder()
                        .name(dto.getName())
                        .build());
                categoryMap.put(saved.getName(), saved);
            });
        }

        Map<String, NhaCungCap> supplierMap = new HashMap<>();
        if (snapshot.getNhaCungCaps() != null) {
            snapshot.getNhaCungCaps().forEach(dto -> {
                NhaCungCap saved = nhaCungCapRepository.save(NhaCungCap.builder()
                        .name(dto.getName())
                        .contactInfo(dto.getContactInfo())
                        .phone(dto.getPhone())
                        .email(dto.getEmail())
                        .address(dto.getAddress())
                        .build());
                supplierMap.put(saved.getEmail() != null ? saved.getEmail() : saved.getName(), saved);
            });
        }

        Map<String, KhachHang> customerMap = new HashMap<>();
        if (snapshot.getKhachHangs() != null) {
            snapshot.getKhachHangs().forEach(dto -> {
                KhachHang saved = khachHangRepository.save(KhachHang.builder()
                        .name(dto.getName())
                        .email(dto.getEmail())
                        .phone(dto.getPhone())
                        .address(dto.getAddress())
                        .notes(dto.getNotes())
                        .build());
                customerMap.put(saved.getEmail() != null ? saved.getEmail() : saved.getName(), saved);
            });
        }

        Map<String, Kho> khoMap = new HashMap<>();
        if (snapshot.getKhos() != null) {
            snapshot.getKhos().forEach(dto -> {
                Kho saved = khoRepository.save(Kho.builder()
                        .name(dto.getName())
                        .address(dto.getAddress())
                        .manager(dto.getManager())
                        .build());
                khoMap.put(saved.getName(), saved);
            });
        }

        Map<String, User> userMap = new HashMap<>();
        if (snapshot.getNguoiDungs() != null && !snapshot.getNguoiDungs().isEmpty()) {
            snapshot.getNguoiDungs().forEach(dto -> {
                User saved = userRepository.save(User.builder()
                        .name(dto.getName())
                        .email(dto.getEmail())
                        .password(dto.getPassword())
                        .phoneNumber(dto.getPhoneNumber())
                        .role(dto.getRole() != null ? dto.getRole() : VaiTroNguoiDung.MANAGER)
                        .build());
                if (saved.getEmail() != null) {
                    userMap.put(saved.getEmail(), saved);
                }
            });
        } else {
            User defaultAdmin = userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password("$2a$10$uOS5/na8tq2DTzmxgR75Ve6bqS2xXU2cT21k0Cw6B1y1vV0G9K3eK")
                    .phoneNumber("0999")
                    .role(VaiTroNguoiDung.ADMIN)
                    .build());
            userMap.put(defaultAdmin.getEmail(), defaultAdmin);
        }

        Map<String, SanPham> productMap = new HashMap<>();
        if (snapshot.getSanPhams() != null) {
            snapshot.getSanPhams().forEach(dto -> {
                DanhMuc category = dto.getCategoryName() != null ? categoryMap.get(dto.getCategoryName()) : null;
                SanPham saved = sanPhamRepository.save(SanPham.builder()
                        .name(dto.getName())
                        .sku(dto.getSku())
                        .price(dto.getPrice())
                        .stockQuantity(dto.getStockQuantity())
                        .minStock(dto.getMinStock())
                        .reservedQuantity(dto.getReservedQuantity())
                        .description(dto.getDescription())
                        .expiryDate(dto.getExpiryDate())
                        .imageUrl(dto.getImageUrl())
                        .category(category)
                        .build());
                productMap.put(saved.getSku(), saved);
            });
        }

        if (snapshot.getTonKhos() != null) {
            snapshot.getTonKhos().forEach(dto -> {
                SanPham sanPham = dto.getSanPhamSku() != null ? productMap.get(dto.getSanPhamSku()) : null;
                Kho kho = dto.getKhoName() != null ? khoMap.get(dto.getKhoName()) : null;
                if (sanPham != null && kho != null) {
                    tonKhoRepository.save(TonKho.builder()
                            .sanPham(sanPham)
                            .warehouse(kho)
                            .quantity(dto.getQuantity())
                            .build());
                }
            });
        }

        if (snapshot.getLoHangs() != null) {
            snapshot.getLoHangs().forEach(dto -> {
                SanPham sanPham = dto.getSanPhamSku() != null ? productMap.get(dto.getSanPhamSku()) : null;
                if (sanPham != null) {
                    loHangRepository.save(LoHang.builder()
                            .sanPham(sanPham)
                            .lotNumber(dto.getLotNumber())
                            .receivedDate(dto.getReceivedDate())
                            .expiryDate(dto.getExpiryDate())
                            .quantityRemaining(dto.getQuantityRemaining())
                            .build());
                }
            });
        }

        if (snapshot.getGiaoDichs() != null) {
            snapshot.getGiaoDichs().forEach(dto -> {
                SanPham sanPham = dto.getSanPhamSku() != null ? productMap.get(dto.getSanPhamSku()) : null;
                User user = dto.getUserEmail() != null ? userMap.get(dto.getUserEmail()) : null;
                if (user == null && dto.getUserEmail() != null) {
                    user = userRepository.save(User.builder()
                            .name(dto.getUserEmail())
                            .email(dto.getUserEmail())
                            .password("$2a$10$uOS5/na8tq2DTzmxgR75Ve6bqS2xXU2cT21k0Cw6B1y1vV0G9K3eK")
                            .role(VaiTroNguoiDung.MANAGER)
                            .build());
                    userMap.put(user.getEmail(), user);
                }

                GiaoDich giaoDich = GiaoDich.builder()
                        .sanPham(sanPham)
                        .user(user)
                        .nhaCungCap(dto.getNhaCungCapEmail() != null ? supplierMap.get(dto.getNhaCungCapEmail()) : null)
                        .customer(dto.getKhachHangEmail() != null ? customerMap.get(dto.getKhachHangEmail()) : null)
                        .transactionType(Optional.ofNullable(dto.getTransactionType()).orElse(LoaiGiaoDich.SALE))
                        .status(Optional.ofNullable(dto.getStatus()).orElse(TrangThaiGiaoDich.COMPLETED))
                        .totalProducts(dto.getTotalProducts())
                        .totalPrice(dto.getTotalPrice())
                        .description(dto.getDescription())
                        .note(dto.getNote())
                        .build();
                giaoDichRepository.save(giaoDich);
            });
        }
    }
}
