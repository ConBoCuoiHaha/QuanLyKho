import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../service/ApiService";

const DangKyPage = () => {
  const [name, setName] = useState("");
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [phoneNumber, setPhoneNumber] = useState("");
  const [message, setMessage] = useState("");

  const navigate = useNavigate();

  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      const registerData = { name, email, password, phoneNumber };
      await ApiService.registerUser(registerData);
      setMessage("Đăng ký thành công");
      navigate("/dang-nhap");
    } catch (error) {
      showMessage(error.response?.data?.message || "Lỗi đăng ký: " + error);
    }
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  return (
    <div className="auth-container">
      <h2>Đăng ký</h2>
      {message && <p className="message">{message}</p>}

      <form onSubmit={handleRegister}>
        <input
          type="text"
          placeholder="Họ và tên"
          value={name}
          onChange={(e) => setName(e.target.value)}
          required
        />
        <input
          type="email"
          placeholder="Email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />
        <input
          type="password"
          placeholder="Mật khẩu"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />
        <input
          type="text"
          placeholder="Số điện thoại"
          value={phoneNumber}
          onChange={(e) => setPhoneNumber(e.target.value)}
          required
        />

        <button type="submit">Đăng ký</button>
      </form>
      <p>
        Đã có tài khoản? <a href="/dang-nhap">Đăng nhập</a>
      </p>
    </div>
  );
};

export default DangKyPage;
