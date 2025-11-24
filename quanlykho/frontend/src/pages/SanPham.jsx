import React, { useCallback, useEffect, useRef, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";
import PaginationComponent from "../component/PaginationComponent";

const ProductPage = () => {
  const [products, setProducts] = useState([]);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const [categories, setCategories] = useState([]);
  const [filters, setFilters] = useState({ keyword: "", categoryId: "" });
  const [keywordInput, setKeywordInput] = useState("");
  const [categoryInput, setCategoryInput] = useState("");
  const [importErrors, setImportErrors] = useState([]);
  const fileInputRef = useRef(null);

  const navigate = useNavigate();

  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const itemsPerPage = 10;
  const hasActiveFilters =
    (filters.keyword && filters.keyword.length > 0) ||
    (filters.categoryId && filters.categoryId !== "");
  const selectedCategoryLabel = filters.categoryId
    ? categories.find(
        (category) => String(category.id) === String(filters.categoryId)
      )
    : null;

  const showMessage = useCallback((msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadProducts = useCallback(async () => {
    setLoading(true);
    try {
      const selectedCategory =
        filters.categoryId && Number(filters.categoryId) > 0
          ? Number(filters.categoryId)
          : undefined;
      const response = await ApiService.getProductsPage({
        page: currentPage - 1,
        size: itemsPerPage,
        keyword: filters.keyword,
        categoryId: selectedCategory,
      });
      if (response.status === 200) {
        setProducts(response.sanPhams || []);
        const total = response.totalPages || 1;
        setTotalPages(total > 0 ? total : 1);
      } else {
        showMessage(response.message || "Không thể tải sản phẩm");
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi tải danh sách sản phẩm: " + error
      );
    } finally {
      setLoading(false);
    }
  }, [
    currentPage,
    itemsPerPage,
    filters.keyword,
    filters.categoryId,
    showMessage,
  ]);

  const loadCategories = useCallback(async () => {
    try {
      const response = await ApiService.getAllCategory();
      if (response.status === 200) {
        setCategories(response.categories || []);
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi tải danh mục: " + error
      );
    }
  }, [showMessage]);

  const downloadBlob = (data, filename) => {
    const url = window.URL.createObjectURL(new Blob([data]));
    const link = document.createElement("a");
    link.href = url;
    link.setAttribute("download", filename);
    document.body.appendChild(link);
    link.click();
    link.remove();
    window.URL.revokeObjectURL(url);
  };

  const handleExportProducts = async () => {
    try {
      const response = await ApiService.exportProductsExcel();
      downloadBlob(response.data, "san-pham.xlsx");
      showMessage("Đã xuất file sản phẩm");
      setImportErrors([]);
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể xuất sản phẩm: " + error
      );
    }
  };

  const handleImportClick = () => {
    fileInputRef.current?.click();
  };

  const handleImportFileChange = async (event) => {
    const file = event.target.files && event.target.files[0];
    if (!file) {
      return;
    }
    const formData = new FormData();
    formData.append("file", file);
    try {
      const response = await ApiService.importProductsExcel(formData);
      const success = response.soLuongThanhCong ?? 0;
      const failed = response.soLuongThatBai ?? 0;
      showMessage(
        `${response.message || "Nhập sản phẩm thành công"} (Thành công: ${success}, Thất bại: ${failed})`
      );
      setImportErrors(response.loiNhap || []);
      loadProducts();
    } catch (error) {
      setImportErrors([]);
      showMessage(
        error.response?.data?.message || "Không thể nhập sản phẩm: " + error
      );
    } finally {
      event.target.value = "";
    }
  };

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  const handleFilterSubmit = (event) => {
    event.preventDefault();
    setFilters({
      keyword: keywordInput.trim(),
      categoryId: categoryInput,
    });
    setCurrentPage(1);
  };

  const handleResetFilters = () => {
    setKeywordInput("");
    setCategoryInput("");
    setFilters({ keyword: "", categoryId: "" });
    setCurrentPage(1);
  };

  const handleDeleteProduct = async (productId) => {
    if (!window.confirm("Ban co chac chan muon Xóa san pham nay?")) {
      return;
    }
    try {
      await ApiService.deleteProduct(productId);
      showMessage("Xóa san pham Thành công");
      loadProducts();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Loi khi Xóa san pham: " + error
      );
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}

      <div className="product-page">
        <div className="product-header">
          <h1>San pham</h1>
          <div className="product-header-actions">
            <button
              type="button"
              className="btn-secondary"
              onClick={handleExportProducts}
            >
              Xuat Excel
            </button>
            <button
              type="button"
              className="btn-secondary"
              onClick={handleImportClick}
            >
              Nhap Excel
            </button>
            <button
              className="add-product-btn"
              onClick={() => navigate("/them-san-pham")}
            >
              Thêm sản phẩm
            </button>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleImportFileChange}
              accept=".xlsx,.xls"
              style={{ display: "none" }}
            />
          </div>
        </div>

        <form className="product-filters" onSubmit={handleFilterSubmit}>
          <input
            type="text"
            placeholder="Tìm tên hoặc SKU"
            value={keywordInput}
            onChange={(e) => setKeywordInput(e.target.value)}
          />
          <select
            value={categoryInput}
            onChange={(e) => setCategoryInput(e.target.value)}
          >
            <option value="">Tất cả danh mục</option>
            {categories.map((category) => (
              <option key={category.id} value={category.id}>
                {category.name}
              </option>
            ))}
          </select>
          <button type="submit" className="apply-filter-btn">
            Áp dụng
          </button>
          <button
            type="button"
            className="reset-filter-btn"
            onClick={handleResetFilters}
            disabled={!hasActiveFilters}
          >
            Xóa lọc
          </button>
        </form>

        {hasActiveFilters && (
          <div className="active-filters">
            <span>Dang Áp dụng:</span>
            {filters.keyword && (
              <span className="filter-tag">Từ khóa: {filters.keyword}</span>
            )}
            {filters.categoryId && (
              <span className="filter-tag">
                Danh mục: {selectedCategoryLabel?.name || filters.categoryId}
              </span>
            )}
          </div>
        )}

        {importErrors.length > 0 && (
          <div className="import-errors">
            <h4>Lỗi nhập:</h4>
            <ul>
              {importErrors.map((error, index) => (
                <li key={index}>{error}</li>
              ))}
            </ul>
          </div>
        )}

        {loading ? (
          <p className="empty-text">Đang tải danh sách sản phẩm...</p>
        ) : products.length === 0 ? (
          <p className="empty-text">Chưa có sản phẩm</p>
        ) : (
          <div className="product-list">
            {products.map((product) => (
              <div key={product.id} className="product-item">
                <img
                  className="product-image"
                  src={product.imageUrl}
                  alt={product.name}
                />

                <div className="product-info">
                  <h3 className="name">{product.name}</h3>
                  <p className="sku">SKU: {product.sku}</p>
                  <p className="price">Giá: {product.price}</p>
                  <p className="quantity">Tồn kho: {product.stockQuantity}</p>
                  <p className="quantity">Tồn tối thiểu: {product.minStock ?? "0"}</p>
                  {product.minStock != null &&
                    product.stockQuantity <= product.minStock && (
                      <p className="alert-text">Cần nhập thêm</p>
                    )}
                </div>

                <div className="product-actions">
                  <button
                    className="edit-btn"
                    onClick={() => navigate(`/sua-san-pham/${product.id}`)}
                  >
                    Chỉnh sửa
                  </button>
                  <button
                    className="delete-btn"
                    onClick={() => handleDeleteProduct(product.id)}
                  >
                    Xóa
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      <PaginationComponent
        currentPage={currentPage}
        totalPages={totalPages}
        onPageChange={setCurrentPage}
      />
    </Layout>
  );
};
export default ProductPage;


