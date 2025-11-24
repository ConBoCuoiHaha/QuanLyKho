import React, { useCallback, useEffect, useMemo, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const MODULE_OPTIONS = [
  "SanPham",
  "KhachHang",
  "DonDatHang",
  "DonBanHang",
  "ĐóngiaoDich",
  "Kho",
];

const ACTION_OPTIONS = [
  "TAO",
  "CAP_NHAT",
  "XOA",
  "CAP_NHAT_Trang_THAI",
  "CAP_NHAT_NHAN_HANG",
  "BAN_HANG",
  "NHAP_KHO",
  "TRA_KHACH",
];

const initialFilters = {
  module: "",
  hanhĐóng: "",
  userId: "",
  doiTuongLoai: "",
  doiTuongId: "",
  tuNgay: "",
  denNgay: "",
};

const NhatKy = () => {
  const [filters, setFilters] = useState(initialFilters);
  const [logs, setLogs] = useState([]);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [selectedLog, setSelectedLog] = useState(null);

  const showMessage = useCallback((text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadLogs = useCallback(async () => {
    setLoading(true);
    try {
      const response = await ApiService.getAuditLogs({
        page,
        size: 10,
        ...Object.fromEntries(
          Object.entries(filters).filter(
            ([, value]) => value !== "" && value !== null
          )
        ),
      });
      if (response.status === 200) {
        setLogs(response.nhatKys || []);
        setTotalPages(response.totalPages || 0);
      } else {
        showMessage(response.message || "Không thể tải nhật ký");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể tải nhật ký: " + error
      );
    } finally {
      setLoading(false);
    }
  }, [filters, page, showMessage]);

  useEffect(() => {
    loadLogs();
  }, [loadLogs]);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const res = await ApiService.getAllUsers();
        if (res.status === 200) {
          setUsers(res.users || []);
        }
      } catch (error) {
        // ignore silent
      }
    };
    fetchUsers();
  }, []);

  const handleFilterChange = (event) => {
    const { name, value } = event.target;
    setFilters((Trước) => ({
      ...Trước,
      [name]: value,
    }));
  };

  const applyFilters = (event) => {
    event.TrướcentDefault();
    setPage(0);
    loadLogs();
  };

  const resetFilters = () => {
    setFilters(initialFilters);
    setPage(0);
  };

  const openDetail = (log) => {
    setSelectedLog(log);
  };

  const closeDetail = () => {
    setSelectedLog(null);
  };

  const formatDateTime = (value) => {
    if (!value) return "--";
    const date = new Date(value);
    if (Number.isNaN(date.getTime())) return value;
    return date.toLocaleString();
  };

  const parsedOldData = useMemo(() => parseJson(selectedLog?.duLieuCu), [
    selectedLog,
  ]);
  const parsedNewData = useMemo(() => parseJson(selectedLog?.duLieuMoi), [
    selectedLog,
  ]);

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="page-header">
        <div>
          <h1>Nhat ky thay doi</h1>
          <p className="text-subtle">
            Theo doi ai thay doi gi va khi nao trong he thong
          </p>
        </div>
      </div>

      <div className="form-card">
        <h3>Bo loc</h3>
        <form onSubmit={applyFilters}>
          <div className="filter-grid">
            <select
              name="module"
              value={filters.module}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả module</option>
              {MODULE_OPTIONS.map((item) => (
                <option key={item} value={item}>
                  {item}
                </option>
              ))}
            </select>
            <select
              name="hanhĐóng"
              value={filters.hanhĐóng}
              onChange={handleFilterChange}
            >
              <option value="">Tất cả Hành động</option>
              {ACTION_OPTIONS.map((action) => (
                <option key={action} value={action}>
                  {action}
                </option>
              ))}
            </select>
            <select
              name="userId"
              value={filters.userId}
              onChange={handleFilterChange}
            >
              <option value="">Người thực hiện</option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
            <input
              type="text"
              name="doiTuongLoai"
              value={filters.doiTuongLoai}
              placeholder="Loai Đối tượng"
              onChange={handleFilterChange}
            />
            <input
              type="number"
              name="doiTuongId"
              value={filters.doiTuongId}
              placeholder="ID Đối tượng"
              onChange={handleFilterChange}
            />
            <input
              type="date"
              name="tuNgay"
              value={filters.tuNgay}
              onChange={handleFilterChange}
            />
            <input
              type="date"
              name="denNgay"
              value={filters.denNgay}
              onChange={handleFilterChange}
            />
          </div>
          <div className="form-actions">
            <button type="submit">Ap dung</button>
            <button type="button" className="secondary" onClick={resetFilters}>
              Dat lai
            </button>
          </div>
        </form>
      </div>

      <div className="table-wrapper">
        <div className="table-title">Danh sách nhật ký</div>
        {loading ? (
          <p className="empty-text">Đang tải...</p>
        ) : (
          <table className="simple-table">
            <thead>
              <tr>
                <th>Thời gian</th>
                <th>Module</th>
                <th>Hành động</th>
                <th>Mô tả</th>
                <th>Người thực hiện</th>
                <th>Đối tượng</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {logs.length === 0 && (
                <tr>
                  <td colSpan="7" className="empty-text">
                    Chưa có nhật ký
                  </td>
                </tr>
              )}
              {logs.map((log) => (
                <tr key={log.id}>
                  <td>{formatDateTime(log.createdAt)}</td>
                  <td>{log.module}</td>
                  <td>{log.hanhĐóng}</td>
                  <td>{log.moTa || "--"}</td>
                  <td>{log.nguoiThucHien?.name || "--"}</td>
                  <td>
                    {log.doiTuongLoai || "--"} #{log.doiTuongId ?? "--"}
                  </td>
                  <td className="table-actions">
                    <button onClick={() => openDetail(log)}>Chi tiết</button>
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
              onClick={() => setPage((Trước) => Math.max(Trước - 1, 0))}
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
                setPage((Trước) => Math.min(Trước + 1, totalPages - 1))
              }
            >
              Sau
            </button>
          </div>
        )}
      </div>

      {selectedLog && (
        <div className="modal-backdrop">
          <div className="modal-content">
            <div className="modal-header">
              <h3>Chi tiết nhat ky #{selectedLog.id}</h3>
              <button onClick={closeDetail}>Đóng</button>
            </div>
            <div className="modal-body">
              <p>Module: {selectedLog.module}</p>
              <p>Hành động: {selectedLog.hanhĐóng}</p>
              <p>Mô tả: {selectedLog.moTa || "--"}</p>
              <p>
                Người thực hiện:{" "}
                {selectedLog.nguoiThucHien?.name ||
                  selectedLog.nguoiThucHien?.email ||
                  "--"}
              </p>
              <p>
                Đối tượng: {selectedLog.doiTuongLoai || "--"} #
                {selectedLog.doiTuongId ?? "--"}
              </p>
              <p>Thời gian: {formatDateTime(selectedLog.createdAt)}</p>
              <div className="po-detail-section">
                <h4>Dữ liệu cũ</h4>
                <pre className="json-block">
                  {parsedOldData
                    ? JSON.stringify(parsedOldData, null, 2)
                    : "Không có"}
                </pre>
              </div>
              <div className="po-detail-section">
                <h4>Dữ liệu mới</h4>
                <pre className="json-block">
                  {parsedNewData
                    ? JSON.stringify(parsedNewData, null, 2)
                    : "Không có"}
                </pre>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
};

function parseJson(value) {
  if (!value) return null;
  try {
    return typeof value === "string" ? JSON.parse(value) : value;
  } catch (error) {
    return value;
  }
}

export default NhatKy;

