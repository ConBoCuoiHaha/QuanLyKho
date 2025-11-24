package com.phegondev.InventoryMgtSystem.dtos;


import com.fasterxml.jackson.annotation.JsonInclude;
import com.phegondev.InventoryMgtSystem.enums.VaiTroNguoiDung;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhanHoi {

    //Generic
    private int status;
    private String message;
    //for login
    private String token;
    private VaiTroNguoiDung role;
    private String expirationTime;

    //for pagination
    private Integer totalPages;
    private Long totalElements;

    //data output optionals
    private UserDTO user;
    private List<UserDTO> users;

    private NhaCungCapDTO nhaCungCap;
    private List<NhaCungCapDTO> nhaCungCaps;

    private CategoryDTO category;
    private List<CategoryDTO> categories;

    private SanPhamDTO sanPham;
    private List<SanPhamDTO> sanPhams;

    private List<KhoDTO> khos;
    private List<TonKhoDTO> tonKhos;
    private List<KhoThongKeDTO> thongKeKhos;

    private KhachHangDTO khachHang;
    private List<KhachHangDTO> khachHangs;

    private GiaoDichDTO giaoDich;
    private List<GiaoDichDTO> giaoDichs;

    private DonDatHangDTO donDatHang;
    private List<DonDatHangDTO> donDatHangs;

    private DonBanHangDTO donBanHang;
    private List<DonBanHangDTO> donBanHangs;

    private LoHangDTO loHang;
    private List<LoHangDTO> loHangs;

    private KiemKeDTO kiemKe;
    private List<KiemKeDTO> kiemKes;

    private Integer soLuongHienTai;
    private BaoCaoTonKhoTongDTO baoCaoTonKhoTong;
    private List<GiaTriTonTheoDanhMucDTO> giaTriTonTheoDanhMuc;
    private List<GiaTriTonTheoKhoDTO> giaTriTonTheoKho;
    private List<BaoCaoXuatNhapTonDTO> baoCaoXuatNhapTon;
    private List<BaoCaoXuatNhapTonDanhMucDTO> baoCaoXuatNhapTonTheoDanhMuc;
    private BaoCaoXuatNhapTonTongDTO baoCaoXuatNhapTonTong;
    private List<HangTonLauDTO> hangTonLaus;
    private HangTonLauThongKeDTO thongKeHangTonLau;
    private List<ABCPhanTichDTO> baoCaoABC;
    private ABCThongKeDTO thongKeABC;
    private List<SanPhamBanChayDTO> sanPhamBanChay;
    private BaoCaoDoanhThuTongDTO baoCaoDoanhThuTong;
    private List<DoanhThuTheoKyDTO> doanhThuTheoKys;
    private DashboardTongQuanDTO dashboardTongQuan;
    private List<DashboardDoanhThuDanhMucDTO> doanhThuTheoDanhMucs;
    private List<DashboardNhapXuatThangDTO> nhapXuatTheoThangs;
    private List<NhaCungCapBaoCaoDTO> nhaCungCapBaoCao;
    private NhatKyDTO nhatKy;
    private List<NhatKyDTO> nhatKys;
    private Integer soLuongThanhCong;
    private Integer soLuongThatBai;
    private List<String> loiNhap;
    private String tepTin;

    private final LocalDateTime timestamp = LocalDateTime.now();


}
