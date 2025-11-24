import React, { useEffect, useState } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const ProfilePage = () => {
  const [user, setUser] = useState(null);
  const [message, setMessage] = useState("");
  const [passwordForm, setPasswordForm] = useState({
    oldPassword: "",
    newPassword: "",
    confirmPassword: "",
  });
  const [changing, setChanging] = useState(false);

  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        const userInfo = await ApiService.getLoggedInUserInfo();
        setUser(userInfo);
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải thông tin người dùng"
        );
      }
    };
    fetchUserInfo();
  }, []);

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };

  const handlePasswordChange = (event) => {
    const { name, value } = event.target;
    setPasswordForm((prev) => ({
      ...prev,
      [name]: value,
    }));
  };

  const submitPasswordChange = async (event) => {
    event.preventDefault();
    if (!passwordForm.oldPassword || !passwordForm.newPassword) {
      showMessage("Vui lòng nhập đầy đủ thông tin mật khẩu");
      return;
    }
    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      showMessage("Mật khẩu mới và xác nhận không khớp");
      return;
    }
    setChanging(true);
    try {
      await ApiService.changePassword({
        matKhauCu: passwordForm.oldPassword,
        matKhauMoi: passwordForm.newPassword,
        xacNhanMatKhau: passwordForm.confirmPassword,
      });
      showMessage("Đổi mật khẩu thành công");
      setPasswordForm({
        oldPassword: "",
        newPassword: "",
        confirmPassword: "",
      });
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể đổi mật khẩu, vui lòng thử lại"
      );
    } finally {
      setChanging(false);
    }
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="profile-page">
        {user && (
          <div className="profile-card">
            <h1>Xin chào, {user.name || "Người dùng"}</h1>
            <div className="profile-info">
              <div className="profile-item">
                <label>Họ tên</label>
                <span>{user.name || "--"}</span>
              </div>
              <div className="profile-item">
                <label>Email</label>
                <span>{user.email || "--"}</span>
              </div>
              <div className="profile-item">
                <label>Số điện thoại</label>
                <span>{user.phoneNumber || "--"}</span>
              </div>
              <div className="profile-item">
                <label>Vai trò</label>
                <span>{user.role || "--"}</span>
              </div>
            </div>
          </div>
        )}

        <div className="profile-card">
          <h2>Đổi mật khẩu</h2>
          <p className="text-subtle">
            Mật khẩu mới cần từ 8 ký tự, có chữ hoa, số và ký tự đặc biệt.
          </p>
          <form className="password-form" onSubmit={submitPasswordChange}>
            <label htmlFor="oldPassword">Mật khẩu cũ</label>
            <input
              type="password"
              id="oldPassword"
              name="oldPassword"
              value={passwordForm.oldPassword}
              onChange={handlePasswordChange}
              placeholder="Nhập mật khẩu hiện tại"
            />

            <label htmlFor="newPassword">Mật khẩu mới</label>
            <input
              type="password"
              id="newPassword"
              name="newPassword"
              value={passwordForm.newPassword}
              onChange={handlePasswordChange}
              placeholder="Nhap Mật khẩu mới"
            />

            <label htmlFor="confirmPassword">Xac nhan Mật khẩu mới</label>
            <input
              type="password"
              id="confirmPassword"
              name="confirmPassword"
              value={passwordForm.confirmPassword}
              onChange={handlePasswordChange}
              placeholder="Nhap lai Mật khẩu mới"
            />

            <button type="submit" disabled={changing}>
              {changing ? "Đang đổi..." : "Cập nhật mật khẩu"}
            </button>
          </form>
        </div>
      </div>
    </Layout>
  );
};

export default ProfilePage;

