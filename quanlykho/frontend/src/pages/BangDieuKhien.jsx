import React, { useCallback, useEffect, useMemo, useState } from "react";
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Legend,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";
import {
  hienThiLoaiGiaoDich,
  hienThiTrangThaiGiaoDich,
} from "../utils/hienThi";

const MONTH_OPTIONS = [
  { value: 1, label: "Tháng 1" },
  { value: 2, label: "Tháng 2" },
  { value: 3, label: "Tháng 3" },
  { value: 4, label: "Tháng 4" },
  { value: 5, label: "Tháng 5" },
  { value: 6, label: "Tháng 6" },
  { value: 7, label: "Tháng 7" },
  { value: 8, label: "Tháng 8" },
  { value: 9, label: "Tháng 9" },
  { value: 10, label: "Tháng 10" },
  { value: 11, label: "Tháng 11" },
  { value: 12, label: "Tháng 12" },
];

const numberFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

const formatNumber = (value) => {
  if (!value) return "0";
  const rounded = Math.round(value);
  return numberFormatter.format(rounded);
};

const toNumber = (value) => {
  if (value === null || value === undefined) return 0;
  if (typeof value === "number") return value;
  const parsed = parseFloat(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const formatCurrency = (value) => `${formatNumber(toNumber(value))} VND`;

const PIE_COLORS = [
  "#6366f1",
  "#22c55e",
  "#f97316",
  "#0ea5e9",
  "#a855f7",
  "#ec4899",
  "#14b8a6",
];

const BangDieuKhien = () => {
  const now = new Date();
  const [month, setMonth] = useState(now.getMonth() + 1);
  const [year, setYear] = useState(now.getFullYear());
  const [message, setMessage] = useState("");
  const [loadingSummary, setLoadingSummary] = useState(false);
  const [loadingTransactions, setLoadingTransactions] = useState(false);
  const [loadingExpiring, setLoadingExpiring] = useState(false);
  const [loadingExpired, setLoadingExpired] = useState(false);

  const [overview, setOverview] = useState({
    doanhThuHomNay: 0,
    doanhThuThang: 0,
    tongGiaTriTon: 0,
    tongTonKho: 0,
    tongSanPham: 0,
    tongNhaCungCap: 0,
    sanPhamSapHet: 0,
    tongDonBan: 0,
    tongDonNhap: 0,
  });
  const [lowStockProducts, setLowStockProducts] = useState([]);
  const [categoryRevenue, setCategoryRevenue] = useState([]);
  const [flowByMonth, setFlowByMonth] = useState([]);
  const [expiringLots, setExpiringLots] = useState([]);
  const [expiredLots, setExpiredLots] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [expiringDays, setExpiringDays] = useState(60);

  const showMessage = useCallback((text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadSummaryData = useCallback(async () => {
    setLoadingSummary(true);
    try {
      const res = await ApiService.getDashboardSummary({
        month,
        year,
        lowLimit: 6,
      });
      const info = res.dashboardTongQuan || {};
      setOverview({
        doanhThuHomNay: toNumber(info.doanhThuHomNay),
        doanhThuThang: toNumber(info.doanhThuThang),
        tongGiaTriTon: toNumber(info.tongGiaTriTon),
        tongTonKho: toNumber(info.tongTonKho),
        tongSanPham: toNumber(info.tongSanPham),
        tongNhaCungCap: toNumber(info.tongNhaCungCap),
        sanPhamSapHet: toNumber(info.sanPhamSapHet),
        tongDonBan: toNumber(info.tongDonBan),
        tongDonNhap: toNumber(info.tongDonNhap),
      });
      setLowStockProducts(res.sanPhams || []);
      setCategoryRevenue(res.doanhThuTheoDanhMucs || []);
      setFlowByMonth(res.nhapXuatTheoThangs || []);
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể tải bảng điều khiển"
      );
    } finally {
      setLoadingSummary(false);
    }
  }, [month, year, showMessage]);

  const loadTransactions = useCallback(
    async (selectedMonth, selectedYear) => {
      setLoadingTransactions(true);
      try {
        const res = await ApiService.geTransactionsByMonthAndYear(
          selectedMonth,
          selectedYear
        );
        setTransactions(res.giaoDichs || []);
      } catch (error) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tải giao dịch theo thời gian"
        );
      } finally {
        setLoadingTransactions(false);
      }
    },
    [showMessage]
  );

  const loadExpiringLots = useCallback(
    async (days) => {
      setLoadingExpiring(true);
      try {
        const res = await ApiService.getExpiringProducts(days);
        const list = res.loHangs || [];
        setExpiringLots(list.slice(0, 5));
      } catch (error) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tải danh sách lô sắp hết hạn"
        );
      } finally {
        setLoadingExpiring(false);
      }
    },
    [showMessage]
  );

  const loadExpiredLots = useCallback(async () => {
    setLoadingExpired(true);
    try {
      const res = await ApiService.getExpiredLots();
      setExpiredLots(res.loHangs || []);
    } catch (error) {
      showMessage(
        error.response?.data?.message ||
          "Không thể tải danh sách lô hết hạn"
      );
    } finally {
      setLoadingExpired(false);
    }
  }, [showMessage]);

  const handleDiscardLot = async (lotId) => {
    try {
      await ApiService.discardLot(lotId);
      showMessage("Đã hủy lô hết hạn");
      loadExpiredLots();
      loadSummaryData();
    } catch (error) {
      showMessage(error.response?.data?.message || "Không thể hủy lô");
    }
  };

  useEffect(() => {
    loadSummaryData();
  }, [loadSummaryData]);

  useEffect(() => {
    loadTransactions(month, year);
  }, [loadTransactions, month, year]);

  useEffect(() => {
    loadExpiringLots(expiringDays);
  }, [loadExpiringLots, expiringDays]);

  useEffect(() => {
    loadExpiredLots();
  }, [loadExpiredLots]);

  const refreshAll = () => {
    loadSummaryData();
    loadTransactions(month, year);
    loadExpiringLots(expiringDays);
    loadExpiredLots();
  };

  const transactionSummary = useMemo(() => {
    return transactions.reduce(
      (acc, tx) => {
        const price = toNumber(tx.totalPrice);
        const units = toNumber(tx.totalProducts);
        acc.totalOrders += 1;
        acc.totalUnits += units;
        if (tx.transactionType === "SALE") {
          acc.salesValue += price;
          acc.salesOrders += 1;
        } else if (tx.transactionType === "PURCHASE") {
          acc.purchaseValue += price;
          acc.purchaseOrders += 1;
        }
        return acc;
      },
      {
        salesValue: 0,
        purchaseValue: 0,
        salesOrders: 0,
        purchaseOrders: 0,
        totalOrders: 0,
        totalUnits: 0,
      }
    );
  }, [transactions]);

  const chartData = useMemo(() => {
    const map = new Map();
    transactions.forEach((tx) => {
      const createdDate = new Date(tx.createdAt);
      if (Number.isNaN(createdDate.getTime())) {
        return;
      }
      const day = createdDate.getDate();
      const entry = map.get(day) || { day, label: `Ngày ${day}`, sale: 0, purchase: 0 };
      const amount = toNumber(tx.totalPrice);
      if (tx.transactionType === "SALE") {
        entry.sale += amount;
      } else if (tx.transactionType === "PURCHASE") {
        entry.purchase += amount;
      }
      map.set(day, entry);
    });
    return Array.from(map.values()).sort((a, b) => a.day - b.day);
  }, [transactions]);

  const formattedCategoryRevenue = useMemo(() => {
    return (categoryRevenue || []).map((item) => ({
      ...item,
      doanhThu: toNumber(item.doanhThu),
      tyLeDongGop:
        typeof item.tyLeDongGop === "number"
          ? item.tyLeDongGop
          : toNumber(item.tyLeDongGop),
    }));
  }, [categoryRevenue]);

  const flowChartData = useMemo(() => {
    return (flowByMonth || []).map((item) => ({
      ...item,
      tongNhap: toNumber(item.tongNhap),
      tongXuat: toNumber(item.tongXuat),
    }));
  }, [flowByMonth]);

  const recentTransactions = useMemo(() => {
    return [...transactions]
      .sort((a, b) => {
        const dateA = new Date(a.createdAt || 0).getTime();
        const dateB = new Date(b.createdAt || 0).getTime();
        return dateB - dateA;
      })
      .slice(0, 5);
  }, [transactions]);

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="dashboard-page">
        <div className="dashboard-heading">
          <div>
            <h1>Bảng điều khiển</h1>
            <p className="text-subtle">
              Tổng quan tồn kho, giao dịch và cảnh báo trong ngày
            </p>
          </div>
          <button
            type="button"
            onClick={refreshAll}
            disabled={loadingSummary || loadingTransactions}
          >
            Làm mới
          </button>
        </div>

        <div className="filter-section">
          <select
            id="month-select"
            value={month}
            onChange={(e) => setMonth(Number(e.target.value))}
          >
            {MONTH_OPTIONS.map((option) => (
              <option key={option.value} value={option.value}>
                {option.label}
              </option>
            ))}
          </select>
          <select
            id="year-select"
            value={year}
            onChange={(e) => setYear(Number(e.target.value))}
          >
            {Array.from({ length: 5 }, (_, idx) => year - 2 + idx).map(
              (yearOption) => (
                <option key={yearOption} value={yearOption}>
                  Năm {yearOption}
                </option>
              )
            )}
          </select>
        </div>

        {(loadingSummary || loadingTransactions) && (
          <p className="loading-text">Đang tải dữ liệu...</p>
        )}

        <div className="summary-grid">
          <div className="stat-card">
            <p className="stat-label">Doanh thu hôm nay</p>
            <p className="stat-value">
              {formatCurrency(overview.doanhThuHomNay)}
            </p>
            <p className="stat-sub">Đã trừ đơn trả hàng</p>
          </div>
          <div className="stat-card">
            <p className="stat-label">Doanh thu tháng này</p>
            <p className="stat-value">
              {formatCurrency(overview.doanhThuThang)}
            </p>
            <p className="stat-sub">
              Kỳ {month}/{year}
            </p>
          </div>
          <div className="stat-card">
            <p className="stat-label">Giá trị tồn kho</p>
            <p className="stat-value">
              {formatCurrency(overview.tongGiaTriTon)}
            </p>
            <p className="stat-sub">Giá × số lượng hiện có</p>
          </div>
          <div className="stat-card">
            <p className="stat-label">Tổng tồn kho</p>
            <p className="stat-value">{formatNumber(overview.tongTonKho)}</p>
            <p className="stat-sub">Tổng số lượng tất cả sản phẩm</p>
          </div>
          <div className="stat-card">
            <p className="stat-label">Tổng sản phẩm</p>
            <p className="stat-value">{formatNumber(overview.tongSanPham)}</p>
            <p className="stat-sub">Đang được quản lý</p>
          </div>
          <div className="stat-card">
            <p className="stat-label">Nhà cung cấp</p>
            <p className="stat-value">
              {formatNumber(overview.tongNhaCungCap)}
            </p>
            <p className="stat-sub">Đối tác nhập hàng</p>
          </div>
          <div className="stat-card warn">
            <p className="stat-label">Cảnh báo sắp hết hàng</p>
            <p className="stat-value">
              {formatNumber(overview.sanPhamSapHet)}
            </p>
            <p className="stat-sub">Sản phẩm dưới mức tồn tối thiểu</p>
          </div>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Doanh thu theo ngày</h2>
              <p className="text-subtle">
                So sanh gia tri nhap va ban trong Tháng {month}/{year}
              </p>
            </div>
            <span className="section-badge">
              {transactions.length} giao dịch
            </span>
          </div>

          {chartData.length === 0 ? (
            <p className="empty-text">Không có giao dịch trong kỳ này</p>
          ) : (
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={350}>
                <AreaChart data={chartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="label" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="purchase"
                    stackId="1"
                    stroke="#8884d8"
                    fill="#8884d8"
                    name="Nhap hang"
                  />
                  <Area
                    type="monotone"
                    dataKey="sale"
                    stackId="1"
                    stroke="#82ca9d"
                    fill="#82ca9d"
                    name="Ban hang"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        <div className="chart-row">
          <div className="dashboard-section">
            <div className="section-header">
              <div>
                <h3>Doanh thu theo danh mục</h3>
                <p className="text-subtle">
                  Phan bo doanh thu Tháng {month}/{year}
                </p>
              </div>
            </div>
            {formattedCategoryRevenue.length === 0 ? (
              <p className="empty-text">
                Chưa có dữ liệu doanh thu theo danh mục
              </p>
            ) : (
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={320}>
                  <PieChart>
                    <Pie
                      data={formattedCategoryRevenue}
                      dataKey="doanhThu"
                      nameKey="tenDanhMuc"
                      outerRadius={110}
                      label
                    >
                      {formattedCategoryRevenue.map((entry, index) => (
                        <Cell
                          key={entry.danhMucId ?? `cat-${index}`}
                          fill={PIE_COLORS[index % PIE_COLORS.length]}
                        />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value) => formatCurrency(value)} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            )}
            {formattedCategoryRevenue.length > 0 && (
              <div className="table-scroll mini">
                <table className="simple-table">
                  <thead>
                    <tr>
                      <th>Danh mục</th>
                      <th>Doanh thu</th>
                      <th>Tỷ lệ</th>
                    </tr>
                  </thead>
                  <tbody>
                    {formattedCategoryRevenue.map((item) => (
                      <tr key={item.danhMucId ?? item.tenDanhMuc}>
                        <td>{item.tenDanhMuc || "Khac"}</td>
                        <td>{formatCurrency(item.doanhThu)}</td>
                        <td>{(item.tyLeDongGop ?? 0).toFixed(2)}%</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

          <div className="dashboard-section">
            <div className="section-header">
              <div>
                <h3>Nhập vs xuất 6 tháng gần nhất</h3>
                <p className="text-subtle">
                  Thong ke den Tháng {month}/{year}
                </p>
              </div>
            </div>
            {flowChartData.length === 0 ? (
              <p className="empty-text">Chưa có dữ liệu 6 tháng gần đây</p>
            ) : (
              <div className="chart-container">
                <ResponsiveContainer width="100%" height={320}>
                  <BarChart data={flowChartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="nhan" />
                    <YAxis />
                    <Tooltip formatter={(value) => formatCurrency(value)} />
                    <Legend />
                    <Bar dataKey="tongNhap" fill="#3b82f6" name="Nhap hang" />
                    <Bar dataKey="tongXuat" fill="#22c55e" name="Ban hang" />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            )}
          </div>
        </div>

        <div className="cards-grid">
          <div className="dashboard-section">
            <div className="section-header">
              <h3>Sản phẩm sắp hết hàng</h3>
              <span className="section-badge">{lowStockProducts.length}</span>
            </div>
            <ul className="data-list">
              {lowStockProducts.length === 0 && (
                <li className="empty-text">Không có cảnh báo nào</li>
              )}
              {lowStockProducts.map((product) => (
                <li key={product.id} className="data-list-item">
                  <div>
                    <p className="item-title">{product.name}</p>
                    <p className="item-sub">
                      Ton: {product.stockQuantity ?? 0} | Min:{" "}
                      {product.minStock ?? 0}
                    </p>
                  </div>
                  <span className="item-badge danger">Cần nhập</span>
                </li>
              ))}
            </ul>
          </div>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Lô hàng sắp hết hạn</h3>
              <p className="text-subtle">
                Theo dõi trong {expiringDays} ngày tới
              </p>
            </div>
            <span className="section-badge">{expiringLots.length}</span>
          </div>

          <div className="mini-filter">
            {[30, 60, 90].map((day) => (
              <button
                key={day}
                type="button"
                className={day === expiringDays ? "active" : ""}
                onClick={() => setExpiringDays(day)}
                disabled={loadingExpiring && day === expiringDays}
              >
                {day} ngày
              </button>
            ))}
          </div>

          <ul className="data-list">
            {loadingExpiring && (
              <li className="empty-text">Đang tải danh sách lô hàng...</li>
            )}
            {!loadingExpiring && expiringLots.length === 0 && (
              <li className="empty-text">Không có lô hàng sắp hết hạn</li>
            )}
            {!loadingExpiring &&
              expiringLots.map((lot) => (
                <li key={lot.id} className="data-list-item">
                  <div>
                    <p className="item-title">{lot.sanPham?.name || "San pham"}</p>
                    <p className="item-sub">
                      Lo: {lot.lotNumber || "N/A"} | Ton:{" "}
                      {lot.quantityRemaining ?? 0}
                    </p>
                    <p className="item-sub">
                      Het han:{" "}
                      {lot.expiryDate
                        ? new Date(lot.expiryDate).toLocaleDateString()
                        : "Chua cap nhat"}
                    </p>
                  </div>
                  <span className="item-badge warning">Theo dõi</span>
                </li>
              ))}
          </ul>
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Lô hàng đã hết hạn</h3>
              <p className="text-subtle">
                Xử lý các lô hết hạn để giảm tồn kho
              </p>
            </div>
            <span className="section-badge">{expiredLots.length}</span>
          </div>

          {loadingExpired ? (
            <p className="empty-text">Đang tải lô hết hạn...</p>
          ) : (
            <ul className="data-list">
              {expiredLots.length === 0 && (
                <li className="empty-text">Không có lô hết hạn</li>
              )}
              {expiredLots.map((lot) => (
                <li key={lot.id} className="data-list-item">
                  <div>
                    <p className="item-title">{lot.sanPham?.name || "San pham"}</p>
                    <p className="item-sub">
                      Lo: {lot.lotNumber || "N/A"} | Ton:{" "}
                      {lot.quantityRemaining ?? 0}
                    </p>
                    <p className="item-sub">
                      Het han:{" "}
                      {lot.expiryDate
                        ? new Date(lot.expiryDate).toLocaleDateString()
                        : "Chua cap nhat"}
                    </p>
                  </div>
                  <button type="button" onClick={() => handleDiscardLot(lot.id)}>
                    Hủy lô
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h3>Thống kê giao dịch tháng này</h3>
              <p className="text-subtle">
                Tong don: {transactionSummary.totalOrders} | Tong so luong:{" "}
                {formatNumber(transactionSummary.totalUnits)}
              </p>
            </div>
          </div>

          <div className="summary-grid mini">
            <div className="stat-card">
              <p className="stat-label">Giá trị bán</p>
              <p className="stat-value">
                {formatNumber(transactionSummary.salesValue)} VND
              </p>
              <p className="stat-sub">
                Don hang: {transactionSummary.salesOrders}
              </p>
            </div>
            <div className="stat-card">
              <p className="stat-label">Giá trị nhập</p>
              <p className="stat-value">
                {formatNumber(transactionSummary.purchaseValue)} VND
              </p>
              <p className="stat-sub">
                Don nhap: {transactionSummary.purchaseOrders}
              </p>
            </div>
          </div>

          <table className="simple-table">
            <thead>
              <tr>
                <th>Ngày</th>
                <th>Loại</th>
                <th>Giá trị</th>
                <th>Số lượng</th>
                <th>Trạng thái</th>
              </tr>
            </thead>
            <tbody>
              {recentTransactions.length === 0 && (
                <tr>
                  <td colSpan="5" className="empty-text">
                    Chưa có giao dịch gần đây
                  </td>
                </tr>
              )}
              {recentTransactions.map((tx) => (
                <tr key={tx.id}>
                  <td>
                    {tx.createdAt
                      ? new Date(tx.createdAt).toLocaleDateString()
                      : "--"}
                  </td>
                  <td>{hienThiLoaiGiaoDich(tx.transactionType)}</td>
                  <td>{formatNumber(toNumber(tx.totalPrice))} VND</td>
                  <td>{tx.totalProducts ?? 0}</td>
                  <td>{hienThiTrangThaiGiaoDich(tx.status)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </Layout>
  );
};

export default BangDieuKhien;





