import React, { useCallback, useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import PaginationComponent from "../component/PaginationComponent";

const QuanLyKho = () => {
  const [warehouses, setWarehouses] = useState([]);
  const [sanPhams, setSanPhams] = useState([]);
  const [tonInfos, setTonInfos] = useState([]);
  const [tonFilterProduct, setTonFilterProduct] = useState("");
  const [tonFilterWarehouse, setTonFilterWarehouse] = useState("");
  const [report, setReport] = useState([]);
  const [message, setMessage] = useState("");

  const [newWarehouse, setNewWarehouse] = useState({
    name: "",
    address: "",
    manager: "",
  });
  const [transferData, setTransferData] = useState({
    productId: "",
    fromWarehouseId: "",
    toWarehouseId: "",
    quantity: "",
  });

  const [loadingTon, setLoadingTon] = useState(false);
  const [loadingReport, setLoadingReport] = useState(false);
  const [tonPage, setTonPage] = useState(1);
  const tonPerPage = 8;

  const showMessage = useCallback((msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadTonKho = useCallback(async () => {
    setLoadingTon(true);
    try {
      const res = await ApiService.getWarehouseStock({
        sanPhamId: tonFilterProduct || null,
        khoId: tonFilterWarehouse || null,
      });
      setTonInfos(res.tonKhos || []);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không tải được tồn kho");
      setTonInfos([]);
    } finally {
      setLoadingTon(false);
    }
  }, [showMessage, tonFilterProduct, tonFilterWarehouse]);

  const loadReport = useCallback(async () => {
    setLoadingReport(true);
    try {
      const res = await ApiService.getWarehouseReport();
      setReport(res.thongKeKhos || []);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không tải được thống kê");
      setReport([]);
    } finally {
      setLoadingReport(false);
    }
  }, [showMessage]);

  useEffect(() => {
    const fetchInitial = async () => {
      try {
        const [productRes, warehouseRes] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getWarehouses(),
        ]);
        setSanPhams(productRes.sanPhams || []);
        setWarehouses(warehouseRes.khos || []);
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không tải được dữ liệu ban đầu"
        );
      }
    };

    fetchInitial();
  }, [showMessage]);

  useEffect(() => {
    loadTonKho();
  }, [loadTonKho]);

  useEffect(() => {
    loadReport();
  }, [loadReport]);

  useEffect(() => {
    setTonPage(1);
  }, [tonFilterProduct, tonFilterWarehouse]);

  const handleCreateWarehouse = async (e) => {
    e.preventDefault();
    if (!newWarehouse.name) {
      showMessage("Tên kho là bắt buộc");
      return;
    }
    try {
      await ApiService.createWarehouse(newWarehouse);
      showMessage("Đã tạo kho mới");
      setNewWarehouse({ name: "", address: "", manager: "" });
      const warehouseRes = await ApiService.getWarehouses();
      setWarehouses(warehouseRes.khos || []);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không tạo được kho");
    }
  };

  const handleTransfer = async (e) => {
    e.preventDefault();
    const { productId, fromWarehouseId, toWarehouseId, quantity } = transferData;
    if (!productId || !fromWarehouseId || !toWarehouseId || !quantity) {
      showMessage("Vui lòng nhập đầy đủ thông tin chuyển kho");
      return;
    }
    if (fromWarehouseId === toWarehouseId) {
      showMessage("Kho nguồn và đích phải khác nhau");
      return;
    }
    try {
      await ApiService.transferBetweenWarehouses({
        productId,
        fromWarehouseId,
        toWarehouseId,
        quantity: Number(quantity),
      });
      showMessage("Da Chuyển kho thanh cong");
      setTransferData({
        productId: "",
        fromWarehouseId: "",
        toWarehouseId: "",
        quantity: "",
      });
      loadTonKho();
      loadReport();
    } catch (error) {
      showMessage(error.response?.data?.message || "Chuyển kho that bai");
    }
  };

  const displayedTon = tonInfos.slice(
    (tonPage - 1) * tonPerPage,
    tonPage * tonPerPage
  );

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="warehouse-page">
        <div className="warehouse-card">
          <h1>Quan ly kho</h1>
          <p className="text-subtle">
            Them kho moi va quan sat trang thai ton kho theo tung vi tri.
          </p>
          <form className="warehouse-form" onSubmit={handleCreateWarehouse}>
            <div className="form-group">
              <label>Ten kho</label>
              <input
                type="text"
                value={newWarehouse.name}
                onChange={(e) =>
                  setNewWarehouse((prev) => ({ ...prev, name: e.target.value }))
                }
                required
              />
            </div>
            <div className="form-group">
              <label>Dia chi</label>
              <input
                type="text"
                value={newWarehouse.address}
                onChange={(e) =>
                  setNewWarehouse((prev) => ({
                    ...prev,
                    address: e.target.value,
                  }))
                }
              />
            </div>
            <div className="form-group">
              <label>Nguoi quan ly</label>
              <input
                type="text"
                value={newWarehouse.manager}
                onChange={(e) =>
                  setNewWarehouse((prev) => ({
                    ...prev,
                    manager: e.target.value,
                  }))
                }
              />
            </div>
            <button type="submit">Them kho</button>
          </form>

          <table className="simple-table">
            <thead>
              <tr>
                <th>Ten kho</th>
                <th>Dia chi</th>
                <th>Nguoi quan ly</th>
              </tr>
            </thead>
            <tbody>
              {warehouses.length === 0 ? (
                <tr>
                  <td colSpan="3" className="empty-text">
                    Chua co kho nao
                  </td>
                </tr>
              ) : (
                warehouses.map((kho) => (
                  <tr key={kho.id}>
                    <td>{kho.name}</td>
                    <td>{kho.address || "--"}</td>
                    <td>{kho.manager || "--"}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        <div className="warehouse-card">
          <div className="section-header">
            <div>
              <h2>Ton kho theo kho</h2>
              <p className="text-subtle">
                Loc theo Sản phẩm hoac kho de xem Số lượng ton.
              </p>
            </div>
          </div>
          <div className="filter-row">
            <select
              value={tonFilterProduct}
              onChange={(e) => setTonFilterProduct(e.target.value)}
            >
              <option value="">Tat ca Sản phẩm</option>
              {sanPhams.map((sp) => (
                <option key={sp.id} value={sp.id}>
                  {sp.name}
                </option>
              ))}
            </select>
            <select
              value={tonFilterWarehouse}
              onChange={(e) => setTonFilterWarehouse(e.target.value)}
            >
              <option value="">Tat ca kho</option>
              {warehouses.map((kho) => (
                <option key={kho.id} value={kho.id}>
                  {kho.name}
                </option>
              ))}
            </select>
            <button type="button" onClick={loadTonKho}>
              Làm mới
            </button>
          </div>

          {loadingTon ? (
            <p className="empty-text">Dang tai ton kho...</p>
          ) : displayedTon.length === 0 ? (
            <p className="empty-text">Chua co du lieu ton kho</p>
          ) : (
            <>
              <table className="simple-table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Kho</th>
                    <th>Số lượng</th>
                  </tr>
                </thead>
                <tbody>
                  {displayedTon.map((item, idx) => (
                    <tr key={`${item.sanPhamId}-${item.khoId}-${idx}`}>
                      <td>{item.tenSanPham || "--"}</td>
                      <td>{item.sku}</td>
                      <td>{item.tenKho}</td>
                      <td>{item.soLuong ?? 0}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {tonInfos.length > tonPerPage && (
                <PaginationComponent
                  currentPage={tonPage}
                  totalPages={Math.ceil(tonInfos.length / tonPerPage) || 1}
                  onPageChange={setTonPage}
                />
              )}
            </>
          )}
        </div>

        <div className="warehouse-card">
          <div className="section-header">
            <h2>Chuyển kho</h2>
          </div>
          <form className="transfer-form" onSubmit={handleTransfer}>
            <div className="form-group">
              <label>Sản phẩm</label>
              <select
                value={transferData.productId}
                onChange={(e) =>
                  setTransferData((prev) => ({
                    ...prev,
                    productId: e.target.value,
                  }))
                }
                required
              >
                <option value="">-- Chon Sản phẩm --</option>
                {sanPhams.map((sp) => (
                  <option key={sp.id} value={sp.id}>
                    {sp.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Kho nguồn</label>
              <select
                value={transferData.fromWarehouseId}
                onChange={(e) =>
                  setTransferData((prev) => ({
                    ...prev,
                    fromWarehouseId: e.target.value,
                  }))
                }
                required
              >
                <option value="">-- Chọn kho --</option>
                {warehouses.map((kho) => (
                  <option key={kho.id} value={kho.id}>
                    {kho.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Kho đích</label>
              <select
                value={transferData.toWarehouseId}
                onChange={(e) =>
                  setTransferData((prev) => ({
                    ...prev,
                    toWarehouseId: e.target.value,
                  }))
                }
                required
              >
                <option value="">-- Chọn kho --</option>
                {warehouses.map((kho) => (
                  <option key={kho.id} value={kho.id}>
                    {kho.name}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Số lượng</label>
              <input
                type="number"
                min="1"
                value={transferData.quantity}
                onChange={(e) =>
                  setTransferData((prev) => ({
                    ...prev,
                    quantity: e.target.value,
                  }))
                }
                required
              />
            </div>
            <button type="submit">Chuyển kho</button>
          </form>
        </div>

        <div className="warehouse-card">
          <div className="section-header">
            <h2>Thống kê theo kho</h2>
            <button type="button" onClick={loadReport}>
              Làm mới
            </button>
          </div>
          {loadingReport ? (
            <p className="empty-text">Đang tải thống kê...</p>
          ) : report.length === 0 ? (
            <p className="empty-text">Chưa có thống kê kho</p>
          ) : (
            <table className="simple-table">
              <thead>
                <tr>
                  <th>Kho</th>
                  <th>Tong Số lượng</th>
                  <th>So Sản phẩm dang ton</th>
                </tr>
              </thead>
              <tbody>
                {report.map((item) => (
                  <tr key={item.khoId}>
                    <td>{item.tenKho}</td>
                    <td>{item.tongSoLuong ?? 0}</td>
                    <td>{item.soSanPham ?? 0}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default QuanLyKho;

