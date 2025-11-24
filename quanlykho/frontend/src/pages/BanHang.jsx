import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const SellPage = () => {
  const [sanPhams, setSanPhams] = useState([]);
  const [warehouses, setWarehouses] = useState([]);
  const [productId, setProductId] = useState("");
  const [warehouseId, setWarehouseId] = useState("");
  const [description, setDescription] = useState("");
  const [note, setNote] = useState("");
  const [quantity, setQuantity] = useState("");
  const [message, setMessage] = useState("");

  useEffect(() => {
    const fetchResources = async () => {
      try {
        const [productData, warehouseData] = await Promise.all([
          ApiService.getAllProducts(),
          ApiService.getWarehouses(),
        ]);
        setSanPhams(productData.sanPhams || []);
        setWarehouses(warehouseData.khos || []);
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Lỗi tải dữ liệu sản phẩm: " + error
        );
      }
    };

    fetchResources();
  }, []);

  const handleSubmit = async (e) => {
    e.preventDefault();

    if (!productId || !quantity || !warehouseId) {
      showMessage("Vui lòng nhập đầy đủ thông tin");
      return;
    }
    const body = {
      productId,
      quantity: parseInt(quantity, 10),
      description,
      note,
      khoId: Number(warehouseId),
    };

    try {
      const response = await ApiService.sellProduct(body);
      showMessage(response.message);
      resetForm();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi khi bán hàng: " + error
      );
    }
  };

  const resetForm = () => {
    setProductId("");
    setWarehouseId("");
    setDescription("");
    setNote("");
    setQuantity("");
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="purchase-form-page">
        <h1>Bán hàng</h1>
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
            <label>Bán tại kho</label>
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
            <label>Số lượng</label>
            <input
              type="number"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
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

          <button type="submit">Bán hàng</button>
        </form>
      </div>
    </Layout>
  );
};

export default SellPage;

