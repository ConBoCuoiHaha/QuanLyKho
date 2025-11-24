import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../service/ApiService";

const DangNhapPage = () => {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [message, setMessage] = useState("");

  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      const loginData = { email, password };
      const res = await ApiService.loginUser(loginData);

      if (res.status === 200) {
        ApiService.saveToken(res.token);
        ApiService.saveRole(res.role);
        setMessage(res.message);
        navigate("/bang-dieu-khien");
      }
    } catch (error) {
      showMessage(error.response?.data?.message || "Đăng nhập thất bại");
    }
  };

  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => setMessage(""), 4000);
  };

  return (
    <div className="auth-container">
      <h2>Đăng nhập</h2>

      {message && <p className="message">{message}</p>}

      <form onSubmit={handleLogin}>
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

        <button type="submit">Đăng nhập</button>
      </form>
      <p>
        Quên mật khẩu? <a href="/quen-mat-khau">Khôi phục tài khoản</a>
      </p>
      <p>
        Chưa có tài khoản? <a href="/dang-ky">Đăng ký</a>
      </p>
    </div>
  );
};

export default DangNhapPage;
