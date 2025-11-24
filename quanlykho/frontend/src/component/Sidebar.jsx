import React from "react";
import { Link } from "react-router-dom";
import ApiService from "../service/ApiService";
import { useTheme } from "../context/ThemeContext";

const logout = () => {
  ApiService.logout();
};

const Sidebar = () => {
  const isAuth = ApiService.isAuthenticated();
  const quanTri = ApiService.hasAnyRole(["ADMIN", "MANAGER"]);
  const onlyAdmin = ApiService.hasRole("ADMIN");
  const khoRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "WAREHOUSE_STAFF"]);
  const salesRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "SALE_STAFF"]);
  const baoCaoRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "ACCOUNTANT"]);
  const giaoDichRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "WAREHOUSE_STAFF", "SALE_STAFF", "ACCOUNTANT"]);
  const barcodeRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "WAREHOUSE_STAFF", "SALE_STAFF"]);
  const auditRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER"]);
  const khachHangRoles = ApiService.hasAnyRole(["ADMIN", "MANAGER", "SALE_STAFF"]);
  const { theme, toggleTheme } = useTheme();

  return (
    <div className="sidebar">
      <h1 className="ims">QLK</h1>
      <ul className="nav-links">
        {isAuth && (
          <li>
            <Link to="/bang-dieu-khien">Bảng điều khiển</Link>
          </li>
        )}

        {isAuth && baoCaoRoles && (
          <li>
            <Link to="/bao-cao">Báo cáo</Link>
          </li>
        )}

        {isAuth && giaoDichRoles && (
          <li>
            <Link to="/giao-dich">Giao dịch</Link>
          </li>
        )}

        {isAuth && quanTri && (
          <>
            <li>
              <Link to="/danh-muc">Danh mục</Link>
            </li>
            <li>
              <Link to="/san-pham">Sản phẩm</Link>
            </li>
            <li>
              <Link to="/nha-cung-cap">Nhà cung cấp</Link>
            </li>
          </>
        )}

        {isAuth && onlyAdmin && (
          <li>
            <Link to="/sao-luu">Sao lưu</Link>
          </li>
        )}

        {isAuth && khoRoles && (
          <li>
            <Link to="/nhap-hang">Nhập hàng</Link>
          </li>
        )}

        {isAuth && khoRoles && (
          <li>
            <Link to="/don-dat-hang">Đơn đặt hàng</Link>
          </li>
        )}

        {isAuth && salesRoles && (
          <li>
            <Link to="/don-ban-hang">Đơn bán hàng</Link>
          </li>
        )}

        {isAuth && khoRoles && (
          <li>
            <Link to="/kiem-ke">Kiểm kê</Link>
          </li>
        )}

        {isAuth && barcodeRoles && (
          <li>
            <Link to="/ma-vach">Mã vạch</Link>
          </li>
        )}

        {isAuth && barcodeRoles && (
          <li>
            <Link to="/quet-ma">Quét mã</Link>
          </li>
        )}

        {isAuth && khoRoles && (
          <li>
            <Link to="/quan-ly-kho">Quản lý kho</Link>
          </li>
        )}

        {isAuth && salesRoles && (
          <li>
            <Link to="/ban-hang">Bán hàng</Link>
          </li>
        )}

        {isAuth && khachHangRoles && (
          <li>
            <Link to="/khach-hang">Khách hàng</Link>
          </li>
        )}

        {isAuth && auditRoles && (
          <li>
            <Link to="/nhat-ky">Nhật ký</Link>
          </li>
        )}

        {isAuth && salesRoles && (
          <li>
            <Link to="/tra-khach">Trả hàng khách</Link>
          </li>
        )}

        {isAuth && (
          <li>
            <Link to="/ho-so">Hồ sơ</Link>
          </li>
        )}

        <li className="theme-toggle">
          <button type="button" onClick={toggleTheme}>
            {theme === "dark" ? "Chế độ sáng" : "Chế độ tối"}
          </button>
        </li>

        {isAuth && (
          <li>
            <Link onClick={logout} to="/dang-nhap">
              Đăng xuất
            </Link>
          </li>
        )}
      </ul>
    </div>
  );
};

export default Sidebar;
