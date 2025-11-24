package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.DashboardDoanhThuDanhMucDTO;
import com.phegondev.InventoryMgtSystem.dtos.DashboardNhapXuatThangDTO;
import com.phegondev.InventoryMgtSystem.dtos.DashboardTongQuanDTO;
import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.dtos.SanPhamDTO;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.GiaoDichRepository;
import com.phegondev.InventoryMgtSystem.repositories.NhaCungCapRepository;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import com.phegondev.InventoryMgtSystem.services.DashboardService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardServiceImpl implements DashboardService {

    private final SanPhamRepository sanPhamRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final GiaoDichRepository giaoDichRepository;
    private final ModelMapper modelMapper;

    @Override
    public PhanHoi tongQuan(Integer year, Integer month, Integer lowStockLimit) {
        LocalDate today = LocalDate.now();
        int resolvedYear = (year == null || year < 2000) ? today.getYear() : year;
        int resolvedMonth = (month == null || month < 1 || month > 12) ? today.getMonthValue() : month;
        int limit = (lowStockLimit == null || lowStockLimit < 1) ? 5 : lowStockLimit;

        YearMonth currentPeriod = YearMonth.of(resolvedYear, resolvedMonth);
        LocalDateTime startOfMonth = currentPeriod.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentPeriod.atEndOfMonth().atTime(23, 59, 59);

        List<GiaoDich> monthTransactions = giaoDichRepository.findByCreatedAtBetween(startOfMonth, endOfMonth);
        DashboardAggregate monthAggregate = aggregateTransactions(monthTransactions);

        List<GiaoDich> todayTransactions = monthTransactions.stream()
                .filter(tx -> tx.getCreatedAt() != null && tx.getCreatedAt().toLocalDate().isEqual(today))
                .collect(Collectors.toList());
        DashboardAggregate todayAggregate = aggregateTransactions(todayTransactions);

        long productCount = sanPhamRepository.count();
        long supplierCount = nhaCungCapRepository.count();
        long totalStock = Optional.ofNullable(sanPhamRepository.sumTotalStock()).orElse(0L);
        BigDecimal stockValue = Optional.ofNullable(sanPhamRepository.sumTotalStockValue()).orElse(BigDecimal.ZERO);

        List<SanPham> lowStockEntities = sanPhamRepository.findLowStockProducts();
        long lowStockCount = lowStockEntities.size();
        List<SanPhamDTO> lowStockDtos = lowStockEntities.stream()
                .sorted(Comparator.comparing(
                        SanPham::getStockQuantity,
                        Comparator.nullsLast(Integer::compareTo)
                ))
                .limit(limit)
                .map(sanPham -> modelMapper.map(sanPham, SanPhamDTO.class))
                .toList();

        List<DashboardDoanhThuDanhMucDTO> revenueByCategory = buildRevenueByCategory(monthTransactions);
        List<DashboardNhapXuatThangDTO> flowStats = buildFlowStats(currentPeriod);

        DashboardTongQuanDTO tongQuanDTO = DashboardTongQuanDTO.builder()
                .doanhThuHomNay(todayAggregate.netRevenue())
                .doanhThuThang(monthAggregate.netRevenue())
                .tongGiaTriTon(stockValue)
                .tongTonKho(totalStock)
                .tongSanPham(productCount)
                .tongNhaCungCap(supplierCount)
                .sanPhamSapHet(lowStockCount)
                .tongDonBan(monthAggregate.saleCount())
                .tongDonNhap(monthAggregate.purchaseCount())
                .build();

        return PhanHoi.builder()
                .status(200)
                .message("Thanh cong")
                .dashboardTongQuan(tongQuanDTO)
                .sanPhams(lowStockDtos)
                .doanhThuTheoDanhMucs(revenueByCategory)
                .nhapXuatTheoThangs(flowStats)
                .build();
    }

    private DashboardAggregate aggregateTransactions(List<GiaoDich> transactions) {
        BigDecimal revenue = BigDecimal.ZERO;
        long saleCount = 0;
        long purchaseCount = 0;

        if (transactions == null) {
            return new DashboardAggregate(revenue, saleCount, purchaseCount);
        }

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getTransactionType() == null) {
                continue;
            }
            BigDecimal amount = safeAmount(giaoDich.getTotalPrice());
            switch (giaoDich.getTransactionType()) {
                case SALE -> {
                    revenue = revenue.add(amount);
                    saleCount++;
                }
                case RETURN_FROM_CUSTOMER -> revenue = revenue.subtract(amount);
                case PURCHASE -> purchaseCount++;
                default -> {
                }
            }
        }
        return new DashboardAggregate(revenue, saleCount, purchaseCount);
    }

    private List<DashboardDoanhThuDanhMucDTO> buildRevenueByCategory(List<GiaoDich> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }
        Map<Long, CategoryRevenue> aggregates = new LinkedHashMap<>();

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getTransactionType() == null
                    || giaoDich.getSanPham() == null
                    || giaoDich.getSanPham().getCategory() == null) {
                continue;
            }
            LoaiGiaoDich type = giaoDich.getTransactionType();
            if (type != LoaiGiaoDich.SALE && type != LoaiGiaoDich.RETURN_FROM_CUSTOMER) {
                continue;
            }
            Long categoryId = giaoDich.getSanPham().getCategory().getId();
            String categoryName = giaoDich.getSanPham().getCategory().getName();
            CategoryRevenue categoryRevenue = aggregates.computeIfAbsent(
                    categoryId,
                    id -> new CategoryRevenue(categoryId, categoryName)
            );
            BigDecimal amount = safeAmount(giaoDich.getTotalPrice());
            if (type == LoaiGiaoDich.SALE) {
                categoryRevenue.add(amount);
            } else {
                categoryRevenue.add(amount.negate());
            }
        }

        if (aggregates.isEmpty()) {
            return List.of();
        }

        BigDecimal totalRevenue = aggregates.values().stream()
                .map(CategoryRevenue::revenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal divisor = totalRevenue.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ONE
                : totalRevenue;

        return aggregates.values().stream()
                .sorted((a, b) -> b.revenue().compareTo(a.revenue()))
                .map(item -> DashboardDoanhThuDanhMucDTO.builder()
                        .danhMucId(item.categoryId())
                        .tenDanhMuc(item.categoryName())
                        .doanhThu(item.revenue())
                        .tyLeDongGop(
                                item.revenue()
                                        .multiply(BigDecimal.valueOf(100))
                                        .divide(divisor, 2, RoundingMode.HALF_UP)
                                        .doubleValue()
                        )
                        .build())
                .toList();
    }

    private List<DashboardNhapXuatThangDTO> buildFlowStats(YearMonth endPeriod) {
        YearMonth startPeriod = endPeriod.minusMonths(5);
        LocalDateTime start = startPeriod.atDay(1).atStartOfDay();
        LocalDateTime end = endPeriod.atEndOfMonth().atTime(23, 59, 59);

        List<GiaoDich> transactions = giaoDichRepository.findByCreatedAtBetween(start, end);
        Map<YearMonth, FlowBucket> buckets = new LinkedHashMap<>();

        YearMonth cursor = startPeriod;
        while (!cursor.isAfter(endPeriod)) {
            buckets.put(cursor, new FlowBucket(cursor));
            cursor = cursor.plusMonths(1);
        }

        for (GiaoDich giaoDich : transactions) {
            if (giaoDich.getCreatedAt() == null || giaoDich.getTransactionType() == null) {
                continue;
            }
            YearMonth eventMonth = YearMonth.from(giaoDich.getCreatedAt());
            FlowBucket bucket = buckets.get(eventMonth);
            if (bucket == null) {
                continue;
            }
            BigDecimal amount = safeAmount(giaoDich.getTotalPrice());
            bucket.addAmount(giaoDich.getTransactionType(), amount);
        }

        return buckets.values().stream()
                .map(bucket -> DashboardNhapXuatThangDTO.builder()
                        .nam(bucket.period().getYear())
                        .thang(bucket.period().getMonthValue())
                        .nhan(String.format("%02d/%d", bucket.period().getMonthValue(), bucket.period().getYear()))
                        .tongNhap(bucket.getNhap())
                        .tongXuat(bucket.getXuat())
                        .build())
                .toList();
    }

    private BigDecimal safeAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private record DashboardAggregate(BigDecimal netRevenue, long saleCount, long purchaseCount) {
    }

    private static class CategoryRevenue {
        private final Long categoryId;
        private final String categoryName;
        private BigDecimal revenue = BigDecimal.ZERO;

        private CategoryRevenue(Long categoryId, String categoryName) {
            this.categoryId = categoryId;
            this.categoryName = categoryName;
        }

        private void add(BigDecimal amount) {
            revenue = revenue.add(amount);
        }

        private Long categoryId() {
            return categoryId;
        }

        private String categoryName() {
            return categoryName;
        }

        private BigDecimal revenue() {
            return revenue;
        }
    }

    private static class FlowBucket {
        private final YearMonth period;
        private BigDecimal nhap = BigDecimal.ZERO;
        private BigDecimal xuat = BigDecimal.ZERO;

        FlowBucket(YearMonth period) {
            this.period = period;
        }

        void addAmount(LoaiGiaoDich type, BigDecimal amount) {
            switch (type) {
                case PURCHASE -> nhap = nhap.add(amount);
                case RETURN_TO_SUPPLIER -> nhap = nhap.subtract(amount);
                case SALE -> xuat = xuat.add(amount);
                case RETURN_FROM_CUSTOMER -> xuat = xuat.subtract(amount);
                default -> {
                }
            }
        }

        YearMonth period() {
            return period;
        }

        BigDecimal getNhap() {
            return nhap;
        }

        BigDecimal getXuat() {
            return xuat;
        }
    }
}
