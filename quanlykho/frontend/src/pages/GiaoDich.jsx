import React, { useState, useEffect } from "react";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import { useNavigate } from "react-router-dom";
import PaginationComponent from "../component/PaginationComponent";
import {
  hienThiLoaiGiaoDich,
  hienThiTrangThaiGiaoDich,
} from "../utils/hienThi";

const TransactionsPage = () => {
  const [giaoDichs, setGiaoDichs] = useState([]);
  const [message, setMessage] = useState("");
  const [boLoc, setBoLoc] = useState("");
  const [tuKhoa, setTuKhoa] = useState("");
  const [exportTuNgay, setExportTuNgay] = useState("");
  const [exportDenNgay, setExportDenNgay] = useState("");

  const navigate = useNavigate();

  //Pagination Set-Up
  const [currentPage, setCurrentPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const itemsPerPage = 10;

  useEffect(() => {
    const getTransactions = async () => {
      try {
        const transactionData = await ApiService.getAllTransactions(tuKhoa);

        if (transactionData.status === 200) {
          const danhSach = transactionData.giaoDichs || [];
          setTotalPages(Math.ceil(danhSach.length / itemsPerPage));

          setGiaoDichs(
            danhSach.slice(
              (currentPage - 1) * itemsPerPage,
              currentPage * itemsPerPage
            )
          );
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message ||
            "Lỗi tải danh sách giao dịch: " + error
        );
      }
    };

    getTransactions();
  }, [currentPage, tuKhoa]);



  //Method to show message or errors
  const showMessage = (msg) => {
    setMessage(msg);
    setTimeout(() => {
      setMessage("");
    }, 4000);
  };


  //handle search
  const handleSearch = () => {
    setCurrentPage(1);
    setTuKhoa(boLoc);
  };

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

  const handleExportTransactions = async () => {
    try {
      const response = await ApiService.exportTransactionsExcel({
        tuNgay: exportTuNgay || undefined,
        denNgay: exportDenNgay || undefined,
      });
      downloadBlob(response.data, "giao-dich.xlsx");
      showMessage("Đã xuất file giao dịch");
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể xuất giao dịch: " + error
      );
    }
  };

  //Navigate to transactions details page
  const navigateToTransactionDetailsPage = (transactionId) =>{
    navigate(`/giao-dich/${transactionId}`);
  }

  return (
    <Layout>

      {message && <p className="message">{message}</p>}
      <div className="transactions-page">
        <div className="transactions-header">
          <h1>Giao dịch</h1>
          <div className="transaction-search">
            <input
              placeholder="Tìm kiếm giao dịch..."
              value={boLoc}
              onChange={(e) => setBoLoc(e.target.value)}
              type="text"
            />
            <button type="button" onClick={handleSearch}>
              Tìm kiếm
            </button>
          </div>
        </div>
        <div className="transaction-actions">
          <input
            type="date"
            value={exportTuNgay}
            onChange={(e) => setExportTuNgay(e.target.value)}
          />
          <input
            type="date"
            value={exportDenNgay}
            onChange={(e) => setExportDenNgay(e.target.value)}
          />
          <button
            type="button"
            className="btn-secondary"
            onClick={handleExportTransactions}
          >
            Xuất Excel
          </button>
        </div>

        {giaoDichs && 
            <table className="transactions-table">
                <thead>
                    <tr>
                        <th>Loại</th>
                        <th>Trạng thái</th>
                        <th>Tổng tiền</th>
                        <th>Tổng sản phẩm</th>
                        <th>Thời gian</th>
                        <th>Thao tác</th>
                    </tr>
                </thead>

                <tbody>
                    {giaoDichs.map((giaoDich) => (
                        <tr key={giaoDich.id}>
                            <td>
                              {hienThiLoaiGiaoDich(giaoDich.transactionType)}
                            </td>
                            <td>{hienThiTrangThaiGiaoDich(giaoDich.status)}</td>
                            <td>{giaoDich.totalPrice?.toLocaleString("vi-VN")} VND</td>
                            <td>{giaoDich.totalProducts}</td>
                            <td>{new Date(giaoDich.createdAt).toLocaleString()}</td>

                            <td>
                                <button onClick={()=> navigateToTransactionDetailsPage(giaoDich.id)}>Xem chi tiết</button>
                            </td>
                        </tr>
                    ))}
                </tbody>
            </table>
        }
      </div>


      <PaginationComponent
      currentPage={currentPage}
      totalPages={totalPages}
      onPageChange={setCurrentPage}
      />
    </Layout>
  );
};
export default TransactionsPage;
