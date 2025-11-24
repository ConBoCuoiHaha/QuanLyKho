import React, { useCallback, useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const downloadBlob = (blob, filename) => {
  const url = window.URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  window.URL.revokeObjectURL(url);
};

const SaoLuu = () => {
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [backups, setBackups] = useState([]);
  const [fileToRestore, setFileToRestore] = useState(null);
  const [seeding, setSeeding] = useState(false);

  const showMessage = (text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  };

  const fetchBackups = useCallback(async () => {
    try {
      const data = await ApiService.getBackupList();
      setBackups(data || []);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tải danh sách backup");
    }
  }, []);

  useEffect(() => {
    fetchBackups();
  }, [fetchBackups]);

  const handleCreateBackup = async () => {
    setLoading(true);
    try {
      const response = await ApiService.exportBackup();
      const filename = response.headers["content-disposition"]
        ?.split("filename=")[1]
        ?.replace(/"/g, "") || `backup-${Date.now()}.json`;
      downloadBlob(response.data, filename);
      showMessage("Đã tạo bản sao lưu mới");
      fetchBackups();
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tạo backup");
    } finally {
      setLoading(false);
    }
  };

  const handleDownload = async (name) => {
    setLoading(true);
    try {
      const response = await ApiService.downloadBackup(name);
      downloadBlob(response.data, name);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tải file");
    } finally {
      setLoading(false);
    }
  };

  const handleRestore = async (event) => {
    event.preventDefault();
    if (!fileToRestore) {
      showMessage("Vui lòng chọn file JSON backup");
      return;
    }
    setLoading(true);
    try {
      const res = await ApiService.restoreBackup(fileToRestore);
      showMessage(res.message || "Khôi phục thành công");
    } catch (error) {
      showMessage(error.response?.data?.message || "Khôi phục thất bại");
    } finally {
      setLoading(false);
    }
  };

  const handleSeed = async (force) => {
    if (!window.confirm(force
      ? "Thao tác này sẽ xóa dữ liệu hiện tại và tạo lại bản demo. Bạn chắc chắn?"
      : "Tạo dữ liệu demo nếu database đang trống. Tiếp tục?")) {
      return;
    }
    setSeeding(true);
    try {
      const res = await ApiService.triggerSeed(force);
      showMessage(res.message || "Đã tạo dữ liệu demo");
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tạo dữ liệu demo");
    } finally {
      setSeeding(false);
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="page-header">
        <div>
          <h1>Sao lưu & Khôi phục</h1>
          <p className="text-subtle">Quản lý file backup JSON của hệ thống</p>
        </div>
        <button type="button" onClick={handleCreateBackup} disabled={loading}>
          {loading ? "Đang xử lý..." : "Tạo bản sao lưu"}
        </button>
      </div>

      <div className="form-card">
        <h3>Khôi phục từ file</h3>
        <p className="text-subtle">Lưu ý: thao tác này sẽ xóa dữ liệu hiện tại và thay bằng dữ liệu backup.</p>
        <form className="password-form" onSubmit={handleRestore}>
          <input
            type="file"
            accept=".json,application/json"
            onChange={(e) => setFileToRestore(e.target.files?.[0] || null)}
          />
          <button type="submit" disabled={loading || !fileToRestore}>
            {loading ? "Đang khôi phục..." : "Khôi phục dữ liệu"}
          </button>
        </form>
      </div>

      <div className="form-card">
        <h3>Dữ liệu demo</h3>
        <p className="text-subtle">
          Tinh nang nay giup tao nhanh Dữ liệu demo (70+ san pham, 100+ giao dich). Force = xoa toan bo du lieu hien tai.
        </p>
        <div className="button-group">
          <button type="button" onClick={() => handleSeed(false)} disabled={seeding}>
            {seeding ? "Đang xử lý..." : "Tạo demo (nếu database rỗng)"}
          </button>
          <button type="button" onClick={() => handleSeed(true)} disabled={seeding}>
            {seeding ? "Đang xử lý..." : "Force tạo lại demo"}
          </button>
        </div>
      </div>

      <div className="form-card">
        <h3>Các bản sao lưu gần đây</h3>
        {backups.length === 0 ? (
          <p className="empty-text">Chưa có bản sao lưu nào</p>
        ) : (
          <div className="table-scroll">
            <table className="simple-table">
              <thead>
                <tr>
                  <th>Tên file</th>
                  <th>Thao tác</th>
                </tr>
              </thead>
              <tbody>
                {backups.map((name) => (
                  <tr key={name}>
                    <td>{name}</td>
                    <td>
                      <button type="button" onClick={() => handleDownload(name)} disabled={loading}>
                        Tải xuống
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </Layout>
  );
};

export default SaoLuu;

