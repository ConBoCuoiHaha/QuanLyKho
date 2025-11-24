import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";
import {
  hienThiLoaiGiaoDich,
  hienThiTrangThaiGiaoDich,
} from "../utils/hienThi";

const ChiTietGiaoDich = () => {
  const { giaoDichId } = useParams();
  const [giaoDich, setGiaoDich] = useState(null);
  const [message, setMessage] = useState("");
  const [status, setStatus] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const loadTransaction = async () => {
      try {
        const response = await ApiService.getTransactionById(giaoDichId);
        if (response.status === 200) {
          setGiaoDich(response.giaoDich);
          setStatus(response.giaoDich.status);
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải giao dịch: " + error
        );
      }
    };
    loadTransaction();
  }, [giaoDichId]);

  const showMessage = (text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleUpdateStatus = async () => {
    try {
      await ApiService.updateTransactionStatus(giaoDichId, status);
      showMessage("Cập nhật trạng thái thành công");
      navigate("/giao-dich");
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không cập nhật được giao dịch: " + error
      );
    }
  };

  return (
    <Layout>
      {message && <p className="message">{message}</p>}
      <div className="transaction-details-page">
        {giaoDich && (
          <>
            <div className="section-card">
              <h2>Thông tin giao dịch</h2>
              <p>Loại: {hienThiLoaiGiaoDich(giaoDich.transactionType)}</p>
              <p>Trạng thái: {hienThiTrangThaiGiaoDich(giaoDich.status)}</p>
              <p>Mô tả: {giaoDich.description || "Không có"}</p>
              <p>Ghi chú: {giaoDich.note || "Không có"}</p>
              <p>Tổng sản phẩm: {giaoDich.totalProducts}</p>
              <p>
                Tổng tiền:{" "}
                {giaoDich.totalPrice
                  ? giaoDich.totalPrice.toLocaleString("vi-VN")
                  : 0}{" "}
                VND
              </p>
              <p>
                Ngày tạo:{" "}
                {giaoDich.createdAt
                  ? new Date(giaoDich.createdAt).toLocaleString()
                  : "--"}
              </p>
              {giaoDich.updatedAt && (
                <p>
                  Cập nhật lúc:{" "}
                  {new Date(giaoDich.updatedAt).toLocaleString("vi-VN")}
                </p>
              )}
            </div>

            <div className="section-card">
              <h2>Thông tin sản phẩm</h2>
              <p>Tên: {giaoDich.sanPham?.name}</p>
              <p>SKU: {giaoDich.sanPham?.sku}</p>
              <p>
                Giá:{" "}
                {giaoDich.sanPham?.price
                  ? giaoDich.sanPham.price.toLocaleString("vi-VN")
                  : 0}{" "}
                VND
              </p>
              <p>Tồn kho: {giaoDich.sanPham?.stockQuantity}</p>
              <p>Mô tả: {giaoDich.sanPham?.description || "Không có"}</p>
              {giaoDich.sanPham?.imageUrl && (
                <img
                  src={giaoDich.sanPham.imageUrl}
                  alt={giaoDich.sanPham.name}
                />
              )}
            </div>

            <div className="section-card">
              <h2>Thông tin khách hàng / nhà cung cấp</h2>
              {giaoDich.customer ? (
                <>
                  <p>Khách hàng: {giaoDich.customer.name}</p>
                  <p>Email: {giaoDich.customer.email}</p>
                  <p>Số điện thoại: {giaoDich.customer.phone}</p>
                </>
              ) : (
                <>
                  <p>Nhà cung cấp: {giaoDich.nhaCungCap?.name}</p>
                  <p>Liên hệ: {giaoDich.nhaCungCap?.phone}</p>
                </>
              )}
            </div>

            <div className="transaction-status-update">
              <h2>Cập nhật trạng thái</h2>
              <select
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="PENDING">Chờ xử lý</option>
                <option value="PROCESSING">Đang xử lý</option>
                <option value="COMPLETED">Hoàn tất</option>
                <option value="CANCELLED">Đã hủy</option>
              </select>
              <button type="button" onClick={handleUpdateStatus}>
                Lưu trạng thái
              </button>
            </div>
          </>
        )}
      </div>
    </Layout>
  );
};

export default ChiTietGiaoDich;
