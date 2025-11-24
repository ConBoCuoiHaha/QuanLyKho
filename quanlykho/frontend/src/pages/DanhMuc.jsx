import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const CategoryPage = () => {
  const [categories, setCategories] = useState([]);
  const [categoryName, setCategoryName] = useState("");
  const [message, setMessage] = useState("");
  const [isEditing, setIsEditing] = useState(false);
  const [editingCategoryId, setEditingCategoryId] = useState(null);

  useEffect(() => {
    const getCategories = async () => {
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
    };
    getCategories();
  }, []);

  const addCategory = async () => {
    if (!categoryName) {
      showMessage("Tên danh mục không được để trống");
      return;
    }
    try {
      await ApiService.createCategory({ name: categoryName });
      showMessage("Thêm danh mục thành công");
      setCategoryName("");
      window.location.reload();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi thêm danh mục: " + error
      );
    }
  };

  const editCategory = async () => {
    try {
      await ApiService.updateCategory(editingCategoryId, { name: categoryName });
      showMessage("Cập nhật danh mục thành công");
      setIsEditing(false);
      setCategoryName("");
      window.location.reload();
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Lỗi cập nhật danh mục: " + error
      );
    }
  };

  const handleEditCategory = (category) => {
    setIsEditing(true);
    setEditingCategoryId(category.id);
    setCategoryName(category.name);
  };

  const handleDeleteCategory = async (categoryId) => {
    if (window.confirm("Xác nhận xóa danh mục này?")) {
      try {
        await ApiService.deleteCategory(categoryId);
        showMessage("Xóa danh mục thành công");
        window.location.reload();
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Lỗi xóa danh mục: " + error
        );
      }
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
      <div className="category-page">
        <div className="category-header">
          <h1>Danh mục</h1>
          <div className="add-cat">
            <input
              value={categoryName}
              type="text"
              placeholder="Tên danh mục"
              onChange={(e) => setCategoryName(e.target.value)}
            />

            {!isEditing ? (
              <button onClick={addCategory}>Thêm danh mục</button>
            ) : (
              <button onClick={editCategory}>Lưu thay đổi</button>
            )}
          </div>
        </div>

        {categories && (
          <ul className="category-list">
            {categories.map((category) => (
              <li className="category-item" key={category.id}>
                <span>{category.name}</span>

                <div className="category-actions">
                  <button onClick={() => handleEditCategory(category)}>
                    Sửa
                  </button>
                  <button onClick={() => handleDeleteCategory(category.id)}>
                    Xóa
                  </button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </Layout>
  );
};

export default CategoryPage;
