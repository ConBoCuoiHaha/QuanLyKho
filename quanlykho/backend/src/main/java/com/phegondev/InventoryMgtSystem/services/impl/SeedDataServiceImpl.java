package com.phegondev.InventoryMgtSystem.services.impl;

import com.phegondev.InventoryMgtSystem.dtos.PhanHoi;
import com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich;
import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import com.phegondev.InventoryMgtSystem.models.*;
import com.phegondev.InventoryMgtSystem.repositories.*;
import com.phegondev.InventoryMgtSystem.services.SeedDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class SeedDataServiceImpl implements SeedDataService {

    private static final int PRODUCT_TARGET = 70;
    private static final int TRANSACTION_TARGET = 120;

    private final DanhMucRepository danhMucRepository;
    private final SanPhamRepository sanPhamRepository;
    private final NhaCungCapRepository nhaCungCapRepository;
    private final UserRepository userRepository;
    private final GiaoDichRepository giaoDichRepository;
    private final KhoRepository khoRepository;
    private final TonKhoRepository tonKhoRepository;
    private final KhachHangRepository khachHangRepository;

    @Override
    @Transactional
    public PhanHoi taoDuLieuDemo(boolean force) {
        if (!force && sanPhamRepository.count() > 0) {
            return PhanHoi.builder()
                    .status(200)
                    .message("Database da co du lieu, bo qua seed (set force=true neu muon tao lai)")
                    .build();
        }

        if (force) {
            log.warn("Force seed: xoa tat ca du lieu truoc khi tao moi");
            giaoDichRepository.deleteAllInBatch();
            tonKhoRepository.deleteAllInBatch();
            sanPhamRepository.deleteAllInBatch();
            loaiBoKhoLienKet();
            nhaCungCapRepository.deleteAllInBatch();
            khachHangRepository.deleteAllInBatch();
            danhMucRepository.deleteAllInBatch();
        }

        Random random = new Random();

        List<User> users = ensureDefaultUsers();
        List<DanhMuc> categories = createCategories();
        List<NhaCungCap> suppliers = createSuppliers();
        List<KhachHang> customers = createCustomers();
        List<Kho> warehouses = createWarehouses();
        List<SanPham> products = createProducts(categories, warehouses, random);
        createTransactions(products, suppliers, customers, users, random);

        return PhanHoi.builder()
                .status(200)
                .message("Da tao du lieu demo thanh cong")
                .totalElements((long) products.size())
                .soLuongThanhCong(TRANSACTION_TARGET)
                .build();
    }

    private void loaiBoKhoLienKet() {
        khoRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    private List<User> ensureDefaultUsers() {
        if (userRepository.count() == 0) {
            String defaultPassword = new BCryptPasswordEncoder().encode("12345678");
            userRepository.save(User.builder()
                    .name("Admin")
                    .email("admin@example.com")
                    .password(defaultPassword)
                    .phoneNumber("0999")
                    .role(VaiTroNguoiDung.ADMIN)
                    .build());
            userRepository.save(User.builder()
                    .name("Quan ly")
                    .email("manager@example.com")
                    .password(defaultPassword)
                    .phoneNumber("0888")
                    .role(VaiTroNguoiDung.MANAGER)
                    .build());
            userRepository.save(User.builder()
                    .name("Thu kho")
                    .email("warehouse@example.com")
                    .password(defaultPassword)
                    .phoneNumber("0777")
                    .role(VaiTroNguoiDung.WAREHOUSE_STAFF)
                    .build());
            userRepository.save(User.builder()
                    .name("Ke toan")
                    .email("accountant@example.com")
                    .password(defaultPassword)
                    .phoneNumber("0666")
                    .role(VaiTroNguoiDung.ACCOUNTANT)
                    .build());
            userRepository.save(User.builder()
                    .name("Ban hang")
                    .email("sales@example.com")
                    .password(defaultPassword)
                    .phoneNumber("0555")
                    .role(VaiTroNguoiDung.SALE_STAFF)
                    .build());
        }
        return userRepository.findAll();
    }

    private List<DanhMuc> createCategories() {
        List<String> names = List.of(
                "Dien tu", "Gia dung", "Van phong", "Thoi trang",
                "Thiet bi y te", "May moc", "Noi that", "Sach vo"
        );
        List<DanhMuc> categories = new ArrayList<>();
        names.forEach(name -> categories.add(DanhMuc.builder().name(name).build()));
        return danhMucRepository.saveAll(categories);
    }

    private List<NhaCungCap> createSuppliers() {
        List<String> suppliers = List.of(
                "Cong ty Alpha", "Cong ty Beta", "Cong ty Gama", "Cong ty Delta", "Cong ty Epsilon",
                "Cong ty Zeta", "Cong ty Eta", "Cong ty Theta", "Cong ty Iota", "Cong ty Kappa"
        );
        List<NhaCungCap> saved = new ArrayList<>();
        AtomicInteger index = new AtomicInteger(1);
        suppliers.forEach(name -> saved.add(
                nhaCungCapRepository.save(
                        NhaCungCap.builder().name(name)
                                .contactInfo("Lien he: 0" + (9000 + index.get()))
                                .phone("09" + (8000 + index.getAndIncrement()))
                                .email(slugify(name) + "@supplier.com")
                                .address(name.contains("Ha") ? "Ha Noi" : "Ho Chi Minh")
                                .build()
                )
        ));
        return saved;
    }

    private List<KhachHang> createCustomers() {
        List<String> names = List.of("Nguyen Van A", "Tran Thi B", "Le Van C", "Pham Thi D", "Hoang Van E");
        List<KhachHang> customers = new ArrayList<>();
        AtomicInteger idx = new AtomicInteger(1);
        names.forEach(name -> customers.add(khachHangRepository.save(
                KhachHang.builder()
                        .name(name)
                        .email(slugify(name) + "@customer.com")
                        .phone("0987" + idx.getAndIncrement() + "123")
                        .address("So " + (10 + idx.get()) + " Duong " + name.split(" ")[1])
                        .notes("Khach hang VIP " + idx.get())
                        .build()
        )));
        return customers;
    }

    private List<Kho> createWarehouses() {
        List<Kho> warehouses = List.of(
                Kho.builder().name("Kho Bac").address("Ha Noi").manager("Nguyen Quan Ly 1").build(),
                Kho.builder().name("Kho Trung").address("Da Nang").manager("Tran Quan Ly 2").build(),
                Kho.builder().name("Kho Nam").address("Ho Chi Minh").manager("Le Quan Ly 3").build()
        );
        return khoRepository.saveAll(warehouses);
    }

    private List<SanPham> createProducts(List<DanhMuc> categories, List<Kho> warehouses, Random random) {
        String[] productPrefixes = {"Bo", "Combo", "Bo doi", "Bo kit", "Mo dun", "Thiet bi"};
        String[] productItems = {"Cam bien", "Den thong minh", "Loa bluetooth", "May in", "Ban lam viec", "Ghe gaming",
                "May khoan", "May hut bui", "Hop dung ho so", "Camera an ninh", "Router WiFi", "Ban phim co"};
        String[] descriptions = {
                "San pham chat luong cao phu hop kho doanh nghiep",
                "Ban chay hang thang voi do ben tot",
                "Phien ban nang cap nam 2025",
                "Tich hop nhieu tinh nang thong minh",
                "Duoc bao hanh 24 thang toan quoc"
        };

        List<SanPham> products = new ArrayList<>();
        AtomicInteger skuCounter = new AtomicInteger(1);

        for (int i = 0; i < PRODUCT_TARGET; i++) {
            DanhMuc category = categories.get(random.nextInt(categories.size()));
            String name = productPrefixes[random.nextInt(productPrefixes.length)] + " " +
                    productItems[random.nextInt(productItems.length)];
            String sku = String.format("SP-%04d", skuCounter.getAndIncrement());
            BigDecimal price = BigDecimal.valueOf(50_000 + random.nextInt(1_500_000))
                    .setScale(0, RoundingMode.HALF_UP);
            int totalStock = 30 + random.nextInt(170);
            int minStock = 5 + random.nextInt(15);

            SanPham sanPham = SanPham.builder()
                    .name(name)
                    .sku(sku)
                    .price(price)
                    .stockQuantity(totalStock)
                    .minStock(minStock)
                    .reservedQuantity(0)
                    .description(descriptions[random.nextInt(descriptions.length)])
                    .category(category)
                    .expiryDate(random.nextBoolean() ? LocalDate.now().plusDays(90 + random.nextInt(365)).atStartOfDay() : null)
                    .build();
            sanPham = sanPhamRepository.save(sanPham);

            distributeStockAcrossWarehouses(sanPham, totalStock, warehouses, random);
            products.add(sanPham);
        }

        return products;
    }

    private void distributeStockAcrossWarehouses(SanPham sanPham, int total, List<Kho> warehouses, Random random) {
        List<Kho> shuffled = new ArrayList<>(warehouses);
        Collections.shuffle(shuffled, random);
        int remaining = total;

        for (int i = 0; i < shuffled.size(); i++) {
            if (remaining <= 0) {
                break;
            }
            int qty = (i == shuffled.size() - 1) ? remaining : random.nextInt(remaining + 1);
            if (qty == 0) {
                continue;
            }
            tonKhoRepository.save(TonKho.builder()
                    .sanPham(sanPham)
                    .warehouse(shuffled.get(i))
                    .quantity(qty)
                    .build());
            remaining -= qty;
        }
    }

    private void createTransactions(List<SanPham> products,
                                    List<NhaCungCap> suppliers,
                                    List<KhachHang> customers,
                                    List<User> users,
                                    Random random) {
        List<GiaoDich> transactions = new ArrayList<>();
        for (int i = 0; i < TRANSACTION_TARGET; i++) {
            SanPham product = products.get(random.nextInt(products.size()));
            int quantity = 1 + random.nextInt(20);
            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(quantity));

            LoaiGiaoDich type = pickTransactionType(random);
            TrangThaiGiaoDich status = TrangThaiGiaoDich.values()[random.nextInt(TrangThaiGiaoDich.values().length)];

            GiaoDich.GiaoDichBuilder builder = GiaoDich.builder()
                    .sanPham(product)
                    .user(users.get(random.nextInt(users.size())))
                    .transactionType(type)
                    .status(status)
                    .totalProducts(quantity)
                    .totalPrice(totalPrice)
                    .description("Giao dich tu dong so " + (i + 1));

            if (type == LoaiGiaoDich.PURCHASE || type == LoaiGiaoDich.RETURN_TO_SUPPLIER) {
                builder.nhaCungCap(suppliers.get(random.nextInt(suppliers.size())));
            } else {
                builder.customer(customers.get(random.nextInt(customers.size())));
            }
            transactions.add(builder.build());
        }
        giaoDichRepository.saveAll(transactions);
    }

    private LoaiGiaoDich pickTransactionType(Random random) {
        LoaiGiaoDich[] types = {LoaiGiaoDich.PURCHASE, LoaiGiaoDich.SALE,
                LoaiGiaoDich.RETURN_TO_SUPPLIER, LoaiGiaoDich.RETURN_FROM_CUSTOMER};
        return types[random.nextInt(types.length)];
    }

    private String slugify(String name) {
        return name.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", ".");
    }
}
