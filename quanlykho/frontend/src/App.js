import React from "react";
import { BrowserRouter as Router, Routes, Route } from "react-router-dom";
import { ProtectedRoute, AdminRoute, RoleRoute } from "./service/Guard";
import DangKy from "./pages/DangKy";
import DangNhap from "./pages/DangNhap";
import DanhMuc from "./pages/DanhMuc";
import NhaCungCap from "./pages/NhaCungCap";
import ThemSuaNhaCungCap from "./pages/ThemSuaNhaCungCap";
import SanPham from "./pages/SanPham";
import ThemSuaSanPham from "./pages/ThemSuaSanPham";
import NhapHang from "./pages/NhapHang";
import BanHang from "./pages/BanHang";
import GiaoDich from "./pages/GiaoDich";
import ChiTietGiaoDich from "./pages/ChiTietGiaoDich";
import HoSo from "./pages/HoSo";
import BangDieuKhien from "./pages/BangDieuKhien";
import KiemKe from "./pages/KiemKe";
import MaVach from "./pages/MaVach";
import QuetMa from "./pages/QuetMa";
import QuanLyKho from "./pages/QuanLyKho";
import TraHangKhach from "./pages/TraHangKhach";
import BaoCao from "./pages/BaoCao";
import KhachHang from "./pages/KhachHang";
import DonDatHang from "./pages/DonDatHang";
import DonBanHang from "./pages/DonBanHang";
import NhatKy from "./pages/NhatKy";
import SaoLuu from "./pages/SaoLuu";
import QuenMatKhau from "./pages/QuenMatKhau";

const KHO_ROLES = ["ADMIN", "MANAGER", "WAREHOUSE_STAFF"];
const SALES_ROLES = ["ADMIN", "MANAGER", "SALE_STAFF"];
const REPORT_ROLES = ["ADMIN", "MANAGER", "ACCOUNTANT"];
const GIAO_DICH_ROLES = ["ADMIN", "MANAGER", "WAREHOUSE_STAFF", "SALE_STAFF", "ACCOUNTANT"];
const BARCODE_ROLES = ["ADMIN", "MANAGER", "WAREHOUSE_STAFF", "SALE_STAFF"];
const AUDIT_ROLES = ["ADMIN", "MANAGER"];
const CUSTOMER_ROLES = ["ADMIN", "MANAGER", "SALE_STAFF"];


function App() {
  return (
    <Router future={{ v7_startTransition: true, v7_relativeSplatPath: true }}>
      <Routes>
        <Route path="/dang-ky" element={<DangKy/>}/>
        <Route path="/dang-nhap" element={<DangNhap/>}/>
        <Route path="/quen-mat-khau" element={<QuenMatKhau/>}/>

        {/* ADMIN ROUTES */}
        <Route path="/danh-muc" element={<AdminRoute element={<DanhMuc/>}/>}/>
        <Route path="/nha-cung-cap" element={<AdminRoute element={<NhaCungCap/>}/>}/>
        <Route path="/them-nha-cung-cap" element={<AdminRoute element={<ThemSuaNhaCungCap/>}/>}/>
        <Route path="/sua-nha-cung-cap/:nhaCungCapId" element={<AdminRoute element={<ThemSuaNhaCungCap/>}/>}/>
        <Route path="/san-pham" element={<AdminRoute element={<SanPham/>}/>}/>


        <Route path="/them-san-pham" element={<AdminRoute element={<ThemSuaSanPham/>}/>}/>
        <Route path="/sua-san-pham/:productId" element={<AdminRoute element={<ThemSuaSanPham/>}/>}/>

          {/* ADMIN AND MANAGERS ROUTES */}
        <Route path="/nhap-hang" element={<RoleRoute roles={KHO_ROLES} element={<NhapHang/>}/>}/>
        <Route path="/ban-hang" element={<RoleRoute roles={SALES_ROLES} element={<BanHang/>}/>}/>
        <Route path="/giao-dich" element={<RoleRoute roles={GIAO_DICH_ROLES} element={<GiaoDich/>}/>}/>
        <Route path="/giao-dich/:giaoDichId" element={<RoleRoute roles={GIAO_DICH_ROLES} element={<ChiTietGiaoDich/>}/>}/>

        <Route path="/ho-so" element={<ProtectedRoute element={<HoSo/>}/>}/>
        <Route path="/bang-dieu-khien" element={<ProtectedRoute element={<BangDieuKhien/>}/>}/>
        <Route path="/kiem-ke" element={<RoleRoute roles={KHO_ROLES} element={<KiemKe/>}/>}/>
        <Route path="/ma-vach" element={<RoleRoute roles={BARCODE_ROLES} element={<MaVach/>}/>}/>
        <Route path="/quet-ma" element={<RoleRoute roles={BARCODE_ROLES} element={<QuetMa/>}/>}/>
        <Route path="/quan-ly-kho" element={<RoleRoute roles={KHO_ROLES} element={<QuanLyKho/>}/>}/>
        <Route path="/don-dat-hang" element={<RoleRoute roles={KHO_ROLES} element={<DonDatHang/>}/>}/>
        <Route path="/don-ban-hang" element={<RoleRoute roles={SALES_ROLES} element={<DonBanHang/>}/>}/>
        <Route path="/khach-hang" element={<RoleRoute roles={CUSTOMER_ROLES} element={<KhachHang/>}/>}/>
        <Route path="/nhat-ky" element={<RoleRoute roles={AUDIT_ROLES} element={<NhatKy/>}/>}/>
        <Route path="/tra-khach" element={<RoleRoute roles={SALES_ROLES} element={<TraHangKhach/>}/>}/>
        <Route path="/bao-cao" element={<RoleRoute roles={REPORT_ROLES} element={<BaoCao/>}/>}/>
        <Route path="/sao-luu" element={<RoleRoute roles={["ADMIN"]} element={<SaoLuu/>}/>}/>

        <Route path="*" element={<DangNhap/>}/>


        

      </Routes>
    </Router>
  )
}

export default App;
