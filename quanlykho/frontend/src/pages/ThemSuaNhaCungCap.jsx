import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";

const AddEditSupplierPage = () => {
  const { nhaCungCapId } = useParams();
  const [name, setName] = useState("");
  const [contactInfo, setContactInfo] = useState("");
  const [address, setAddress] = useState("");
  const [message, setMessage] = useState("");
  const [isEditing, setIsEditing] = useState(false);

  const navigate = useNavigate();

  useEffect(() => {
    if (nhaCungCapId) {
      setIsEditing(true);

      const fetchSupplier = async () => {
        try {
          const supplierData = await ApiService.getSupplierById(nhaCungCapId);
          if (supplierData.status === 200) {
            setName(supplierData.nhaCungCap.name);
            setContactInfo(supplierData.nhaCungCap.contactInfo);
            setAddress(supplierData.nhaCungCap.address);
          }
        } catch (error) {
          showMessage(
            error.response?.data?.message || "Lỗi tải nhà cung cấp: " + error
          );
        }
      };
      fetchSupplier();
    }
  }, [nhaCungCapId]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const supplierData = { name, contactInfo, address };

    try {
      if (isEditing) {
        await ApiService.updateSupplier(nhaCungCapId, supplierData);
        showMessage("Cập nhật nhà cung cấp thành công");
        navigate("/nha-cung-cap");
      } else {
        await ApiService.addSupplier(supplierData);
        showMessage("Thêm nhà cung cấp thành công");
        navigate("/nha-cung-cap");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi lưu nhà cung cấp: " + error
      );
    }
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="supplier-form-page">
        <h1>{isEditing ? "Chỉnh sửa nhà cung cấp" : "Thêm nhà cung cấp"}</h1>

        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tên nhà cung cấp</label>
            <input
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              type="text"
            />
          </div>

          <div className="form-group">
            <label>Thông tin liên hệ</label>
            <input
              value={contactInfo}
              onChange={(e) => setContactInfo(e.target.value)}
              required
              type="text"
            />
          </div>

          <div className="form-group">
            <label>Địa chỉ</label>
            <input
              value={address}
              onChange={(e) => setAddress(e.target.value)}
              required
              type="text"
            />
          </div>
          <button type="submit">
            {isEditing ? "Lưu thay đổi" : "Lưu nhà cung cấp"}
          </button>
        </form>
      </div>
    </Layout>
  );
};
export default AddEditSupplierPage;

