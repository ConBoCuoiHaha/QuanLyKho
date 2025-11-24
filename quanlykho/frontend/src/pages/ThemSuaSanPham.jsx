import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate, useParams } from "react-router-dom";

const AddEditProductPage = () => {
  const { productId } = useParams();
  const [name, setName] = useState("");
  const [sku, setSku] = useState("");
  const [price, setPrice] = useState("");
  const [stockQuantity, setStockQuantity] = useState("");
  const [minStock, setMinStock] = useState("");
  const [categoryId, setCategoryId] = useState("");
  const [description, setDescription] = useState("");
  const [imageFile, setImageFile] = useState(null);
  const [imageUrl, setImageUrl] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [categories, setCategories] = useState([]);
  const [message, setMessage] = useState("");

  const navigate = useNavigate();

  useEffect(() => {
    const fetchCategories = async () => {
      try {
        const categoriesData = await ApiService.getAllCategory();
        setCategories(categoriesData.categories || []);
      } catch (error) {
        showMessage(error.response?.data?.message || "Lỗi tải danh mục: " + error);
      }
    };

    const fetchProductById = async () => {
      if (!productId) return;
      setIsEditing(true);
      try {
        const productData = await ApiService.getProductById(productId);
        if (productData.status === 200) {
          const sanPham = productData.sanPham;
          setName(sanPham.name);
          setSku(sanPham.sku);
          setPrice(sanPham.price);
          setStockQuantity(sanPham.stockQuantity);
          setMinStock(sanPham.minStock ?? "");
          setCategoryId(sanPham.categoryId);
          setDescription(sanPham.description);
          setImageUrl(sanPham.imageUrl);
        } else {
          showMessage(productData.message);
        }
      } catch (error) {
        showMessage(error.response?.data?.message || "Lỗi tải sản phẩm: " + error);
      }
    };

    fetchCategories();
    fetchProductById();
  }, [productId]);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  const handleImageChange = (e) => {
    const file = e.target.files[0];
    setImageFile(file);
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => setImageUrl(reader.result);
      reader.readAsDataURL(file);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const formData = new FormData();
    formData.append("name", name);
    formData.append("sku", sku);
    formData.append("price", price);
    formData.append("stockQuantity", stockQuantity);
    formData.append("minStock", minStock === "" ? 0 : minStock);
    formData.append("categoryId", categoryId);
    formData.append("description", description);
    if (imageFile) {
      formData.append("imageFile", imageFile);
    }

    try {
      if (isEditing) {
        formData.append("sanPhamId", productId);
        await ApiService.updateProduct(formData);
        showMessage("Cập nhật sản phẩm thành công");
      } else {
        await ApiService.addProduct(formData);
        showMessage("Thêm sản phẩm thành công");
      }
      navigate("/san-pham");
    } catch (error) {
      showMessage(error.response?.data?.message || "Lỗi lưu sản phẩm: " + error);
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}

      <div className="product-form-page">
        <h1>{isEditing ? "Chỉnh sửa sản phẩm" : "Thêm sản phẩm"}</h1>
        <form onSubmit={handleSubmit}>
          <div className="form-group">
            <label>Tên sản phẩm</label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>SKU</label>
            <input
              type="text"
              value={sku}
              onChange={(e) => setSku(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Tồn kho</label>
            <input
              type="number"
              value={stockQuantity}
              onChange={(e) => setStockQuantity(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Mức tồn tối thiểu</label>
            <input
              type="number"
              value={minStock}
              onChange={(e) => setMinStock(e.target.value)}
              min="0"
            />
          </div>

          <div className="form-group">
            <label>Giá</label>
            <input
              type="number"
              value={price}
              onChange={(e) => setPrice(e.target.value)}
              required
            />
          </div>

          <div className="form-group">
            <label>Mô tả</label>
            <textarea
              value={description}
              onChange={(e) => setDescription(e.target.value)}
            />
          </div>

          <div className="form-group">
            <label>Danh mục</label>
            <select
              value={categoryId}
              onChange={(e) => setCategoryId(e.target.value)}
              required
            >
              <option value="">-- Chon Danh mục --</option>
              {categories.map((category) => (
                <option key={category.id} value={category.id}>
                  {category.name}
                </option>
              ))}
            </select>
          </div>

          <div className="form-group">
            <label>Hình ảnh</label>
            <input type="file" onChange={handleImageChange} />

            {imageUrl && (
              <img src={imageUrl} alt="preview" className="image-preview" />
            )}
          </div>
          <button type="submit">
            {isEditing ? "Lưu thay đổi" : "Lưu sản phẩm"}
          </button>
        </form>
      </div>
    </Layout>
  );
};

export default AddEditProductPage;

