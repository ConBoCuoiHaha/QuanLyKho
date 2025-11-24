import React, { useCallback, useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import {
  hienThiLoaiGiaoDich,
  hienThiTrangThaiGiaoDich,
} from "../utils/hienThi";

const initialForm = {
  name: "",
  phone: "",
  email: "",
  address: "",
  notes: "",
};

const KhachHang = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [formData, setFormData] = useState(initialForm);
  const [editingId, setEditingId] = useState(null);
  const [detail, setDetail] = useState(null);
  const [history, setHistory] = useState([]);
  const [showDetail, setShowDetail] = useState(false);

  const showMessage = useCallback((text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadCustomers = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.getCustomers();
      if (response.status === 200) {
        setCustomers(response.khachHangs || []);
      } else {
        showMessage(response.message || "Không thể tải danh sách khách hàng");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tải danh sách khách hàng: " + error
      );
    } finally {
      setLoading(false);
    }
  }, [showMessage]);

  useEffect(() => {
    loadCustomers();
  }, [loadCustomers]);

  const handleInputChange = (event) => {
    const { name, value } = event.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleSubmit = async (event) => {
    event.preventDefault();
    try {
      if (editingId) {
        await ApiService.updateCustomer(editingId, formData);
        showMessage("Cập nhật khách hàng thành công");
      } else {
        await ApiService.createCustomer(formData);
        showMessage("Thêm khách hàng thành công");
      }
      setFormData(initialForm);
      setEditingId(null);
      loadCustomers();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể lưu khách hàng: " + error
      );
    }
  };

  const handleEdit = (customer) => {
    setEditingId(customer.id);
    setFormData({
      name: customer.name || "",
      phone: customer.phone || "",
      email: customer.email || "",
      address: customer.address || "",
      notes: customer.notes || "",
    });
  };

  const handleDelete = async (customerId) => {
    if (!window.confirm("Bạn có chắc chắn muốn xóa khách hàng này?")) {
      return;
    }
    try {
      await ApiService.deleteCustomer(customerId);
      showMessage("Xóa khách hàng thành công");
      loadCustomers();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể xóa khách hàng: " + error
      );
    }
  };

  const openDetail = async (customerId) => {
    setShowDetail(true);
    setDetail(null);
    setHistory([]);
    try {
      const response = await ApiService.getCustomerDetail(customerId);
      if (response.status === 200) {
        setDetail(response.khachHang || null);
        setHistory(response.giaoDichs || []);
      } else {
        showMessage(response.message || "Không thể tải chi tiết khách hàng");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tải chi tiết khách hàng: " + error
      );
    }
  };

  const closeDetail = () => {
    setShowDetail(false);
    setDetail(null);
    setHistory([]);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="page-header">
        <div>
          <h1>Khách hàng</h1>
          <p className="text-subtle">Quản lý thông tin khách hàng và công nợ</p>
        </div>
      </div>

      <div className="form-card">
        <h3>{editingId ? "Cập nhật khách hàng" : "Thêm khách hàng"}</h3>
        <form className="form-grid" onSubmit={handleSubmit}>
          <input
            type="text"
            name="name"
            placeholder="Tên khách hàng"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
          <input
            type="text"
            name="phone"
            placeholder="Số điện thoại"
            value={formData.phone}
            onChange={handleInputChange}
          />
          <input
            type="email"
            name="email"
            placeholder="Email"
            value={formData.email}
            onChange={handleInputChange}
          />
          <input
            type="text"
            name="address"
            placeholder="Địa chỉ"
            value={formData.address}
            onChange={handleInputChange}
          />
          <textarea
            name="notes"
            placeholder="Ghi chú"
            value={formData.notes}
            onChange={handleInputChange}
          />
          <div className="form-actions">
            <button type="submit">
              {editingId ? "Cập nhật" : "Thêm mới"}
            </button>
            {editingId && (
              <button
                type="button"
                className="secondary"
                onClick={() => {
                  setEditingId(null);
                  setFormData(initialForm);
                }}
              >
                Hủy
              </button>
            )}
          </div>
        </form>
      </div>

      <div className="table-wrapper">
        <div className="table-title">Danh sách khách hàng</div>
        {loading ? (
          <p className="empty-text">Đang tải...</p>
        ) : (
          <table className="simple-table">
            <thead>
              <tr>
                <th>Tên</th>
                <th>SĐT</th>
                <th>Email</th>
                <th>Địa chỉ</th>
                <th>Tổng chi tiêu</th>
                <th>Công nợ</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {customers.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-text">
                    Chưa có khách hàng
                  </td>
                </tr>
              )}
              {customers.map((customer) => (
                <tr key={customer.id}>
                  <td>{customer.name}</td>
                  <td>{customer.phone || "--"}</td>
                  <td>{customer.email || "--"}</td>
                  <td>{customer.address || "--"}</td>
                  <td>{formatCurrency(customer.tongChiTieu)}</td>
                  <td>{formatCurrency(customer.congNo)}</td>
                  <td className="table-actions">
                    <button onClick={() => openDetail(customer.id)}>
                      Chi tiết
                    </button>
                    <button onClick={() => handleEdit(customer)}>Sửa</button>
                    <button
                      className="danger"
                      onClick={() => handleDelete(customer.id)}
                    >
                      Xóa
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showDetail && detail && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>{detail.name}</h3>
              <button onClick={closeDetail}>Đóng</button>
            </div>
            <div className="modal-body">
              <p>SĐT: {detail.phone || "--"}</p>
              <p>Email: {detail.email || "--"}</p>
              <p>Địa chỉ: {detail.address || "--"}</p>
              <p>Tổng giao dịch: {detail.tongGiaoDich ?? 0}</p>
              <p>Tổng chi tiêu: {formatCurrency(detail.tongChiTieu)}</p>
              <p>Công nợ: {formatCurrency(detail.congNo)}</p>
              {detail.notes && (
                <p>
                  Ghi chú: <em>{detail.notes}</em>
                </p>
              )}

              <h4>Lịch sử giao dịch</h4>
              <div className="table-scroll">
                <table className="simple-table">
                  <thead>
                    <tr>
                      <th>Ngày</th>
                      <th>Loại</th>
                      <th>Giá trị</th>
                      <th>Trạng thái</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.length === 0 && (
                      <tr>
                        <td colSpan="4" className="empty-text">
                          Chưa có giao dịch
                        </td>
                      </tr>
                    )}
                    {history.map((tx) => (
                      <tr key={tx.id}>
                        <td>
                          {tx.createdAt
                            ? new Date(tx.createdAt).toLocaleDateString()
                            : "--"}
                        </td>
                        <td>{hienThiLoaiGiaoDich(tx.transactionType)}</td>
                        <td>{formatCurrency(tx.totalPrice)}</td>
                        <td>{hienThiTrangThaiGiaoDich(tx.status)}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

const formatCurrency = (value) => {
  if (value === null || value === undefined) {
    return "0";
  }
  const number = Number(value);
  if (Number.isNaN(number)) {
    return "0";
  }
  return new Intl.NumberFormat("en-US").format(Math.round(number)) + " VND";
};

export default KhachHang;

