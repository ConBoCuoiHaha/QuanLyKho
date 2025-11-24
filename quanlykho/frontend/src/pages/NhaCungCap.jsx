import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";

const SupplierPage = () => {
  const [nhaCungCaps, setNhaCungCaps] = useState([]);
  const [message, setMessage] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const getSuppliers = async () => {
      try {
        const responseData = await ApiService.getAllSuppliers();
        if (responseData.status === 200) {
          setNhaCungCaps(responseData.nhaCungCaps || []);
        } else {
          showMessage(responseData.message);
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Lỗi tải danh sách nhà cung cấp: " + error
        );
      }
    };
    getSuppliers();
  }, []);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleDeleteSupplier = async (supplierId) => {
    try {
      if (window.confirm("Xác nhận xóa nhà cung cấp này?")) {
        await ApiService.deleteSupplier(supplierId);
        window.location.reload();
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi khi xóa nhà cung cấp: " + error
      );
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="supplier-page">
        <div className="supplier-header">
          <h1>Nhà cung cấp</h1>
          <div className="add-sup">
            <button onClick={() => navigate("/them-nha-cung-cap")}>
              Thêm nhà cung cấp
            </button>
          </div>
        </div>
      </div>

      {nhaCungCaps && (
        <ul className="supplier-list">
          {nhaCungCaps.map((supplier) => (
            <li className="supplier-item" key={supplier.id}>
              <span>{supplier.name}</span>

              <div className="supplier-actions">
                <button onClick={() => navigate(`/sua-nha-cung-cap/${supplier.id}`)}>
                  Sửa
                </button>
                <button onClick={() => handleDeleteSupplier(supplier.id)}>
                  Xóa
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </Layout>
  );
};

export default SupplierPage;
