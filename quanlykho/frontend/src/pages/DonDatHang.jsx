import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const STATUS_LABELS = {
  NHAP: "Nháp",
  CHO_DUYET: "Chờ duyệt",
  DA_DUYET: "Đã duyệt",
  DANG_GIAO: "Đang giao",
  HOAN_THANH: "Hoàn thành",
  DA_HUY: "Đã hủy",
};

const STATUS_TRANSITIONS = {
  NHAP: [
    { value: "CHO_DUYET", label: "Gửi duyệt" },
    { value: "DA_DUYET", label: "Duyệt nhanh" },
    { value: "DA_HUY", label: "Hủy đơn" },
  ],
  CHO_DUYET: [
    { value: "DA_DUYET", label: "Duyệt đơn" },
    { value: "DA_HUY", label: "Hủy đơn" },
  ],
  DA_DUYET: [
    { value: "DANG_GIAO", label: "Đang giao" },
    { value: "DA_HUY", label: "Hủy đơn" },
  ],
  DANG_GIAO: [
    { value: "HOAN_THANH", label: "Hoàn thành" },
    { value: "DA_HUY", label: "Hủy đơn" },
  ],
};

const itemTemplate = { sanPhamId: "", soLuong: 1, donGia: "" };
const receiveTemplate = {
  chiTietId: "",
  soLuongNhan: "",
  khoId: "",
  soLo: "",
  ngayNhap: "",
  ghiChu: "",
};

const currencyFormatter = new Intl.NumberFormat("vi-VN", {
  style: "currency",
  currency: "VND",
  maximumFractionDigits: 0,
});

const DonDatHang = () => {
  const [orders, setOrders] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [statusFilter, setStatusFilter] = useState("");
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [suppliers, setSuppliers] = useState([]);
  const [products, setProducts] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [form, setForm] = useState({
    nhaCungCapId: "",
    ngayDuKien: "",
    ghiChu: "",
    items: [{ ...itemTemplate }],
  });
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [receiveForm, setReceiveForm] = useState(receiveTemplate);
  const messageTimerRef = useRef(null);

  const showMessage = useCallback((text) => {
    if (messageTimerRef.current) {
      clearTimeout(messageTimerRef.current);
    }
    setMessage(text);
    messageTimerRef.current = setTimeout(() => setMessage(""), 4000);
  }, []);

  useEffect(() => {
    return () => {
      if (messageTimerRef.current) {
        clearTimeout(messageTimerRef.current);
      }
    };
  }, []);

  const loadOrders = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.getPurchaseOrders({
        page,
        size: 10,
        status: statusFilter || undefined,
      });
      if (response.status === 200) {
        setOrders(response.donDatHangs || []);
        setTotalPages(response.totalPages || 0);
      } else {
        showMessage(response.message || "Không thể tải danh sách đơn đặt hàng");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tải danh sách đơn đặt hàng: " +
            (error.message || error.toString())
      );
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, showMessage]);

  useEffect(() => {
    loadOrders();
  }, [loadOrders]);

  useEffect(() => {
    const loadReferences = async () => {
      try {
        const [supplierRes, productRes, warehouseRes] = await Promise.all([
          ApiService.getAllSuppliers(),
          ApiService.getAllProducts(),
          ApiService.getWarehouses(),
        ]);
        if (supplierRes.status === 200) {
          setSuppliers(supplierRes.nhaCungCaps || []);
        }
        if (productRes.status === 200) {
          setProducts(productRes.sanPhams || []);
        }
        if (warehouseRes.status === 200) {
          setWarehouses(warehouseRes.khos || []);
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tải dữ liệu tham chiếu: " +
              (error.message || error.toString())
        );
      }
    };
    loadReferences();
  }, [showMessage]);

  const remainingDetails = useMemo(() => {
    if (!selectedOrder?.chiTiets) return [];
    return selectedOrder.chiTiets.map((detail) => {
      const ordered = Number(detail.soLuong ?? 0);
      const received = Number(detail.soLuongDaNhan ?? 0);
      return {
        ...detail,
        remaining: Math.max(ordered - received, 0),
      };
    });
  }, [selectedOrder]);

  useEffect(() => {
    if (!selectedOrder) {
      setReceiveForm(receiveTemplate);
      return;
    }
    const firstPending = remainingDetails.find(
      (detail) => detail.remaining > 0
    );
    setReceiveForm((prev) => ({
      ...receiveTemplate,
      chiTietId: firstPending ? String(firstPending.id) : prev.chiTietId,
    }));
  }, [selectedOrder, remainingDetails]);

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
      items: [...prev.items, { ...itemTemplate }],
    }));
  };

  const removeItemRow = (index) => {
    setForm((prev) => {
      if (prev.items.length === 1) {
        return prev;
      }
      const filtered = prev.items.filter((_, idx) => idx !== index);
      return { ...prev, items: filtered };
    });
  };

  const resetForm = () => {
    setForm({
      nhaCungCapId: "",
      ngayDuKien: "",
      ghiChu: "",
      items: [{ ...itemTemplate }],
    });
  };

  const handleCreateOrder = async (event) => {
    event.preventDefault();
    if (!form.nhaCungCapId) {
      showMessage("Vui lòng chọn nhà cung cấp");
      return;
    }
    const validItems = form.items
      .filter((item) => item.sanPhamId && item.soLuong)
      .map((item) => ({
        sanPhamId: Number(item.sanPhamId),
        soLuong: Number(item.soLuong),
        donGia: item.donGia ? Number(item.donGia) : null,
      }));
    if (validItems.length === 0) {
      showMessage("Vui lòng thêm ít nhất một sản phẩm");
      return;
    }
    const payload = {
      nhaCungCapId: Number(form.nhaCungCapId),
      ngayDuKien: form.ngayDuKien || null,
      ghiChu: form.ghiChu || "",
      items: validItems,
    };
    try {
      const response = await ApiService.createPurchaseOrder(payload);
      showMessage(response.message || "Tạo đơn đặt hàng thành công");
      resetForm();
      loadOrders();
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tạo đơn đặt hàng: " + (error.message || error.toString())
      );
    }
  };

  const openDetail = async (orderId) => {
    try {
      const response = await ApiService.getPurchaseOrderDetail(orderId);
      if (response.status === 200) {
        setSelectedOrder(response.donDatHang);
      } else {
        showMessage(response.message || "Không thể tải chi tiết đơn");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tải chi tiết đơn: " + (error.message || error.toString())
      );
    }
  };

  const closeDetail = () => {
    setSelectedOrder(null);
    setReceiveForm(receiveTemplate);
  };

  const handleUpdateStatus = async (orderId, nextStatus) => {
    try {
      const response = await ApiService.updatePurchaseOrderStatus(
        orderId,
        nextStatus
      );
      showMessage(response.message || "Cập nhật trạng thái thành công");
      loadOrders();
      if (selectedOrder?.id === orderId) {
        openDetail(orderId);
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể cập nhật trạng thái: " +
            (error.message || error.toString())
      );
    }
  };

  const handleReceiveChange = (event) => {
    const { name, value } = event.target;
    setReceiveForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const handleReceiveSubmit = async (event) => {
    event.preventDefault();
    if (!selectedOrder) {
      return;
    }
    if (!receiveForm.chiTietId || !receiveForm.soLuongNhan) {
      showMessage("Vui lòng chọn chi tiết và nhập số lượng nhận");
      return;
    }
    const detail = remainingDetails.find(
      (item) => String(item.id) === String(receiveForm.chiTietId)
    );
    if (!detail) {
      showMessage("Chi tiết đơn không hợp lệ");
      return;
    }
    if (Number(receiveForm.soLuongNhan) > detail.remaining) {
      showMessage("Số lượng nhận vượt quá số còn lại");
      return;
    }
    const payload = {
      ghiChu: receiveForm.ghiChu || "",
      items: [
        {
          chiTietId: Number(receiveForm.chiTietId),
          soLuongNhan: Number(receiveForm.soLuongNhan),
          khoId: receiveForm.khoId ? Number(receiveForm.khoId) : null,
          soLo: receiveForm.soLo || null,
          ngayNhap: receiveForm.ngayNhap || null,
        },
      ],
    };
    try {
      const response = await ApiService.receivePurchaseOrder(
        selectedOrder.id,
        payload
      );
      showMessage(response.message || "Cập nhật nhận hàng thành công");
      setReceiveForm(receiveTemplate);
      openDetail(selectedOrder.id);
      loadOrders();
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể cập nhật nhận hàng: " +
            (error.message || error.toString())
      );
    }
  };

  const canShowActions = (status) =>
    Array.isArray(STATUS_TRANSITIONS[status]) &&
    STATUS_TRANSITIONS[status].length > 0;

  const formatCurrency = (value) => {
    if (value === null || value === undefined) {
      return currencyFormatter.format(0);
    }
    const numeric = Number(value);
    if (Number.isNaN(numeric)) {
      return currencyFormatter.format(0);
    }
    return currencyFormatter.format(Math.round(numeric));
  };

  const formatDate = (value) => {
    if (!value) return "--";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return "--";
    return date.toLocaleDateString("vi-VN");
  };

  const renderStatusLabel = (status) =>
    STATUS_LABELS[status] || status || "--";

  return (
    <Layout>
      {message && <div className="message">{message}</div>}

      <div className="page-header">
        <div>
          <h1>Đơn đặt hàng</h1>
          <p className="text-subtle">
            Tạo đơn đặt hàng, theo dõi trạng thái duyệt và cập nhật nhận hàng.
          </p>
        </div>
      </div>

      <div className="form-card">
        <h3>Tạo đơn đặt hàng</h3>
        <form onSubmit={handleCreateOrder}>
          <div className="form-grid">
            <div>
              <label>Nhà cung cấp</label>
              <select
                name="nhaCungCapId"
                value={form.nhaCungCapId}
                onChange={handleFormChange}
                required
              >
                <option value="">-- Chọn nhà cung cấp --</option>
                {suppliers.map((supplier) => (
                  <option key={supplier.id} value={supplier.id}>
                    {supplier.name}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>Ngày dự kiến nhận</label>
              <input
                type="date"
                name="ngayDuKien"
                value={form.ngayDuKien}
                onChange={handleFormChange}
              />
            </div>
            <div>
              <label>Ghi chú</label>
              <textarea
                name="ghiChu"
                placeholder="Nhập hướng dẫn cho nhà cung cấp..."
                value={form.ghiChu}
                onChange={handleFormChange}
              />
            </div>
          </div>

          <div className="po-item-list">
            {form.items.map((item, index) => (
              <div className="po-item-row" key={`po-item-${index}`}>
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
            <button type="button" className="secondary" onClick={addItemRow}>
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
        <div className="table-title">Danh sách đơn đặt hàng</div>
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
        </div>
        {loading ? (
          <p className="empty-text">Đang tải...</p>
        ) : (
          <table className="simple-table">
            <thead>
              <tr>
                <th>Mã đơn</th>
                <th>Nhà cung cấp</th>
                <th>Trạng thái</th>
                <th>Ngày dự kiến</th>
                <th>Tổng số lượng</th>
                <th>Tổng giá trị</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {orders.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-text">
                    Chưa có đơn đặt hàng nào
                  </td>
                </tr>
              )}
              {orders.map((order) => (
                <tr key={order.id}>
                  <td>{order.maDon}</td>
                  <td>{order.nhaCungCap?.name || "--"}</td>
                  <td>
                    <span
                      className={`item-badge status-${(
                        order.trangThai || ""
                      ).toLowerCase()}`}
                    >
                      {renderStatusLabel(order.trangThai)}
                    </span>
                  </td>
                  <td>{formatDate(order.ngayDuKien)}</td>
                  <td>{order.tongSoLuong ?? 0}</td>
                  <td>{formatCurrency(order.tongTien)}</td>
                  <td className="table-actions">
                    <button type="button" onClick={() => openDetail(order.id)}>
                      Chi tiết
                    </button>
                    {canShowActions(order.trangThai) &&
                      STATUS_TRANSITIONS[order.trangThai].map((action) => (
                        <button
                          type="button"
                          key={action.value}
                          className={
                            action.value === "DA_HUY" ? "danger" : undefined
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
              Trang {page + 1}/{totalPages}
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
              <button type="button" className="secondary" onClick={closeDetail}>
                Đóng
              </button>
            </div>
            <div className="modal-body">
              <div className="po-info-grid">
                <div className="po-info-card">
                  <span>Trạng thái</span>
                  <strong>{renderStatusLabel(selectedOrder.trangThai)}</strong>
                </div>
                <div className="po-info-card">
                  <span>Nhà cung cấp</span>
                  <strong>{selectedOrder.nhaCungCap?.name || "--"}</strong>
                </div>
                <div className="po-info-card">
                  <span>Ngày dự kiến</span>
                  <strong>{formatDate(selectedOrder.ngayDuKien)}</strong>
                </div>
                <div className="po-info-card">
                  <span>Tổng giá trị</span>
                  <strong>{formatCurrency(selectedOrder.tongTien)}</strong>
                </div>
                <div className="po-info-card">
                  <span>Số lượng đã nhận</span>
                  <strong>{selectedOrder.tongSoLuongDaNhan ?? 0}</strong>
                </div>
              </div>

              <div className="po-detail-section">
                <h4>Chi tiết sản phẩm</h4>
                <div className="table-scroll">
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>Sản phẩm</th>
                        <th>Số lượng</th>
                        <th>Đã nhận</th>
                        <th>Còn lại</th>
                        <th>Đơn giá</th>
                        <th>Trạng thái</th>
                      </tr>
                    </thead>
                    <tbody>
                      {remainingDetails.map((item) => (
                        <tr key={item.id}>
                          <td>{item.sanPham?.name || "--"}</td>
                          <td>{item.soLuong ?? 0}</td>
                          <td>{item.soLuongDaNhan ?? 0}</td>
                          <td>{item.remaining}</td>
                          <td>{formatCurrency(item.donGia)}</td>
                          <td>{renderStatusLabel(item.trangThai)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>

              <div className="po-detail-section">
                <h4>Nhận hàng</h4>
                <form
                  onSubmit={handleReceiveSubmit}
                  className="po-receive-form"
                >
                  <select
                    name="chiTietId"
                    value={receiveForm.chiTietId}
                    onChange={handleReceiveChange}
                  >
                    <option value="">Chọn sản phẩm</option>
                    {remainingDetails.map((detail) => (
                      <option
                        key={detail.id}
                        value={detail.id}
                        disabled={detail.remaining === 0}
                      >
                        {detail.sanPham?.name || "--"} (còn {detail.remaining})
                      </option>
                    ))}
                  </select>
                  <input
                    type="number"
                    min="1"
                    name="soLuongNhan"
                    placeholder="Số lượng nhận"
                    value={receiveForm.soLuongNhan}
                    onChange={handleReceiveChange}
                  />
                  <select
                    name="khoId"
                    value={receiveForm.khoId}
                    onChange={handleReceiveChange}
                  >
                    <option value="">Chọn kho</option>
                    {warehouses.map((warehouse) => (
                      <option key={warehouse.id} value={warehouse.id}>
                        {warehouse.name}
                      </option>
                    ))}
                  </select>
                  <input
                    type="text"
                    name="soLo"
                    placeholder="Số lô"
                    value={receiveForm.soLo}
                    onChange={handleReceiveChange}
                  />
                  <input
                    type="date"
                    name="ngayNhap"
                    value={receiveForm.ngayNhap}
                    onChange={handleReceiveChange}
                  />
                  <textarea
                    name="ghiChu"
                    placeholder="Ghi chú"
                    value={receiveForm.ghiChu}
                    onChange={handleReceiveChange}
                  />
                  <div className="po-receive-actions">
                    <button type="submit">Cập nhật nhận hàng</button>
                    <button
                      type="button"
                      className="secondary"
                      onClick={() => setReceiveForm(receiveTemplate)}
                    >
                      Làm mới
                    </button>
                  </div>
                </form>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

export default DonDatHang;
