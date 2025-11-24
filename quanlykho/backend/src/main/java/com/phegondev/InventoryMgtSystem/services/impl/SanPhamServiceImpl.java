package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.SanPhamDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.DanhMuc;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.DanhMucRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import com.phegondev.InventoryMgtSystem.services.NhatKyThayDoiService;
import com.phegondev.InventoryMgtSystem.services.SanPhamService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SanPhamServiceImpl implements SanPhamService {

    private final SanPhamRepository sanPhamRepository;
    private final ModelMapper modelMapper;
    private final DanhMucRepository danhMucRepository;
    private final NhatKyThayDoiService nhatKyThayDoiService;

    @Value("${app.frontend.public.dir:../frontend/public}")
    private String frontendPublicDir;

    private static final String IMAGE_DIR_FALLBACK = System.getProperty("user.dir") + "/product-images/";

    @Override
    public PhanHoi luuSanPham(SanPhamDTO sanPhamDTO, MultipartFile imageFile) {
        DanhMuc category = danhMucRepository.findById(sanPhamDTO.getCategoryId())
                .orElseThrow(() -> new NotFoundException("Không tìm thấy danh mục"));

        Integer minStock = sanPhamDTO.getMinStock() != null && sanPhamDTO.getMinStock() >= 0
                ? sanPhamDTO.getMinStock()
                : 0;

        SanPham sanPham = SanPham.builder()
                .name(sanPhamDTO.getName())
                .sku(sanPhamDTO.getSku())
                .price(sanPhamDTO.getPrice())
                .stockQuantity(sanPhamDTO.getStockQuantity())
                .minStock(minStock)
                .description(sanPhamDTO.getDescription())
                .category(category)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            sanPham.setImageUrl(saveImageToFrontend(imageFile));
        }

        sanPhamRepository.save(sanPham);
        ghiNhatKy("TAO", "Tao san pham " + sanPham.getName(), null,
                modelMapper.map(sanPham, SanPhamDTO.class), sanPham.getId());

        return PhanHoi.builder()
                .status(200)
                .message("Lưu sản phẩm thành công")
                .build();
    }

    @Override
    public PhanHoi capNhatSanPham(SanPhamDTO sanPhamDTO, MultipartFile imageFile) {
        SanPham existing = sanPhamRepository.findById(sanPhamDTO.getId())
                .orElseThrow(() -> new NotFoundException("Kh�ng t�m th?y s?n ph?m"));
        SanPhamDTO duLieuCu = modelMapper.map(existing, SanPhamDTO.class);

        if (imageFile != null && !imageFile.isEmpty()) {
            existing.setImageUrl(saveImageToFrontend(imageFile));
        }

        if (sanPhamDTO.getCategoryId() != null && sanPhamDTO.getCategoryId() > 0) {
            DanhMuc category = danhMucRepository.findById(sanPhamDTO.getCategoryId())
                    .orElseThrow(() -> new NotFoundException("Kh�ng t�m th?y danh m?c"));
            existing.setCategory(category);
        }
        if (sanPhamDTO.getName() != null && !sanPhamDTO.getName().isBlank()) {
            existing.setName(sanPhamDTO.getName());
        }
        if (sanPhamDTO.getSku() != null && !sanPhamDTO.getSku().isBlank()) {
            existing.setSku(sanPhamDTO.getSku());
        }
        if (sanPhamDTO.getDescription() != null && !sanPhamDTO.getDescription().isBlank()) {
            existing.setDescription(sanPhamDTO.getDescription());
        }
        if (sanPhamDTO.getPrice() != null && sanPhamDTO.getPrice().compareTo(BigDecimal.ZERO) >= 0) {
            existing.setPrice(sanPhamDTO.getPrice());
        }
        if (sanPhamDTO.getStockQuantity() != null && sanPhamDTO.getStockQuantity() >= 0) {
            existing.setStockQuantity(sanPhamDTO.getStockQuantity());
        }
        if (sanPhamDTO.getMinStock() != null && sanPhamDTO.getMinStock() >= 0) {
            existing.setMinStock(sanPhamDTO.getMinStock());
        }

        sanPhamRepository.save(existing);
        ghiNhatKy("CAP_NHAT", "Cap nhat san pham " + existing.getName(), duLieuCu,
                modelMapper.map(existing, SanPhamDTO.class), existing.getId());

        return PhanHoi.builder()
                .status(200)
                .message("C?p nh?t s?n ph?m th�nh c�ng")
                .build();
    }

    @Override
    public PhanHoi layTatCaSanPham() {
        List<SanPham> sanPhams = sanPhamRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhams, new TypeToken<List<SanPhamDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .sanPhams(dtoList)
                .build();
    }

    @Override
    public PhanHoi layTrangSanPham(int page, int size, String keyword, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        String normalizedKeyword = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        Long normalizedCategoryId = (categoryId == null || categoryId <= 0) ? null : categoryId;
        Page<SanPham> sanPhamPage = sanPhamRepository.search(normalizedKeyword, normalizedCategoryId, pageable);
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhamPage.getContent(), new TypeToken<List<SanPhamDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .sanPhams(dtoList)
                .totalElements(sanPhamPage.getTotalElements())
                .totalPages(sanPhamPage.getTotalPages())
                .build();
    }

    @Override
    public PhanHoi laySanPhamTheoId(Long id) {
        SanPham sanPham = sanPhamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .sanPham(modelMapper.map(sanPham, SanPhamDTO.class))
                .build();
    }

    @Override
    public PhanHoi xoaSanPham(Long id) {
        sanPhamRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy sản phẩm"));
        sanPhamRepository.deleteById(id);
        return PhanHoi.builder()
                .status(200)
                .message("Xóa sản phẩm thành công")
                .build();
    }

    @Override
    public PhanHoi timKiemSanPham(String input) {
        List<SanPham> sanPhams = sanPhamRepository.findByNameContainingOrDescriptionContaining(input, input);
        if (sanPhams.isEmpty()) {
            throw new NotFoundException("Không tìm thấy sản phẩm");
        }
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhams, new TypeToken<List<SanPhamDTO>>() {
        }.getType());

        return PhanHoi.builder()
                .status(200)
                .message("thành công")
                .sanPhams(dtoList)
                .build();
    }

    @Override
    public PhanHoi laySanPhamSapHetHang() {
        List<SanPham> sanPhams = sanPhamRepository.findLowStockProducts();
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhams, new TypeToken<List<SanPhamDTO>>() {
        }.getType());
        return PhanHoi.builder().status(200).message("thành công").sanPhams(dtoList).build();
    }

    @Override
    public PhanHoi laySanPhamSapHetHan(int days) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime to = now.plusDays(days);
        List<SanPham> sanPhams = sanPhamRepository.findExpiringBetween(now, to);
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhams, new TypeToken<List<SanPhamDTO>>() {
        }.getType());
        return PhanHoi.builder().status(200).message("thành công").sanPhams(dtoList).build();
    }

    @Override
    public PhanHoi laySanPhamHetHan() {
        List<SanPham> sanPhams = sanPhamRepository.findExpired(LocalDateTime.now());
        List<SanPhamDTO> dtoList = modelMapper.map(sanPhams, new TypeToken<List<SanPhamDTO>>() {
        }.getType());
        return PhanHoi.builder().status(200).message("thành công").sanPhams(dtoList).build();
    }

    @Override
    public PhanHoi laySanPhamTheoSku(String sku) {
        if (sku == null || sku.isBlank()) {
            throw new IllegalArgumentException("SKU la bat buoc");
        }
        SanPham sanPham = sanPhamRepository.findBySkuIgnoreCase(sku.trim())
                .orElseThrow(() -> new NotFoundException("Khong tim thay san pham theo SKU"));
        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .sanPham(modelMapper.map(sanPham, SanPhamDTO.class))
                .build();
    }

    private String saveImageToFrontend(MultipartFile imageFile) {
        if (!imageFile.getContentType().startsWith("image/") || imageFile.getSize() > 1024 * 1024 * 1024) {
            throw new IllegalArgumentException("Chỉ cho phép tệp ảnh dung lượng dưới 1GB");
        }
        try {
            Path productsDir = Paths.get(System.getProperty("user.dir")).resolve(frontendPublicDir).resolve("products");
            if (!Files.exists(productsDir)) {
                Files.createDirectories(productsDir);
                log.info("Created directory: {}", productsDir);
            }
            String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path destination = productsDir.resolve(uniqueFileName);
            imageFile.transferTo(destination.toFile());
            return "products/" + uniqueFileName;
        } catch (Exception e) {
            log.warn("Lưu ảnh vào frontend thất bại, dùng fallback. Lỗi: {}", e.getMessage());
            return saveImageFallback(imageFile);
        }
    }

    private String saveImageFallback(MultipartFile imageFile) {
        try {
            File directory = new File(IMAGE_DIR_FALLBACK);
            if (!directory.exists()) {
                directory.mkdir();
            }
            String uniqueFileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
            Path destination = Paths.get(IMAGE_DIR_FALLBACK + uniqueFileName);
            imageFile.transferTo(destination.toFile());
            return destination.getFileName().toString();
        } catch (Exception e) {
            throw new IllegalArgumentException("Lỗi lưu ảnh: " + e.getMessage());
        }
    }
    private void ghiNhatKy(String action, String message, Object oldData, Object newData, Long targetId) {
        nhatKyThayDoiService.ghi("SanPham", action, message, "SanPham", targetId, oldData, newData);
    }
}







