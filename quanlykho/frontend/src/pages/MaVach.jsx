import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const BARCODE_TYPES = [
  { value: "BARCODE", label: "Mã vạch CODE-128" },
  { value: "QR", label: "Mã QR" },
];

const MaVach = () => {
  const [sanPhams, setSanPhams] = useState([]);
  const [selectedSku, setSelectedSku] = useState("");
  const [barcodeType, setBarcodeType] = useState("BARCODE");
  const [quantity, setQuantity] = useState(4);
  const [previewUrl, setPreviewUrl] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const res = await ApiService.getAllProducts();
        setSanPhams(res.sanPhams || []);
      } catch (error) {
        showMessage(error.response?.data?.message || "Không tải được sản phẩm");
      }
    };
    fetchProducts();
  }, []);

  useEffect(() => {
    let revokeUrl;
    const loadPreview = async () => {
      if (!selectedSku) {
        setPreviewUrl("");
        return;
      }
      setLoading(true);
      try {
        const blob = await ApiService.fetchBarcodeImage({
          sku: selectedSku,
          type: barcodeType,
        });
        const url = URL.createObjectURL(blob);
        setPreviewUrl(url);
        revokeUrl = url;
      } catch (error) {
        showMessage(error.response?.data?.message || "Không tạo được mã vạch");
        setPreviewUrl("");
      } finally {
        setLoading(false);
      }
    };
    loadPreview();
    return () => {
      if (revokeUrl) {
        URL.revokeObjectURL(revokeUrl);
      }
    };
  }, [selectedSku, barcodeType]);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  const downloadPng = async () => {
    if (!selectedSku) {
      showMessage("Vui lòng chọn sản phẩm");
      return;
    }
    try {
      const blob = await ApiService.fetchBarcodeImage({
        sku: selectedSku,
        type: barcodeType,
      });
      downloadBlob(blob, `${selectedSku}-${barcodeType.toLowerCase()}.png`);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tải file PNG");
    }
  };

  const downloadPdf = async () => {
    if (!selectedSku) {
      showMessage("Vui lòng chọn sản phẩm");
      return;
    }
    if (quantity < 1 || quantity > 100) {
      showMessage("Số tem phải từ 1 đến 100");
      return;
    }
    try {
      const blob = await ApiService.downloadBarcodePdf({
        sku: selectedSku,
        quantity,
        type: barcodeType,
      });
      downloadBlob(blob, `${selectedSku}-tem.pdf`);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể tải file PDF");
    }
  };

  const downloadBlob = (blob, filename) => {
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");
    link.href = url;
    link.download = filename;
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="barcode-page">
        <div className="barcode-card">
          <h1>Quản lý mã vạch / QR</h1>
          <p className="text-subtle">
            Tự động sinh mã từ SKU và tải về tem in (PNG hoặc PDF).
          </p>

          <div className="form-group">
            <label>Sản phẩm</label>
            <select
              value={selectedSku}
              onChange={(e) => setSelectedSku(e.target.value)}
            >
              <option value="">-- Chon Sản phẩm --</option>
              {sanPhams.map((sp) => (
                <option key={sp.id} value={sp.sku}>
                  {sp.name} ({sp.sku})
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Dạng mã</label>
            <div className="radio-group">
              {BARCODE_TYPES.map((type) => (
                <label key={type.value}>
                  <input
                    type="radio"
                    name="barcodeType"
                    value={type.value}
                    checked={barcodeType === type.value}
                    onChange={(e) => setBarcodeType(e.target.value)}
                  />
                  {type.label}
                </label>
              ))}
            </div>
          </div>

          <div className="form-group">
            <label>Số tem trong PDF</label>
            <input
              type="number"
              min="1"
              max="100"
              value={quantity}
              onChange={(e) => setQuantity(Number(e.target.value))}
            />
          </div>

          <div className="barcode-actions">
            <button type="button" onClick={downloadPng} disabled={!selectedSku}>
              Tải ảnh PNG
            </button>
            <button type="button" onClick={downloadPdf} disabled={!selectedSku}>
              Tải PDF
            </button>
          </div>
        </div>

        <div className="barcode-card">
          <h2>Xem trước</h2>
          {loading && <p className="empty-text">Đang tạo mã...</p>}
          {!loading && !previewUrl && (
            <p className="empty-text">Chon Sản phẩm de xem ma vach</p>
          )}
          {!loading && previewUrl && (
            <div className="barcode-preview">
              <img src={previewUrl} alt="barcode preview" />
              <p>SKU: {selectedSku}</p>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default MaVach;

