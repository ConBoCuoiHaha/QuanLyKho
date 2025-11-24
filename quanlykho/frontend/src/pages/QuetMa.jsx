import React, { useCallback, useEffect, useMemo, useRef, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { Html5QrcodeScanner } from "html5-qrcode";

const QuetMa = () => {
  const [scanResult, setScanResult] = useState("");
  const [product, setProduct] = useState(null);
  const [error, setError] = useState("");
  const [loadingProduct, setLoadingProduct] = useState(false);
  const [scannerKey, setScannerKey] = useState(0);
  const scannerRef = useRef(null);

  const fetchProduct = useCallback(
    async (sku) => {
      setLoadingProduct(true);
      try {
        const res = await ApiService.getProductBySku(sku);
        setProduct(res.sanPham || null);
        if (!res.sanPham) {
          setError("Không tìm thấy sản phẩm với mã vừa quét");
        }
      } catch (err) {
        setProduct(null);
        setError(err.response?.data?.message || "Không tìm thấy sản phẩm");
      } finally {
        setLoadingProduct(false);
      }
    },
    []
  );

  const handleScanSuccess = useCallback(
    async (decodedText) => {
      setScanResult(decodedText);
      setError("");
      if (scannerRef.current) {
        scannerRef.current.clear().catch(() => {});
      }
      await fetchProduct(decodedText);
    },
    [fetchProduct]
  );

  const handleScanFailure = useCallback(() => {
    // ignore continuous errors
  }, []);

  const startScanner = useCallback(() => {
    const config = {
      fps: 10,
      qrbox: { width: 250, height: 250 },
      rememberLastUsedCamera: true,
      disableFlip: false,
    };
    const scanner = new Html5QrcodeScanner("barcode-reader", config, false);
    scannerRef.current = scanner;
    scanner.render(handleScanSuccess, handleScanFailure);
  }, [handleScanFailure, handleScanSuccess]);

  useEffect(() => {
    startScanner();
    return () => {
      if (scannerRef.current) {
        scannerRef.current.clear().catch(() => {});
      }
    };
  }, [scannerKey, startScanner]);

  const resetScanner = () => {
    setScanResult("");
    setProduct(null);
    setError("");
    setScannerKey((prev) => prev + 1);
  };

  const productInfo = useMemo(() => {
    if (!product) return null;
    return (
      <div className="scan-result-card">
        <h3>{product.name}</h3>
        <p>SKU: {product.sku}</p>
        <p>Tồn kho: {product.stockQuantity}</p>
        <p>Giá: {product.price}</p>
      </div>
    );
  }, [product]);

  return (
    <Layout>
      <div className="scanner-page">
        <div className="scanner-card">
          <h1>Quét mã vạch / QR</h1>
          <p className="text-subtle">
            Đưa mã trước camera, hệ thống sẽ tự động truy ra sản phẩm theo SKU.
          </p>
          <div id="barcode-reader" className="scanner-wrapper"></div>
          <button type="button" onClick={resetScanner}>
            Quét lại
          </button>
        </div>

        <div className="scanner-card">
          <h2>Kết quả</h2>
          {scanResult && (
            <p className="scan-text">
              Mã đã quét: <strong>{scanResult}</strong>
            </p>
          )}
          {loadingProduct && <p className="empty-text">Đang tải sản phẩm...</p>}
          {error && <p className="text-negative">{error}</p>}
          {productInfo || (!scanResult && <p className="empty-text">Chưa có dữ liệu</p>)}
        </div>
      </div>
    </Layout>
  );
};

export default QuetMa;

