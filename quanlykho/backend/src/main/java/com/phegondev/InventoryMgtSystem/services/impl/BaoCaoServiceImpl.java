package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.ABCPhanTichDTO;
import com.phegondev.InventoryMgtSystem.dtos.ABCThongKeDTO;
import com.phegondev.InventoryMgtSystem.dtos.BaoCaoDoanhThuTongDTO;
import com.phegondev.InventoryMgtSystem.dtos.BaoCaoTonKhoTongDTO;
import com.phegondev.InventoryMgtSystem.dtos.BaoCaoXuatNhapTonDTO;
import com.phegondev.InventoryMgtSystem.dtos.BaoCaoXuatNhapTonDanhMucDTO;
import com.phegondev.InventoryMgtSystem.dtos.BaoCaoXuatNhapTonTongDTO;
import com.phegondev.InventoryMgtSystem.dtos.DoanhThuTheoKyDTO;
import com.phegondev.InventoryMgtSystem.dtos.GiaTriTonTheoDanhMucDTO;
import com.phegondev.InventoryMgtSystem.dtos.GiaTriTonTheoKhoDTO;
import com.phegondev.InventoryMgtSystem.dtos.HangTonLauDTO;
import com.phegondev.InventoryMgtSystem.dtos.HangTonLauThongKeDTO;
import com.phegondev.InventoryMgtSystem.dtos.NhaCungCapBaoCaoDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.SanPhamBanChayDTO;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.models.Kho;
import com.phegondev.InventoryMgtSystem.models.LoHang;
import com.phegondev.InventoryMgtSystem.models.NhaCungCap;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.models.TonKho;
import com.phegondev.InventoryMgtSystem.repositories.GiaoDichRepository;
import com.phegondev.InventoryMgtSystem.repositories.KhoRepository;
import com.phegondev.InventoryMgtSystem.repositories.LoHangRepository;
import com.phegondev.InventoryMgtSystem.repositories.NhaCungCapRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import com.phegondev.InventoryMgtSystem.repositories.TonKhoRepository;
import com.phegondev.InventoryMgtSystem.services.BaoCaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaoCaoServiceImpl implements BaoCaoService {

    private final SanPhamRepository sanPhamRepository;
    private final GiaoDichRepository giaoDichRepository;
    private final TonKhoRepository tonKhoRepository;
    private final KhoRepository khoRepository;
    private final LoHangRepository loHangRepository;
    private final NhaCungCapRepository nhaCungCapRepository;

    @Override
    public PhanHoi inventoryValuation() {
        List<SanPham> sanPhamList = sanPhamRepository.findAll();
        int totalQuantity = sanPhamList.stream()
                .mapToInt(this::getSafeStock)
                .sum();
        BigDecimal totalValue = sanPhamList.stream()
                .map(this::calculateProductValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BaoCaoTonKhoTongDTO summary = BaoCaoTonKhoTongDTO.builder()
                .tongGiaTri(totalValue)
                .tongSoLuong(totalQuantity)
                .soSanPham(sanPhamList.size())
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .baoCaoTonKhoTong(summary)
                .build();
    }

    @Override
    public PhanHoi inventoryValuationByCategory() {
        List<SanPham> sanPhamList = sanPhamRepository.findAll();
        Map<Long, GiaTriTonTheoDanhMucDTO> aggregated = new LinkedHashMap<>();

        for (SanPham sanPham : sanPhamList) {
            Long categoryId = sanPham.getCategory() != null ? sanPham.getCategory().getId() : null;
            String categoryName = sanPham.getCategory() != null ? sanPham.getCategory().getName() : "Khac";
            Long key = categoryId == null ? Long.MIN_VALUE : categoryId;
            GiaTriTonTheoDanhMucDTO dto = aggregated.computeIfAbsent(key, id ->
                    GiaTriTonTheoDanhMucDTO.builder()
                            .danhMucId(categoryId)
                            .tenDanhMuc(categoryName)
                            .tongSoLuong(0)
                            .giaTriTon(BigDecimal.ZERO)
                            .build()
            );
            int qty = getSafeStock(sanPham);
            dto.setTongSoLuong(dto.getTongSoLuong() + qty);
            dto.setGiaTriTon(dto.getGiaTriTon().add(calculateProductValue(sanPham)));
        }

        List<GiaTriTonTheoDanhMucDTO> result = aggregated.values().stream()
                .sorted(Comparator.comparing(GiaTriTonTheoDanhMucDTO::getGiaTriTon, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .giaTriTonTheoDanhMuc(result)
                .build();
    }

    @Override
    public PhanHoi inventoryValuationByWarehouse() {
        Map<Long, GiaTriTonTheoKhoDTO> aggregated = new LinkedHashMap<>();
        khoRepository.findAll().forEach(kho -> aggregated.put(kho.getId(),
                GiaTriTonTheoKhoDTO.builder()
                        .khoId(kho.getId())
                        .tenKho(kho.getName())
                        .tongSoLuong(0)
                        .giaTriTon(BigDecimal.ZERO)
                        .build()));

        List<TonKho> tonKhoList = tonKhoRepository.findAll();
        for (TonKho tonKho : tonKhoList) {
            Kho kho = tonKho.getWarehouse();
            SanPham sanPham = tonKho.getSanPham();
            if (kho == null || sanPham == null) {
                continue;
            }
            GiaTriTonTheoKhoDTO dto = aggregated.computeIfAbsent(kho.getId(), id ->
                    GiaTriTonTheoKhoDTO.builder()
                            .khoId(kho.getId())
                            .tenKho(kho.getName())
                            .tongSoLuong(0)
                            .giaTriTon(BigDecimal.ZERO)
                            .build());
            int qty = tonKho.getQuantity() == null ? 0 : tonKho.getQuantity();
            BigDecimal price = sanPham.getPrice() == null ? BigDecimal.ZERO : sanPham.getPrice();
            dto.setTongSoLuong(dto.getTongSoLuong() + qty);
            dto.setGiaTriTon(dto.getGiaTriTon().add(price.multiply(BigDecimal.valueOf(qty))));
        }

        List<GiaTriTonTheoKhoDTO> result = aggregated.values().stream()
                .sorted(Comparator.comparing(GiaTriTonTheoKhoDTO::getGiaTriTon, Comparator.nullsLast(BigDecimal::compareTo)).reversed())
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .giaTriTonTheoKho(result)
                .build();
    }

    @Override
    public PhanHoi stockMovement(LocalDate from, LocalDate to, Long categoryId) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime requestedEnd = to.atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = requestedEnd.isAfter(now) ? now : requestedEnd;
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("Khoang thoi gian khong hop le");
        }

        List<SanPham> sanPhams = categoryId != null
                ? sanPhamRepository.findByCategory_Id(categoryId)
                : sanPhamRepository.findAll();

        if (sanPhams.isEmpty()) {
            return PhanHoi.builder()
                    .status(200)
                    .message("Thanh cong")
                    .baoCaoXuatNhapTon(List.of())
                    .baoCaoXuatNhapTonTheoDanhMuc(List.of())
                    .baoCaoXuatNhapTonTong(BaoCaoXuatNhapTonTongDTO.builder()
                            .tonDauKy(0)
                            .tongNhap(0)
                            .tongXuat(0)
                            .tonCuoiKy(0)
                            .build())
                    .build();
        }

        Set<Long> productIds = sanPhams.stream()
                .map(SanPham::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<GiaoDich> trongKy = giaoDichRepository.findByCreatedAtBetween(start, end);
        List<GiaoDich> sauKy = List.of();
        if (end.isBefore(now)) {
            LocalDateTime afterStart = end.plusSeconds(1);
            sauKy = giaoDichRepository.findByCreatedAtBetween(afterStart, now);
        }

        Map<Long, MovementTotals> trongKyMap = aggregateMovements(trongKy, productIds);
        Map<Long, MovementTotals> sauKyMap = aggregateMovements(sauKy, productIds);

        List<BaoCaoXuatNhapTonDTO> baoCaoTheoSanPham = new ArrayList<>();
        Map<Long, BaoCaoXuatNhapTonDanhMucDTO> baoCaoTheoDanhMuc = new LinkedHashMap<>();

        int tongTonDau = 0;
        int tongNhap = 0;
        int tongXuat = 0;
        int tongTonCuoi = 0;

        for (SanPham sanPham : sanPhams) {
            Long sanPhamId = sanPham.getId();
            if (sanPhamId == null) {
                continue;
            }
            MovementTotals trongKyTotals = trongKyMap.getOrDefault(sanPhamId, new MovementTotals());
            MovementTotals sauKyTotals = sauKyMap.getOrDefault(sanPhamId, new MovementTotals());

            int currentStock = getSafeStock(sanPham);
            int tonCuoiKy = clampNonNegative(currentStock - sauKyTotals.net());
            int tonDauKy = clampNonNegative(tonCuoiKy - trongKyTotals.net());

            tongTonDau += tonDauKy;
            tongNhap += trongKyTotals.getInbound();
            tongXuat += trongKyTotals.getOutbound();
            tongTonCuoi += tonCuoiKy;

            Long danhMucId = sanPham.getCategory() != null ? sanPham.getCategory().getId() : null;
            String tenDanhMuc = sanPham.getCategory() != null ? sanPham.getCategory().getName() : "Khac";

            baoCaoTheoSanPham.add(BaoCaoXuatNhapTonDTO.builder()
                    .sanPhamId(sanPhamId)
                    .tenSanPham(sanPham.getName())
                    .sku(sanPham.getSku())
                    .danhMucId(danhMucId)
                    .tenDanhMuc(tenDanhMuc)
                    .tonDauKy(tonDauKy)
                    .tongNhap(trongKyTotals.getInbound())
                    .tongXuat(trongKyTotals.getOutbound())
                    .tonCuoiKy(tonCuoiKy)
                    .build());

            Long danhMucKey = danhMucId == null ? Long.MIN_VALUE : danhMucId;
            BaoCaoXuatNhapTonDanhMucDTO danhMucDTO = baoCaoTheoDanhMuc.computeIfAbsent(danhMucKey, id ->
                    BaoCaoXuatNhapTonDanhMucDTO.builder()
                            .danhMucId(danhMucId)
                            .tenDanhMuc(tenDanhMuc)
                            .tonDauKy(0)
                            .tongNhap(0)
                            .tongXuat(0)
                            .tonCuoiKy(0)
                            .build());

            danhMucDTO.setTonDauKy(danhMucDTO.getTonDauKy() + tonDauKy);
            danhMucDTO.setTongNhap(danhMucDTO.getTongNhap() + trongKyTotals.getInbound());
            danhMucDTO.setTongXuat(danhMucDTO.getTongXuat() + trongKyTotals.getOutbound());
            danhMucDTO.setTonCuoiKy(danhMucDTO.getTonCuoiKy() + tonCuoiKy);
        }

        BaoCaoXuatNhapTonTongDTO tongDTO = BaoCaoXuatNhapTonTongDTO.builder()
                .tonDauKy(tongTonDau)
                .tongNhap(tongNhap)
                .tongXuat(tongXuat)
                .tonCuoiKy(tongTonCuoi)
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .baoCaoXuatNhapTon(baoCaoTheoSanPham)
                .baoCaoXuatNhapTonTheoDanhMuc(new ArrayList<>(baoCaoTheoDanhMuc.values()))
                .baoCaoXuatNhapTonTong(tongDTO)
                .build();
    }

    private int getSafeStock(SanPham sanPham) {
        return sanPham.getStockQuantity() == null ? 0 : sanPham.getStockQuantity();
    }

    private BigDecimal calculateProductValue(SanPham sanPham) {
        BigDecimal price = sanPham.getPrice() == null ? BigDecimal.ZERO : sanPham.getPrice();
        return price.multiply(BigDecimal.valueOf(getSafeStock(sanPham)));
    }

    @Override
    public PhanHoi agingInventory(int minDays) {
        int effectiveMinDays = Math.max(minDays, 0);
        LocalDate today = LocalDate.now();
        List<LoHang> loHangs = loHangRepository.findByQuantityRemainingGreaterThan(0);
        List<HangTonLauDTO> dtoList = new ArrayList<>();

        int totalTracked = 0;
        int over30 = 0;
        int over60 = 0;
        int over90 = 0;
        int over180 = 0;

        for (LoHang loHang : loHangs) {
            LocalDate receivedDate = loHang.getReceivedDate();
            if (receivedDate == null) {
                continue;
            }
            int days = (int) ChronoUnit.DAYS.between(receivedDate, today);
            if (days < 0) {
                continue;
            }
            totalTracked++;
            if (days >= 30) over30++;
            if (days >= 60) over60++;
            if (days >= 90) over90++;
            if (days >= 180) over180++;
            if (days < effectiveMinDays) {
                continue;
            }

            SanPham sanPham = loHang.getSanPham();
            Integer remaining = loHang.getQuantityRemaining();
            dtoList.add(HangTonLauDTO.builder()
                    .loHangId(loHang.getId())
                    .sanPhamId(sanPham != null ? sanPham.getId() : null)
                    .tenSanPham(sanPham != null ? sanPham.getName() : null)
                    .sku(sanPham != null ? sanPham.getSku() : null)
                    .danhMucId(sanPham != null && sanPham.getCategory() != null ? sanPham.getCategory().getId() : null)
                    .tenDanhMuc(sanPham != null && sanPham.getCategory() != null ? sanPham.getCategory().getName() : "Khac")
                    .soLo(loHang.getLotNumber())
                    .ngayNhap(receivedDate)
                    .soNgayTon(days)
                    .soLuongConLai(remaining == null ? 0 : remaining)
                    .nhomTuoi(determineAgingBucket(days))
                    .build());
        }

        dtoList.sort(Comparator.comparing(HangTonLauDTO::getSoNgayTon, Comparator.nullsLast(Integer::compareTo)).reversed());

        HangTonLauThongKeDTO thongKe = HangTonLauThongKeDTO.builder()
                .tongLo(totalTracked)
                .tren30Ngay(over30)
                .tren60Ngay(over60)
                .tren90Ngay(over90)
                .tren180Ngay(over180)
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .hangTonLaus(dtoList)
                .thongKeHangTonLau(thongKe)
                .build();
    }

    @Override
    public PhanHoi abcAnalysis(LocalDate from, LocalDate to) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = to != null ? to : today;
        LocalDate fromDate = from != null ? from : toDate.minusDays(90);
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        List<GiaoDich> transactions = giaoDichRepository.findByCreatedAtBetween(start, end);
        Map<Long, ABCAggregate> aggregates = new LinkedHashMap<>();

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getSanPham() == null || giaoDich.getTransactionType() == null) {
                continue;
            }
            BigDecimal totalPrice = giaoDich.getTotalPrice();
            if (totalPrice == null || totalPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            LoaiGiaoDich type = giaoDich.getTransactionType();
            BigDecimal signedPrice;
            if (type == LoaiGiaoDich.SALE) {
                signedPrice = totalPrice;
            } else if (type == LoaiGiaoDich.RETURN_FROM_CUSTOMER) {
                signedPrice = totalPrice.negate();
            } else {
                continue;
            }

            if (signedPrice.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            SanPham sanPham = giaoDich.getSanPham();
            Long productId = sanPham.getId();
            if (productId == null) {
                continue;
            }
            ABCAggregate agg = aggregates.computeIfAbsent(productId, id -> new ABCAggregate(sanPham));
            agg.addRevenue(signedPrice);
        }

        List<ABCAggregate> validAggregates = aggregates.values().stream()
                .filter(agg -> agg.getRevenue().compareTo(BigDecimal.ZERO) > 0)
                .sorted(Comparator.comparing(ABCAggregate::getRevenue).reversed())
                .toList();

        if (validAggregates.isEmpty()) {
            return PhanHoi.builder()
                    .status(200)
                    .message("Thanh cong")
                    .baoCaoABC(List.of())
                    .thongKeABC(ABCThongKeDTO.builder().tongSanPham(0).build())
                    .build();
        }

        BigDecimal totalRevenue = validAggregates.stream()
                .map(ABCAggregate::getRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return PhanHoi.builder()
                    .status(200)
                    .message("Thanh cong")
                    .baoCaoABC(List.of())
                    .thongKeABC(ABCThongKeDTO.builder().tongSanPham(0).build())
                    .build();
        }

        BigDecimal cumulative = BigDecimal.ZERO;
        List<ABCPhanTichDTO> dtoList = new ArrayList<>();
        int countA = 0;
        int countB = 0;
        int countC = 0;
        BigDecimal revenueA = BigDecimal.ZERO;
        BigDecimal revenueB = BigDecimal.ZERO;
        BigDecimal revenueC = BigDecimal.ZERO;

        for (ABCAggregate agg : validAggregates) {
            BigDecimal revenue = agg.getRevenue();
            cumulative = cumulative.add(revenue);
            double share = revenue.divide(totalRevenue, 6, RoundingMode.HALF_UP).doubleValue();
            double cumulativeShare = cumulative.divide(totalRevenue, 6, RoundingMode.HALF_UP).doubleValue();

            String category;
            if (cumulativeShare <= 0.8) {
                category = "A";
                countA++;
                revenueA = revenueA.add(revenue);
            } else if (cumulativeShare <= 0.95) {
                category = "B";
                countB++;
                revenueB = revenueB.add(revenue);
            } else {
                category = "C";
                countC++;
                revenueC = revenueC.add(revenue);
            }

            SanPham sanPham = agg.getSanPham();
            dtoList.add(ABCPhanTichDTO.builder()
                    .sanPhamId(sanPham.getId())
                    .tenSanPham(sanPham.getName())
                    .sku(sanPham.getSku())
                    .danhMucId(sanPham.getCategory() != null ? sanPham.getCategory().getId() : null)
                    .tenDanhMuc(sanPham.getCategory() != null ? sanPham.getCategory().getName() : "Khac")
                    .doanhThu(revenue)
                    .tyLe(share * 100)
                    .tichLuy(cumulativeShare * 100)
                    .nhom(category)
                    .build());
        }

        int totalProducts = validAggregates.size();
        double pctA = revenueA.divide(totalRevenue, 6, RoundingMode.HALF_UP).doubleValue() * 100;
        double pctB = revenueB.divide(totalRevenue, 6, RoundingMode.HALF_UP).doubleValue() * 100;
        double pctC = revenueC.divide(totalRevenue, 6, RoundingMode.HALF_UP).doubleValue() * 100;

        ABCThongKeDTO summary = ABCThongKeDTO.builder()
                .tongSanPham(totalProducts)
                .nhomA(countA)
                .nhomB(countB)
                .nhomC(countC)
                .tyLeDoanhThuA(pctA)
                .tyLeDoanhThuB(pctB)
                .tyLeDoanhThuC(pctC)
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .baoCaoABC(dtoList)
                .thongKeABC(summary)
                .build();
    }

    @Override
    public PhanHoi bestSellers(LocalDate from, LocalDate to, int limit, String metric) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = to != null ? to : today;
        LocalDate fromDate = from != null ? from : toDate.minusDays(30);
        int normalizedLimit = Math.max(limit, 1);
        String normalizedMetric = (metric == null ? "quantity" : metric.trim().toLowerCase());
        if (!normalizedMetric.equals("revenue")) {
            normalizedMetric = "quantity";
        }
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        List<GiaoDich> transactions = giaoDichRepository.findByCreatedAtBetween(start, end);
        Map<Long, BestSellerAggregate> aggregates = new LinkedHashMap<>();

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getSanPham() == null || giaoDich.getTransactionType() == null) {
                continue;
            }
            LoaiGiaoDich type = giaoDich.getTransactionType();
            int quantity = giaoDich.getTotalProducts() == null ? 0 : giaoDich.getTotalProducts();
            BigDecimal price = giaoDich.getTotalPrice() == null ? BigDecimal.ZERO : giaoDich.getTotalPrice();
            if (quantity == 0 && price.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            int qtyDelta = 0;
            BigDecimal revenueDelta = BigDecimal.ZERO;
            switch (type) {
                case SALE -> {
                    qtyDelta = quantity;
                    revenueDelta = price;
                }
                case RETURN_FROM_CUSTOMER -> {
                    qtyDelta = -quantity;
                    revenueDelta = price.negate();
                }
                default -> {
                    continue;
                }
            }

            if (qtyDelta == 0 && revenueDelta.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            SanPham sanPham = giaoDich.getSanPham();
            Long productId = sanPham.getId();
            if (productId == null) {
                continue;
            }
            BestSellerAggregate aggregate = aggregates.computeIfAbsent(productId, id -> new BestSellerAggregate(sanPham));
            aggregate.addQuantity(qtyDelta);
            aggregate.addRevenue(revenueDelta);
        }

        final String metricKey = normalizedMetric;
        List<BestSellerAggregate> filtered = aggregates.values().stream()
                .filter(agg -> {
                    if (metricKey.equals("revenue")) {
                        return agg.getRevenue().compareTo(BigDecimal.ZERO) > 0;
                    }
                    return agg.getQuantity() > 0;
                })
                .toList();

        if (filtered.isEmpty()) {
            return PhanHoi.builder()
                    .status(200)
                    .message("Thanh cong")
                    .sanPhamBanChay(List.of())
                    .build();
        }

        List<BestSellerAggregate> sorted = filtered.stream()
                .sorted((a, b) -> {
                    if (metricKey.equals("revenue")) {
                        return b.getRevenue().compareTo(a.getRevenue());
                    }
                    return Long.compare(b.getQuantity(), a.getQuantity());
                })
                .limit(normalizedLimit)
                .toList();

        double totalContribution;
        if (normalizedMetric.equals("revenue")) {
            BigDecimal totalRevenue = sorted.stream()
                    .map(BestSellerAggregate::getRevenue)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            totalContribution = totalRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? totalRevenue.doubleValue()
                    : 1.0;
        } else {
            long totalQty = sorted.stream()
                    .mapToLong(BestSellerAggregate::getQuantity)
                    .sum();
            totalContribution = totalQty > 0 ? (double) totalQty : 1.0;
        }

        final double baseContribution = totalContribution;
        List<SanPhamBanChayDTO> dtoList = sorted.stream()
                .map(agg -> {
                    SanPham sanPham = agg.getSanPham();
                    long qty = agg.getQuantity();
                    BigDecimal rev = agg.getRevenue();
                    double contribution = metricKey.equals("revenue")
                            ? rev.doubleValue() / baseContribution * 100
                            : qty / baseContribution * 100;
                    return SanPhamBanChayDTO.builder()
                            .sanPhamId(sanPham.getId())
                            .tenSanPham(sanPham.getName())
                            .sku(sanPham.getSku())
                            .danhMucId(sanPham.getCategory() != null ? sanPham.getCategory().getId() : null)
                            .tenDanhMuc(sanPham.getCategory() != null ? sanPham.getCategory().getName() : "Khac")
                            .tongSoLuong(qty)
                            .tongDoanhThu(rev)
                            .tyLeDongGop(contribution)
                            .build();
                })
                .toList();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .sanPhamBanChay(dtoList)
                .build();
    }

    @Override
    public PhanHoi revenueProfit(LocalDate from, LocalDate to, String interval) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = to != null ? to : today;
        LocalDate fromDate = from != null ? from : toDate.minusDays(30);
        String normalizedInterval = interval == null ? "DAY" : interval.trim().toUpperCase();
        boolean byMonth = normalizedInterval.startsWith("MONTH");
        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);

        List<GiaoDich> transactions = giaoDichRepository.findByCreatedAtBetween(start, end);

        Map<String, DoanhThuBucket> bucketMap = new LinkedHashMap<>();
        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        int saleCount = 0;
        int purchaseCount = 0;

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getTransactionType() == null) {
                continue;
            }
            BigDecimal amount = giaoDich.getTotalPrice() == null ? BigDecimal.ZERO : giaoDich.getTotalPrice();
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }
            LocalDate date = giaoDich.getCreatedAt() != null ? giaoDich.getCreatedAt().toLocalDate() : null;
            if (date == null) {
                continue;
            }
            LocalDate bucketDate = byMonth ? date.withDayOfMonth(1) : date;
            String key = byMonth ? bucketDate.toString().substring(0, 7) : bucketDate.toString();
            DoanhThuBucket bucket = bucketMap.computeIfAbsent(key, k -> new DoanhThuBucket(bucketDate));

            switch (giaoDich.getTransactionType()) {
                case SALE -> {
                    totalRevenue = totalRevenue.add(amount);
                    saleCount++;
                    bucket.addRevenue(amount);
                }
                case RETURN_FROM_CUSTOMER -> {
                    totalRevenue = totalRevenue.subtract(amount);
                    bucket.addRevenue(amount.negate());
                }
                case PURCHASE -> {
                    totalCost = totalCost.add(amount);
                    purchaseCount++;
                    bucket.addCost(amount);
                }
                case RETURN_TO_SUPPLIER -> {
                    totalCost = totalCost.subtract(amount);
                    bucket.addCost(amount.negate());
                }
                default -> {
                }
            }
        }

        BigDecimal profit = totalRevenue.subtract(totalCost);

        List<DoanhThuTheoKyDTO> series = bucketMap.values().stream()
                .sorted(Comparator.comparing(DoanhThuBucket::date))
                .map(bucket -> DoanhThuTheoKyDTO.builder()
                        .nhan(byMonth
                                ? bucket.date().getYear() + "-" + String.format("%02d", bucket.date().getMonthValue())
                                : bucket.date().toString())
                        .doanhThu(bucket.getRevenue())
                        .chiPhi(bucket.getCost())
                        .loiNhuan(bucket.getRevenue().subtract(bucket.getCost()))
                        .build())
                .toList();

        BaoCaoDoanhThuTongDTO summary = BaoCaoDoanhThuTongDTO.builder()
                .tongDoanhThu(totalRevenue)
                .tongChiPhi(totalCost)
                .loiNhuan(profit)
                .soDonBan(saleCount)
                .soDonNhap(purchaseCount)
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .baoCaoDoanhThuTong(summary)
                .doanhThuTheoKys(series)
                .build();
    }

    @Override
    public PhanHoi supplierReport(LocalDate from, LocalDate to, int limit) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = to != null ? to : today;
        LocalDate fromDate = from != null ? from : toDate.minusDays(90);
        int normalizedLimit = Math.max(limit, 1);

        LocalDateTime start = fromDate.atStartOfDay();
        LocalDateTime end = toDate.atTime(23, 59, 59);
        List<GiaoDich> purchases = giaoDichRepository.findByCreatedAtBetween(start, end).stream()
                .filter(tx -> tx.getTransactionType() != null)
                .filter(tx -> tx.getTransactionType() == LoaiGiaoDich.PURCHASE
                        || tx.getTransactionType() == LoaiGiaoDich.RETURN_TO_SUPPLIER)
                .collect(Collectors.toList());

        Map<Long, SupplierAggregate> aggregates = new LinkedHashMap<>();

        for (GiaoDich giaoDich : purchases) {
            if (giaoDich.getNhaCungCap() == null) {
                continue;
            }
            Long supplierId = giaoDich.getNhaCungCap().getId();
            if (supplierId == null) {
                continue;
            }
            BigDecimal amount = giaoDich.getTotalPrice() == null ? BigDecimal.ZERO : giaoDich.getTotalPrice();
            if (amount.compareTo(BigDecimal.ZERO) == 0) {
                continue;
            }

            SupplierAggregate agg = aggregates.computeIfAbsent(supplierId, id -> new SupplierAggregate(giaoDich.getNhaCungCap()));
            if (giaoDich.getTransactionType() == LoaiGiaoDich.PURCHASE) {
                agg.addTransaction(amount);
            } else if (giaoDich.getTransactionType() == LoaiGiaoDich.RETURN_TO_SUPPLIER) {
                agg.addTransaction(amount.negate());
            }
        }

        List<SupplierAggregate> sorted = aggregates.values().stream()
                .filter(agg -> agg.getTotalValue().compareTo(BigDecimal.ZERO) > 0)
                .sorted((a, b) -> b.getTotalValue().compareTo(a.getTotalValue()))
                .limit(normalizedLimit)
                .toList();

        BigDecimal totalValue = sorted.stream()
                .map(SupplierAggregate::getTotalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        double totalValueDouble = totalValue.compareTo(BigDecimal.ZERO) > 0 ? totalValue.doubleValue() : 1.0;

        List<NhaCungCapBaoCaoDTO> dtoList = new ArrayList<>();
        int rank = 1;
        for (SupplierAggregate aggregate : sorted) {
            NhaCungCap supplier = aggregate.getSupplier();
            dtoList.add(NhaCungCapBaoCaoDTO.builder()
                    .nhaCungCapId(supplier.getId())
                    .tenNhaCungCap(supplier.getName())
                    .soDienThoai(supplier.getPhone())
                    .email(supplier.getEmail())
                    .soGiaoDich(aggregate.getTransactions())
                    .tongGiaTri(aggregate.getTotalValue())
                    .tyLeDongGop((aggregate.getTotalValue().doubleValue() / totalValueDouble) * 100)
                    .xepHang(rank++)
                    .build());
        }

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .nhaCungCapBaoCao(dtoList)
                .build();
    }

    private Map<Long, MovementTotals> aggregateMovements(List<GiaoDich> giaoDiches, Set<Long> allowedProductIds) {
        Map<Long, MovementTotals> result = new HashMap<>();
        for (GiaoDich giaoDich : giaoDiches) {
            if (giaoDich.getSanPham() == null || giaoDich.getTransactionType() == null) {
                continue;
            }
            Long productId = giaoDich.getSanPham().getId();
            if (productId == null) {
                continue;
            }
            if (allowedProductIds != null && !allowedProductIds.contains(productId)) {
                continue;
            }
            int quantity = giaoDich.getTotalProducts() == null ? 0 : giaoDich.getTotalProducts();
            if (quantity == 0) {
                continue;
            }
            MovementTotals totals = result.computeIfAbsent(productId, id -> new MovementTotals());
            switch (giaoDich.getTransactionType()) {
                case PURCHASE, RETURN_FROM_CUSTOMER -> totals.addInbound(quantity);
                case SALE, RETURN_TO_SUPPLIER -> totals.addOutbound(quantity);
                default -> {
                }
            }
        }
        return result;
    }

    private int clampNonNegative(int value) {
        return Math.max(value, 0);
    }

    private static class MovementTotals {
        private int inbound;
        private int outbound;

        void addInbound(int qty) {
            inbound += qty;
        }

        void addOutbound(int qty) {
            outbound += qty;
        }

        int getInbound() {
            return inbound;
        }

        int getOutbound() {
            return outbound;
        }

        int net() {
            return inbound - outbound;
        }
    }

    private String determineAgingBucket(int days) {
        if (days >= 180) {
            return ">=180 ngay";
        }
        if (days >= 90) {
            return "90-179 ngay";
        }
        if (days >= 60) {
            return "60-89 ngay";
        }
        if (days >= 30) {
            return "30-59 ngay";
        }
        return "<30 ngay";
    }

    private static class ABCAggregate {
        private final SanPham sanPham;
        private BigDecimal revenue = BigDecimal.ZERO;

        ABCAggregate(SanPham sanPham) {
            this.sanPham = sanPham;
        }

        void addRevenue(BigDecimal amount) {
            revenue = revenue.add(amount);
        }

        BigDecimal getRevenue() {
            return revenue;
        }

        SanPham getSanPham() {
            return sanPham;
        }
    }

    private static class BestSellerAggregate {
        private final SanPham sanPham;
        private long quantity;
        private BigDecimal revenue = BigDecimal.ZERO;

        BestSellerAggregate(SanPham sanPham) {
            this.sanPham = sanPham;
        }

        void addQuantity(long qty) {
            quantity += qty;
        }

        void addRevenue(BigDecimal amount) {
            revenue = revenue.add(amount);
        }

        long getQuantity() {
            return quantity;
        }

        BigDecimal getRevenue() {
            return revenue;
        }

        SanPham getSanPham() {
            return sanPham;
        }
    }

    private static class DoanhThuBucket {
        private final LocalDate date;
        private BigDecimal revenue = BigDecimal.ZERO;
        private BigDecimal cost = BigDecimal.ZERO;

        DoanhThuBucket(LocalDate date) {
            this.date = date;
        }

        void addRevenue(BigDecimal amount) {
            revenue = revenue.add(amount);
        }

        void addCost(BigDecimal amount) {
            cost = cost.add(amount);
        }

        LocalDate date() {
            return date;
        }

        BigDecimal getRevenue() {
            return revenue;
        }

        BigDecimal getCost() {
            return cost;
        }
    }

    private static class SupplierAggregate {
        private final NhaCungCap supplier;
        private long transactions;
        private BigDecimal totalValue = BigDecimal.ZERO;

        SupplierAggregate(NhaCungCap supplier) {
            this.supplier = supplier;
        }

        void addTransaction(BigDecimal amount) {
            if (amount == null) {
                return;
            }
            totalValue = totalValue.add(amount);
            transactions++;
        }

        NhaCungCap getSupplier() {
            return supplier;
        }

        long getTransactions() {
            return transactions;
        }

        BigDecimal getTotalValue() {
            return totalValue;
        }
    }
}
