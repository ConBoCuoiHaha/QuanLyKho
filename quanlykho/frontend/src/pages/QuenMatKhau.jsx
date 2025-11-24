import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import ApiService from "../service/ApiService";

const QuenMatKhau = () => {
  const [step, setStep] = useState("otp");
  const [email, setEmail] = useState("");
  const [otp, setOtp] = useState("");
  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const showMessage = (text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  };

  const requestOtp = async (event) => {
    event.preventDefault();
    if (!email.trim()) {
      showMessage("Vui lòng nhập email");
      return;
    }
    setLoading(true);
    try {
      const res = await ApiService.requestPasswordOtp(email.trim());
      showMessage(res.message || "Đã gửi OTP. Vui lòng kiểm tra console server (demo).");
      setStep("reset");
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể gửi OTP");
    } finally {
      setLoading(false);
    }
  };

  const resetPassword = async (event) => {
    event.preventDefault();
    if (newPassword !== confirmPassword) {
      showMessage("Mật khẩu mới và xác nhận không khớp");
      return;
    }
    setLoading(true);
    try {
      const res = await ApiService.resetPasswordWithOtp({
        email: email.trim(),
        otp: otp.trim(),
        matKhauMoi: newPassword,
        xacNhanMatKhau: confirmPassword,
      });
      showMessage(res.message || "Đặt lại mật khẩu thành công");
      setTimeout(() => navigate("/dang-nhap"), 1500);
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể đặt lại mật khẩu");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <h2>Quên mật khẩu</h2>
      {message && <p className="message">{message}</p>}

      {step === "otp" && (
        <form onSubmit={requestOtp}>
          <input
            type="email"
            placeholder="Nhập email đã đăng ký"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? "Đang gửi OTP..." : "Gửi OTP"}
          </button>
        </form>
      )}

      {step === "reset" && (
        <form onSubmit={resetPassword}>
          <input
            type="text"
            placeholder="Nhập mã OTP 6 chữ số"
            value={otp}
            onChange={(e) => setOtp(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Mật khẩu mới"
            value={newPassword}
            onChange={(e) => setNewPassword(e.target.value)}
            required
          />
          <input
            type="password"
            placeholder="Nhap lai Mật khẩu mới"
            value={confirmPassword}
            onChange={(e) => setConfirmPassword(e.target.value)}
            required
          />
          <button type="submit" disabled={loading}>
            {loading ? "Đang cập nhật..." : "Đặt lại mật khẩu"}
          </button>
        </form>
      )}

      <p>
        Bạn nhớ mật khẩu? <a href="/dang-nhap">Quay lại đăng nhập</a>
      </p>
    </div>
  );
};

export default QuenMatKhau;

