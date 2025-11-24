import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const TraHangKhach = () => {
  const [sanPhams, setSanPhams] = useState([]);
  const [customers, setCustomers] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [productId, setProductId] = useState("");
  const [customerId, setCustomerId] = useState("");
  const [warehouseId, setWarehouseId] = useState("");
  const [quantity, setQuantity] = useState("");
  const [description, setDescription] = useState("");
  const [note, setNote] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [productRes, customerRes, warehouseRes] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getCustomers(),
          ApiService.getWarehouses(),
        ]);
        setSanPhams(productRes.sanPhams || []);
        setCustomers(customerRes.khachHangs || []);
        setWarehouses(warehouseRes.khos || []);
      } catch (error) {
        showMessage(error.response?.data?.message || "Không tải được dữ liệu");
      }
    };
    fetchData();
  }, []);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!productId || !quantity || !warehouseId) {
      showMessage("Vui lòng chọn sản phẩm, số lượng và kho nhận");
      return;
    }
    try {
      await ApiService.returnFromCustomer({
        productId,
        quantity: Number(quantity),
        description,
        note,
        khoId: Number(warehouseId),
        khachHangId: customerId ? Number(customerId) : null,
      });
      showMessage("Đã ghi nhận trả hàng");
      setProductId("");
      setCustomerId("");
      setWarehouseId("");
      setQuantity("");
      setDescription("");
      setNote("");
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể trả hàng");
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="purchase-form-page">
        <h1>Khách hàng trả hàng</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Sản phẩm</label>
            <select
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
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
            <label>Khách hàng (tùy chọn)</label>
            <select
              value={customerId}
              onChange={(e) => setCustomerId(e.target.value)}
            >
              <option value="">-- Không chọn --</option>
              {customers.map((customer) => (
                <option key={customer.id} value={customer.id}>
                  {customer.name} {customer.phone ? `(${customer.phone})` : ""}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Trả về kho</label>
            <select
              value={warehouseId}
              onChange={(e) => setWarehouseId(e.target.value)}
              required
            >
              <option value="">-- Chọn kho --</option>
              {warehouses.map((warehouse) => (
                <option key={warehouse.id} value={warehouse.id}>
                  {warehouse.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Số lượng trả</label>
            <input
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Lý do</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Nhap Lý do tra hang"
            />
          </div>

          <div className="form-group">
            <label>Ghi chú</label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
            />
          </div>

          <button type="submit">Xác nhận trả hàng</button>
        </form>
      </div>
    </Layout>
  );
};

export default TraHangKhach;

