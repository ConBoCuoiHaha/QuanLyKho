import React, { useCallback, useEffect, useMemo, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

// Trạng thái đơn bán hàng (enum TrangThaiDonBanHang + chi tiết)
const STATUS_LABELS = {
  NHAP: "Nhập",
  XAC_NHAN: "Xác nhận",
  DONG_GOI: "Đóng gói",
  GIAO_HANG: "Giao hàng",
  HOAN_THANH: "Hoàn thành",
  DA_HUY: "Đã hủy",
  CHO_XU_LY: "Chờ xử lý",
  DANG_GIAO: "Đang giao",
};

const STATUS_TRANSITIONS = {
  NHAP: [
    { value: "XAC_NHAN", label: "Xác nhận" },
    { value: "DA_HUY", label: "Hủy" },
  ],
  XAC_NHAN: [
    { value: "DONG_GOI", label: "Chuyển đóng gói" },
    { value: "DA_HUY", label: "Hủy" },
  ],
  DONG_GOI: [
    { value: "GIAO_HANG", label: "Đang giao" },
    { value: "DA_HUY", label: "Hủy" },
  ],
  GIAO_HANG: [
    { value: "HOAN_THANH", label: "Hoàn thành" },
    { value: "DA_HUY", label: "Hủy" },
  ],
};

const emptyItem = { sanPhamId: "", soLuong: 1, donGia: "" };

const DonBanHang = () => {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [customerFilter, setCustomerFilter] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [customers, setCustomers] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [form, setForm] = useState({
    khachHangId: "",
    khoId: "",
    ngayGiaoDuKien: "",
    ghiChu: "",
    items: [{ ...emptyItem }],
  });
  const [selectedOrder, setSelectedOrder] = useState(null);

  const showMessage = useCallback((text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const handleAuthError = useCallback(
    (error) => {
      if (error?.response?.status === 401) {
        ApiService.logout();
        showMessage("Phiên đăng nhập hết hạn, vui lòng đăng nhập lại");
        window.location.href = "/dang-nhap";
        return true;
      }
      return false;
    },
    [showMessage]
  );

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.getSalesOrders({
        page,
        size: 10,
        status: statusFilter || undefined,
        khachHangId: customerFilter || undefined,
      });
      if (response.status === 200) {
        setOrders(response.donBanHangs || []);
        setTotalPages(response.totalPages || 0);
      } else {
        showMessage(response.message || "Không thể tải đơn bán hàng");
      }
    } catch (error) {
      if (!handleAuthError(error)) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tải đơn bán hàng: " + error
        );
      }
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, customerFilter, handleAuthError, showMessage]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    const loadReferences = async () => {
      try {
        const [customerRes, productRes, warehouseRes] = await Promise.all([
          ApiService.getCustomers(),
          ApiService.getAllProducts(),
          ApiService.getWarehouses(),
        ]);
        if (customerRes.status === 200) {
          setCustomers(customerRes.khachHangs || []);
        }
        if (productRes.status === 200) {
          setProducts(productRes.sanPhams || []);
        }
        if (warehouseRes.status === 200) {
          setWarehouses(warehouseRes.khos || []);
        }
      } catch (error) {
        if (!handleAuthError(error)) {
          showMessage(
            error.response?.data?.message ||
              "Không thể tải dữ liệu tham chiếu: " + error
          );
        }
      }
    };
    loadReferences();
  }, [handleAuthError, showMessage]);

  const handleFormChange = (event) => {
    const { name, value } = event.target;
    setForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleItemChange = (index, field, value) => {
    setForm((prev) => {
      const updated = [...prev.items];
      updated[index] = { ...updated[index], [field]: value };
      return { ...prev, items: updated };
    });
  };

  const addItemRow = () => {
    setForm((prev) => ({
      ...prev,
      items: [...prev.items, { ...emptyItem }],
    }));
  };

  const removeItemRow = (index) => {
    setForm((prev) => {
      if (prev.items.length === 1) {
        return prev;
      }
      return {
        ...prev,
        items: prev.items.filter((_, idx) => idx !== index),
      };
    });
  };

  const resetForm = () => {
    setForm({
      khachHangId: "",
      khoId: "",
      ngayGiaoDuKien: "",
      ghiChu: "",
      items: [{ ...emptyItem }],
    });
  };

  const handleCreateOrder = async (event) => {
    event.preventDefault();
    const payload = {
      khachHangId: form.khachHangId ? Number(form.khachHangId) : null,
      khoId: form.khoId ? Number(form.khoId) : null,
      ngayGiaoDuKien: form.ngayGiaoDuKien || null,
      ghiChu: form.ghiChu || "",
      items: form.items
        .filter((item) => item.sanPhamId && item.soLuong)
        .map((item) => ({
          sanPhamId: Number(item.sanPhamId),
          soLuong: Number(item.soLuong),
          donGia: item.donGia ? Number(item.donGia) : null,
        })),
    };

    if (!payload.khachHangId) {
      showMessage("Vui lòng chọn khách hàng");
      return;
    }
    if (payload.items.length === 0) {
      showMessage("Vui lòng thêm ít nhất 1 sản phẩm");
      return;
    }

    try {
      await ApiService.createSalesOrder(payload);
      showMessage("Tạo đơn bán hàng thành công");
      resetForm();
      loadOrders();
    } catch (error) {
      if (!handleAuthError(error)) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tạo đơn bán hàng: " + error
        );
      }
    }
  };

  const openDetail = async (id) => {
    try {
      const response = await ApiService.getSalesOrderDetail(id);
      if (response.status === 200) {
        setSelectedOrder(response.donBanHang);
      } else {
        showMessage(response.message || "Không thể tải chi tiết");
      }
    } catch (error) {
      if (!handleAuthError(error)) {
        showMessage(
          error.response?.data?.message || "Không thể tải chi tiết: " + error
        );
      }
    }
  };

  const closeDetail = () => {
    setSelectedOrder(null);
  };

  const handleUpdateStatus = async (orderId, nextStatus) => {
    try {
      await ApiService.updateSalesOrderStatus(orderId, nextStatus);
      showMessage("Cập nhật trạng thái thành công");
      loadOrders();
      if (selectedOrder?.id === orderId) {
        openDetail(orderId);
      }
    } catch (error) {
      if (!handleAuthError(error)) {
        showMessage(
          error.response?.data?.message ||
            "Không thể cập nhật trạng thái: " + error
        );
      }
    }
  };

  const invoiceLines = useMemo(() => {
    if (!selectedOrder?.chiTiets) return [];
    return selectedOrder.chiTiets.map((detail) => ({
      name: detail.sanPham?.name || "--",
      qty: detail.soLuong ?? 0,
      price: detail.donGia ?? 0,
      total: (detail.soLuong ?? 0) * Number(detail.donGia ?? 0),
    }));
  }, [selectedOrder]);

  const canShowActions = (status) =>
    Array.isArray(STATUS_TRANSITIONS[status]) &&
    STATUS_TRANSITIONS[status].length > 0;

  const formatCurrency = (value) => {
    if (value === null || value === undefined) return "0 VND";
    const number = Number(value);
    if (Number.isNaN(number)) return "0 VND";
    return new Intl.NumberFormat("vi-VN", {
      style: "currency",
      currency: "VND",
      maximumFractionDigits: 0,
    }).format(Math.round(number));
  };

  const formatDate = (value) => {
    if (!value) return "--";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "--";
    return date.toLocaleDateString("vi-VN");
  };

  const printInvoice = () => {
    window.print();
  };

  const getStatusLabel = (status) => STATUS_LABELS[status] || status || "--";

  return (
    <Layout>
      {message && <div className="message">{message}</div>}

      <div className="page-header">
        <div>
          <h1>Đơn bán hàng</h1>
          <p className="text-subtle">
            Giữ hàng trước, quản lý trạng thái và in hóa đơn bán hàng cho khách
          </p>
        </div>
      </div>

      <div className="form-card">
        <h3>Tạo đơn bán hàng</h3>
        <form onSubmit={handleCreateOrder}>
          <div className="form-grid">
            <select
              name="khachHangId"
              value={form.khachHangId}
              onChange={handleFormChange}
            >
              <option value="">Chọn khách hàng</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name}
                </option>
              ))}
            </select>
            <select
              name="khoId"
              value={form.khoId}
              onChange={handleFormChange}
            >
              <option value="">Chọn kho xuất</option>
              {warehouses.map((warehouse) => (
                <option key={warehouse.id} value={warehouse.id}>
                  {warehouse.name}
                </option>
              ))}
            </select>
            <input
              type="date"
              name="ngayGiaoDuKien"
              value={form.ngayGiaoDuKien}
              onChange={handleFormChange}
            />
            <textarea
              name="ghiChu"
              placeholder="Ghi chú"
              value={form.ghiChu}
              onChange={handleFormChange}
            />
          </div>

          <div className="po-item-list">
            {form.items.map((item, index) => (
              <div className="po-item-row" key={index}>
                <select
                  value={item.sanPhamId}
                  onChange={(e) =>
                    handleItemChange(index, "sanPhamId", e.target.value)
                  }
                >
                  <option value="">Chọn sản phẩm</option>
                  {products.map((product) => (
                    <option key={product.id} value={product.id}>
                      {product.name}
                    </option>
                  ))}
                </select>
                <input
                  type="number"
                  min="1"
                  value={item.soLuong}
                  onChange={(e) =>
                    handleItemChange(index, "soLuong", e.target.value)
                  }
                  placeholder="Số lượng"
                />
                <input
                  type="number"
                  min="0"
                  step="0.01"
                  value={item.donGia}
                  onChange={(e) =>
                    handleItemChange(index, "donGia", e.target.value)
                  }
                  placeholder="Đơn giá"
                />
                <button
                  type="button"
                  className="secondary"
                  onClick={() => removeItemRow(index)}
                >
                  Xóa
                </button>
              </div>
            ))}
            <button
              type="button"
              className="secondary"
              onClick={addItemRow}
            >
              Thêm sản phẩm
            </button>
          </div>

          <div className="form-actions">
            <button type="submit">Lưu đơn</button>
            <button type="button" className="secondary" onClick={resetForm}>
              Làm mới
            </button>
          </div>
        </form>
      </div>

      <div className="table-wrapper">
        <div className="table-title">Danh sách đơn bán hàng</div>
        <div className="po-filters">
          <label>
            Trạng thái
            <select
              value={statusFilter}
              onChange={(e) => {
                setPage(0);
                setStatusFilter(e.target.value);
              }}
            >
              <option value="">Tất cả</option>
              {Object.keys(STATUS_LABELS).map((status) => (
                <option key={status} value={status}>
                  {STATUS_LABELS[status]}
                </option>
              ))}
            </select>
          </label>
          <label>
            Khách hàng
            <select
              value={customerFilter}
              onChange={(e) => {
                setPage(0);
                setCustomerFilter(e.target.value);
              }}
            >
              <option value="">Tất cả</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name}
                </option>
              ))}
            </select>
          </label>
        </div>
        {loading ? (
          <p className="empty-text">Đang tải...</p>
        ) : (
          <table className="simple-table">
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Khách hàng</th>
                <th>Kho xuất</th>
                <th>Trạng thái</th>
                <th>Ngày giao dự kiến</th>
                <th>Tổng giá trị</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-text">
                    Chưa có đơn bán hàng
                  </td>
                </tr>
              )}
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.maDon}</td>
                  <td>{order.khachHang?.name || "--"}</td>
                  <td>{order.kho?.name || "--"}</td>
                  <td>
                    <span
                      className={`item-badge status-${order.trangThai?.toLowerCase()}`}
                    >
                      {getStatusLabel(order.trangThai)}
                    </span>
                  </td>
                  <td>{formatDate(order.ngayGiaoDuKien)}</td>
                  <td>{formatCurrency(order.tongTien)}</td>
                  <td className="table-actions">
                    <button onClick={() => openDetail(order.id)}>
                      Chi tiết
                    </button>
                    {canShowActions(order.trangThai) &&
                      STATUS_TRANSITIONS[order.trangThai].map((action) => (
                        <button
                          key={action.value}
                          className={
                            action.value === "DA_HUY" ? "danger" : ""
                          }
                          onClick={() =>
                            handleUpdateStatus(order.id, action.value)
                          }
                        >
                          {action.label}
                        </button>
                      ))}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
        {totalPages > 1 && (
          <div className="pagination-container">
            <button
              className="pagination-button"
              disabled={page === 0}
              onClick={() => setPage((prev) => Math.max(prev - 1, 0))}
            >
              Trước
            </button>
            <span>
              Trang {page + 1} / {totalPages}
            </span>
            <button
              className="pagination-button"
              disabled={page + 1 >= totalPages}
              onClick={() =>
                setPage((prev) => Math.min(prev + 1, totalPages - 1))
              }
            >
              Sau
            </button>
          </div>
        )}
      </div>

      {selectedOrder && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Chi tiết {selectedOrder.maDon}</h3>
              <div className="table-actions">
                <button className="secondary" onClick={printInvoice}>
                  In hóa đơn
                </button>
                <button onClick={closeDetail}>Đóng</button>
              </div>
            </div>
            <div className="modal-body">
              <div className="po-info-grid">
                <div className="po-info-card">
                  <span>Khách hàng</span>
                  <strong>{selectedOrder.khachHang?.name || "--"}</strong>
                </div>
                <div className="po-info-card">
                  <span>Trạng thái</span>
                  <strong>{getStatusLabel(selectedOrder.trangThai)}</strong>
                </div>
                <div className="po-info-card">
                  <span>Ngày giao dự kiến</span>
                  <strong>{formatDate(selectedOrder.ngayGiaoDuKien)}</strong>
                </div>
                <div className="po-info-card">
                  <span>Tổng giá trị</span>
                  <strong>{formatCurrency(selectedOrder.tongTien)}</strong>
                </div>
              </div>

              <div className="po-detail-section">
                <h4>Danh sách sản phẩm</h4>
                <div className="table-scroll">
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Thành tiền</th>
                        <th>Trạng thái</th>
                      </tr>
                    </thead>
                    <tbody>
                      {selectedOrder.chiTiets?.map((detail) => (
                        <tr key={detail.id}>
                          <td>{detail.sanPham?.name || "--"}</td>
                          <td>{detail.soLuong ?? 0}</td>
                          <td>{formatCurrency(detail.donGia)}</td>
                          <td>
                            {formatCurrency(
                              Number(detail.donGia || 0) *
                                Number(detail.soLuong || 0)
                            )}
                          </td>
                          <td>
                            {detail.trangThai
                              ? getStatusLabel(detail.trangThai)
                              : "--"}
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="po-detail-section">
                <h4>Hóa đơn</h4>
                <div className="invoice-card">
                  <div className="invoice-header">
                    <div>
                      <strong>Đơn: {selectedOrder.maDon}</strong>
                      <p>Ngày: {formatDate(selectedOrder.createdAt)}</p>
                    </div>
                    <div>
                      <p>Khách: {selectedOrder.khachHang?.name || "--"}</p>
                      <p>SDT: {selectedOrder.khachHang?.phone || "--"}</p>
                    </div>
                  </div>
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đơn giá</th>
                        <th>Tổng</th>
                      </tr>
                    </thead>
                    <tbody>
                      {invoiceLines.map((line, index) => (
                        <tr key={index}>
                          <td>{line.name}</td>
                          <td>{line.qty}</td>
                          <td>{formatCurrency(line.price)}</td>
                          <td>{formatCurrency(line.total)}</td>
                        </tr>
                      ))}
                      <tr>
                        <td colSpan="3" style={{ textAlign: "right" }}>
                          <strong>Tổng cộng</strong>
                        </td>
                        <td>
                          <strong>
                            {formatCurrency(selectedOrder.tongTien)}
                          </strong>
                        </td>
                      </tr>
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default DonBanHang;
