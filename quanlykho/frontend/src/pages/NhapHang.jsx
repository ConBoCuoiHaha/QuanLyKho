import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const PurchasePage = () => {
  const [sanPhams, setSanPhams] = useState([]);
  const [nhaCungCaps, setNhaCungCaps] = useState([]);
  const [productId, setProductId] = useState("");
  const [supplierId, setSupplierId] = useState("");
  const [description, setDescription] = useState("");
  const [note, setNote] = useState("");
  const [quantity, setQuantity] = useState("");
  const [lotNumber, setLotNumber] = useState("");
  const [receivedDate, setReceivedDate] = useState("");
  const [warehouses, setWarehouses] = useState([]);
  const [warehouseId, setWarehouseId] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [productData, supplierData, warehouseData] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getAllSuppliers(),
          ApiService.getWarehouses(),
        ]);
        setSanPhams(productData.sanPhams || []);
        setNhaCungCaps(supplierData.nhaCungCaps || []);
        setWarehouses(warehouseData.khos || []);
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Lỗi tải dữ liệu nhập hàng: " + error
        );
      }
    };

    fetchData();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!productId || !supplierId || !quantity) {
      showMessage("Vui lòng nhập đầy đủ thông tin");
      return;
    }

    const body = {
      productId,
      quantity: parseInt(quantity, 10),
      supplierId,
      description,
      note,
      soLo: lotNumber,
      ngayNhap: receivedDate,
      khoId: warehouseId ? Number(warehouseId) : null,
    };

    try {
      const response = await ApiService.purchaseProduct(body);
      showMessage(response.message);
      resetForm();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi khi nhập hàng: " + error
      );
    }
  };

  const resetForm = () => {
    setProductId("");
    setSupplierId("");
    setDescription("");
    setNote("");
    setQuantity("");
    setLotNumber("");
    setReceivedDate("");
    setWarehouseId("");
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
      <div className="purchase-form-page">
        <h1>Nhập hàng</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Chọn sản phẩm</label>

            <select
              value={productId}
              onChange={(e) => setProductId(e.target.value)}
              required
            >
              <option value="">-- Chọn sản phẩm --</option>
              {sanPhams.map((product) => (
                <option key={product.id} value={product.id}>
                  {product.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Chọn nhà cung cấp</label>

            <select
              value={supplierId}
              onChange={(e) => setSupplierId(e.target.value)}
              required
            >
              <option value="">-- Chọn nhà cung cấp --</option>
              {nhaCungCaps.map((supplier) => (
                <option key={supplier.id} value={supplier.id}>
                  {supplier.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Nhập về kho</label>
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
            <label>Số lô</label>
            <input
              type="text"
              value={lotNumber}
              onChange={(e) => setLotNumber(e.target.value)}
              placeholder="VD: L2025-01"
            />
          </div>

          <div className="form-group">
            <label>Ngày nhập lô</label>
            <input
              type="date"
              value={receivedDate}
              onChange={(e) => setReceivedDate(e.target.value)}
            />
          </div><div className="form-group">
            <label>Mô tả</label>
            <input
              type="text"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Ghi chú</label>
            <input
              type="text"
              value={note}
              onChange={(e) => setNote(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Số lượng</label>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
            />
          </div>

          <button type="submit">Nhập hàng</button>
        </form>
      </div>
    </Layout>
  );
};
export default PurchasePage;




