import React, { useCallback, useEffect, useMemo, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import PaginationComponent from "../component/PaginationComponent";

const KiemKe = () => {
  const [sanPhams, setSanPhams] = useState([]);
  const [khos, setKhos] = useState([]);
  const [productId, setProductId] = useState("");
  const [khoId, setKhoId] = useState("");
  const [systemQty, setSystemQty] = useState(0);
  const [actualQty, setActualQty] = useState("");
  const [reason, setReason] = useState("");
  const [note, setNote] = useState("");
  const [message, setMessage] = useState("");
  const [history, setHistory] = useState([]);
  const [historyPage, setHistoryPage] = useState(1);
  const [historyTotalPages, setHistoryTotalPages] = useState(1);
  const [loadingSnapshot, setLoadingSnapshot] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const pageSize = 10;

  const showMessage = useCallback((msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  useEffect(() => {
    const fetchInitial = async () => {
      try {
        const [productRes, khoRes] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getWarehouses(),
        ]);
        setSanPhams(productRes.sanPhams || []);
        setKhos(khoRes.khos || []);
      } catch (error) {
        showMessage(error.response?.data?.message || "Lỗi tải dữ liệu ban đầu");
      }
    };
    fetchInitial();
  }, [showMessage]);

  const fetchSnapshot = useCallback(async (sanPham, kho) => {
    setLoadingSnapshot(true);
    try {
      const res = await ApiService.getStockSnapshot(sanPham, kho || null);
      const qty = res.soLuongHienTai ?? res.sanPham?.stockQuantity ?? 0;
      setSystemQty(qty);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không lấy được tồn kho");
      setSystemQty(0);
    } finally {
      setLoadingSnapshot(false);
    }
  }, [showMessage]);

  const fetchHistory = useCallback(async (page) => {
    setLoadingHistory(true);
    try {
      const res = await ApiService.getStockTakeHistory({
        sanPhamId: productId || null,
        khoId: khoId || null,
        page: page - 1,
        size: pageSize,
      });
      setHistory(res.kiemKes || []);
      const total = res.totalPages || 1;
      setHistoryTotalPages(total);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không tải được lịch sử");
      setHistory([]);
      setHistoryTotalPages(1);
    } finally {
      setLoadingHistory(false);
    }
  }, [productId, khoId, pageSize, showMessage]);

  useEffect(() => {
    if (productId) {
      fetchSnapshot(productId, khoId);
    } else {
      setSystemQty(0);
    }
  }, [productId, khoId, fetchSnapshot]);

  useEffect(() => {
    fetchHistory(historyPage);
  }, [historyPage, fetchHistory]);

  const difference = useMemo(() => {
    if (actualQty === "" || isNaN(actualQty)) return 0;
    return parseInt(actualQty, 10) - systemQty;
  }, [actualQty, systemQty]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!productId) {
      showMessage("Vui lòng chọn sản phẩm");
      return;
    }
    if (actualQty === "" || Number(actualQty) < 0) {
      showMessage("Số lượng thực tế không hợp lệ");
      return;
    }
    try {
      await ApiService.performStockTake({
        sanPhamId: Number(productId),
        khoId: khoId ? Number(khoId) : null,
        soLuongThucTe: Number(actualQty),
        lyDo: reason,
        ghiChu: note,
      });
      showMessage("Đã ghi nhận kiểm kê");
      fetchSnapshot(productId, khoId);
      fetchHistory(1);
      setHistoryPage(1);
      setReason("");
      setNote("");
    } catch (error) {
      showMessage(error.response?.data?.message || "Kiểm kê thất bại");
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="stocktake-page">
        <div className="stocktake-card">
          <h1>Kiem ke kho</h1>
          <p className="text-subtle">
            Nhap so luong Thực tế va ghi nhan Chênh lệch so voi Hệ thống
          </p>

          <form onSubmit={handleSubmit} className="stocktake-form">
            <div className="form-group">
              <label>Sản phẩm</label>
              <select
                value={productId}
                onChange={(e) => {
                  setProductId(e.target.value);
                  setHistoryPage(1);
                }}
                required
              >
                <option value="">-- Chon Sản phẩm --</option>
                {sanPhams.map((sp) => (
                  <option key={sp.id} value={sp.id}>
                    {sp.name} ({sp.sku})
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>Kho (tuy chon)</label>
              <select
                value={khoId}
                onChange={(e) => {
                  setKhoId(e.target.value);
                  setHistoryPage(1);
                }}
              >
                <option value="">Tất cả kho</option>
                {khos.map((kho) => (
                  <option key={kho.id} value={kho.id}>
                    {kho.name}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group inline">
              <div>
                <label>So luong Hệ thống</label>
                <input type="number" value={systemQty} readOnly />
              </div>
              <div>
                <label>So luong Thực tế</label>
                <input
                  type="number"
                  min="0"
                  value={actualQty}
                  onChange={(e) => setActualQty(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="difference-badge">
              {loadingSnapshot ? (
                <span>Đang tải tồn kho...</span>
              ) : (
                <>
                  <span>Chênh lệch: </span>
                  <strong className={difference === 0 ? "" : difference > 0 ? "positive" : "negative"}>
                    {difference}
                  </strong>
                </>
              )}
            </div>

            <div className="form-group">
              <label>Lý do</label>
              <input
                type="text"
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="Nhập lý do điều chỉnh"
              />
            </div>

            <div className="form-group">
              <label>Ghi chú</label>
              <textarea value={note} onChange={(e) => setNote(e.target.value)} />
            </div>

            <button type="submit">Ghi nhận kiểm kê</button>
          </form>
        </div>

        <div className="stocktake-card">
          <div className="section-header">
            <div>
              <h2>Lịch sử kiểm kê</h2>
              <p className="text-subtle">
                Theo dõi các lần điều chỉnh mức tồn kho gần đây
              </p>
            </div>
          </div>
          {loadingHistory ? (
            <p className="empty-text">Đang tải lịch sử...</p>
          ) : history.length === 0 ? (
            <p className="empty-text">Chưa có dữ liệu kiểm kê</p>
          ) : (
            <div className="history-table-wrapper">
              <table className="simple-table">
                <thead>
                  <tr>
                    <th>Sản phẩm</th>
                    <th>Kho</th>
                    <th>Hệ thống</th>
                    <th>Thực tế</th>
                    <th>Chênh lệch</th>
                    <th>Lý do</th>
                    <th>Thời gian</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((item) => (
                    <tr key={item.id}>
                      <td>{item.sanPham?.name}</td>
                      <td>{item.tenKho || "Tất cả"}</td>
                      <td>{item.soLuongHeThong}</td>
                      <td>{item.soLuongThucTe}</td>
                      <td
                        className={
                          item.chenhlech > 0
                            ? "text-positive"
                            : item.chenhlech < 0
                            ? "text-negative"
                            : ""
                        }
                      >
                        {item.chenhlech}
                      </td>
                      <td>{item.lyDo || "--"}</td>
                      <td>
                        {item.thoiGian
                          ? new Date(item.thoiGian).toLocaleString()
                          : "--"}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          {historyTotalPages > 1 && (
            <PaginationComponent
              currentPage={historyPage}
              totalPages={historyTotalPages}
              onPageChange={(page) => setHistoryPage(page)}
            />
          )}
        </div>
      </div>
    </Layout>
  );
};

export default KiemKe;

