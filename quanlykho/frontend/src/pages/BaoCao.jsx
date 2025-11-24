import React, {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import {
  Area,
  AreaChart,
  CartesianGrid,
  Legend,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from "recharts";
import Layout from "../component/Layout";
import ApiService from "../service/ApiService";

const numberFormatter = new Intl.NumberFormat("en-US", {
  maximumFractionDigits: 0,
});

const formatNumber = (value) => {
  if (!value) return "0";
  return numberFormatter.format(Math.round(value));
};

const toNumber = (value) => {
  if (value === null || value === undefined) return 0;
  if (typeof value === "number") return value;
  const parsed = parseFloat(value);
  return Number.isNaN(parsed) ? 0 : parsed;
};

const formatCurrency = (value) => `${formatNumber(toNumber(value))} VND`;
const formatDateValue = (date) => date.toISOString().split("T")[0];

const BaoCao = () => {
  const nowRef = useRef(new Date());
  const now = nowRef.current;
  const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
  const defaultFromDate = formatDateValue(startOfMonth);
  const defaultToDate = formatDateValue(now);

  const [message, setMessage] = useState("");

  const [revenueForm, setRevenueForm] = useState({
    fromDate: defaultFromDate,
    toDate: defaultToDate,
    interval: "DAY",
  });
  const [revenueFilters, setRevenueFilters] = useState(revenueForm);
  const [loadingRevenue, setLoadingRevenue] = useState(false);
  const [revenueSummary, setRevenueSummary] = useState({
    tongDoanhThu: 0,
    tongChiPhi: 0,
    loiNhuan: 0,
    soDonBan: 0,
    soDonNhap: 0,
  });
  const [revenueSeries, setRevenueSeries] = useState([]);

  const [loadingValuation, setLoadingValuation] = useState(false);
  const [inventoryValuation, setInventoryValuation] = useState({
    totalValue: 0,
    totalQuantity: 0,
    productCount: 0,
  });
  const [categoryValuation, setCategoryValuation] = useState([]);
  const [warehouseValuation, setWarehouseValuation] = useState([]);

  const [categories, setCategories] = useState([]);
  const [stockMovementForm, setStockMovementForm] = useState({
    fromDate: defaultFromDate,
    toDate: defaultToDate,
    categoryId: "",
  });
  const [stockMovementFilters, setStockMovementFilters] = useState(
    stockMovementForm
  );
  const [loadingStockMovement, setLoadingStockMovement] = useState(false);
  const [stockMovementRows, setStockMovementRows] = useState([]);
  const [stockMovementCategoryRows, setStockMovementCategoryRows] = useState(
    []
  );
  const [stockMovementSummary, setStockMovementSummary] = useState({
    tonDauKy: 0,
    tongNhap: 0,
    tongXuat: 0,
    tonCuoiKy: 0,
  });

  const [agingBucket, setAgingBucket] = useState(30);
  const [loadingAging, setLoadingAging] = useState(false);
  const [agingRows, setAgingRows] = useState([]);
  const [agingSummary, setAgingSummary] = useState({
    tongLo: 0,
    tren30Ngay: 0,
    tren60Ngay: 0,
    tren90Ngay: 0,
    tren180Ngay: 0,
  });

  const [abcForm, setAbcForm] = useState({
    fromDate: defaultFromDate,
    toDate: defaultToDate,
  });
  const [abcFilters, setAbcFilters] = useState(abcForm);
  const [loadingABC, setLoadingABC] = useState(false);
  const [abcRows, setAbcRows] = useState([]);
  const [abcSummary, setAbcSummary] = useState({
    tongSanPham: 0,
    nhomA: 0,
    nhomB: 0,
    nhomC: 0,
    tyLeDoanhThuA: 0,
    tyLeDoanhThuB: 0,
    tyLeDoanhThuC: 0,
  });

  const [bestSellerForm, setBestSellerForm] = useState({
    fromDate: defaultFromDate,
    toDate: defaultToDate,
    limit: 10,
    metric: "quantity",
  });
  const [bestSellerFilters, setBestSellerFilters] = useState(bestSellerForm);
  const [loadingBestSeller, setLoadingBestSeller] = useState(false);
  const [bestSellerRows, setBestSellerRows] = useState([]);

  const [supplierForm, setSupplierForm] = useState({
    fromDate: defaultFromDate,
    toDate: defaultToDate,
    limit: 10,
  });
  const [supplierFilters, setSupplierFilters] = useState(supplierForm);
  const [loadingSupplier, setLoadingSupplier] = useState(false);
  const [supplierRows, setSupplierRows] = useState([]);

  const showMessage = useCallback((text) => {
    setMessage(text);
    setTimeout(() => setMessage(""), 4000);
  }, []);

  const loadRevenueReport = useCallback(
    async (filtersParam) => {
      const params = filtersParam || revenueFilters;
      if (!params.fromDate || !params.toDate) {
        showMessage("Vui lòng chon Khoảng thoi gian hop le");
        return;
      }
      setLoadingRevenue(true);
      try {
        const response = await ApiService.getRevenueReport({
          from: params.fromDate,
          to: params.toDate,
          interval: params.interval,
        });
        if (response.status === 200) {
          setRevenueSummary(
            response.baoCaoDoanhThuTổng || {
              tongDoanhThu: 0,
              tongChiPhi: 0,
              loiNhuan: 0,
              soDonBan: 0,
              soDonNhap: 0,
            }
          );
          setRevenueSeries(response.doanhThuTheoKys || []);
        } else {
          showMessage(response.message || "Không thể tải báo cáo Doanh thu");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải báo cáo Doanh thu"
        );
      } finally {
        setLoadingRevenue(false);
      }
    },
    [revenueFilters, showMessage]
  );

  const loadInventoryValuation = useCallback(async () => {
    setLoadingValuation(true);
    try {
      const [summaryRes, categoryRes, warehouseRes] = await Promise.all([
        ApiService.getInventoryValuationSummary(),
        ApiService.getInventoryValuationByCategory(),
        ApiService.getInventoryValuationByWarehouse(),
      ]);
      if (summaryRes.status === 200 && summaryRes.baoCaoTonKhoTong) {
        const data = summaryRes.baoCaoTonKhoTong;
        setInventoryValuation({
          totalValue: toNumber(data.tongGiaTri),
          totalQuantity: data.tongSoLuong || 0,
          productCount: data.soSanPham || 0,
        });
      }
      if (categoryRes.status === 200) {
        setCategoryValuation(categoryRes.giaTriTonTheoDanhMuc || []);
      }
      if (warehouseRes.status === 200) {
        setWarehouseValuation(warehouseRes.giaTriTonTheoKho || []);
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể tải báo cáo tồn kho"
      );
    } finally {
      setLoadingValuation(false);
    }
  }, [showMessage]);

  const loadCategories = useCallback(async () => {
    try {
      const response = await ApiService.getAllCategory();
      if (response.status === 200) {
        setCategories(response.categories || []);
      }
    } catch (error) {
      showMessage(
        error.response?.data?.message || "Không thể tải danh sách danh mục"
      );
    }
  }, [showMessage]);

  const loadStockMovementReport = useCallback(
    async (filtersParam) => {
      const params = filtersParam || stockMovementFilters;
      if (!params.fromDate || !params.toDate) {
        showMessage("Vui lòng chon Khoảng thoi gian hop le");
        return;
      }
      setLoadingStockMovement(true);
      try {
        const response = await ApiService.getStockMovementReport({
          from: params.fromDate,
          to: params.toDate,
          categoryId: params.categoryId,
        });
        setStockMovementRows(response.baoCaoXuatNhapTon || []);
        setStockMovementCategoryRows(
          response.baoCaoXuatNhapTonTheoDanhMuc || []
        );
        setStockMovementSummary(
          response.baoCaoXuatNhapTonTong || {
            tonDauKy: 0,
            tongNhap: 0,
            tongXuat: 0,
            tonCuoiKy: 0,
          }
        );
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải báo cáo xuất nhập tồn"
        );
      } finally {
        setLoadingStockMovement(false);
      }
    },
    [stockMovementFilters, showMessage]
  );

  const loadAgingReport = useCallback(
    async (bucketDays) => {
      const targetBucket = bucketDays ?? agingBucket;
      setLoadingAging(true);
      try {
        const response = await ApiService.getAgingInventoryReport(targetBucket);
        if (response.status === 200) {
          setAgingRows(response.hangTonLaus || []);
          setAgingSummary(
            response.thongKeHangTonLau || {
              tongLo: 0,
              tren30Ngay: 0,
              tren60Ngay: 0,
              tren90Ngay: 0,
              tren180Ngay: 0,
            }
          );
        } else {
          showMessage(response.message || "Không thể tải báo cáo tồn lâu");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải báo cáo tồn lâu"
        );
      } finally {
        setLoadingAging(false);
      }
    },
    [agingBucket, showMessage]
  );

  const loadAbcReport = useCallback(
    async (filtersParam) => {
      const params = filtersParam || abcFilters;
      setLoadingABC(true);
      try {
        const response = await ApiService.getABCReport({
          from: params.fromDate,
          to: params.toDate,
        });
        if (response.status === 200) {
          setAbcRows(response.baoCaoABC || []);
          setAbcSummary(
            response.thongKeABC || {
              tongSanPham: 0,
              nhomA: 0,
              nhomB: 0,
              nhomC: 0,
              tyLeDoanhThuA: 0,
              tyLeDoanhThuB: 0,
              tyLeDoanhThuC: 0,
            }
          );
        } else {
          showMessage(response.message || "Không thể tải báo cáo ABC");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải báo cáo ABC"
        );
      } finally {
        setLoadingABC(false);
      }
    },
    [abcFilters, showMessage]
  );

  const loadBestSellers = useCallback(
    async (filtersParam) => {
      const params = filtersParam || bestSellerFilters;
      setLoadingBestSeller(true);
      try {
        const response = await ApiService.getBestSellers({
          from: params.fromDate,
          to: params.toDate,
          limit: params.limit,
          metric: params.metric,
        });
        if (response.status === 200) {
          setBestSellerRows(response.sanPhamBanChay || []);
        } else {
          showMessage(response.message || "Không thể tải top sản phẩm");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message || "Không thể tải top sản phẩm"
        );
      } finally {
        setLoadingBestSeller(false);
      }
    },
    [bestSellerFilters, showMessage]
  );

  const loadSupplierReport = useCallback(
    async (filtersParam) => {
      const params = filtersParam || supplierFilters;
      setLoadingSupplier(true);
      try {
        const response = await ApiService.getSupplierReport({
          from: params.fromDate,
          to: params.toDate,
          limit: params.limit,
        });
        if (response.status === 200) {
          setSupplierRows(response.nhaCungCapBaoCao || []);
        } else {
          showMessage(response.message || "Không thể tải báo cáo nhà cung cấp");
        }
      } catch (error) {
        showMessage(
          error.response?.data?.message ||
            "Không thể tải báo cáo nhà cung cấp"
        );
      } finally {
        setLoadingSupplier(false);
      }
    },
    [supplierFilters, showMessage]
  );

  useEffect(() => {
    loadRevenueReport(revenueFilters);
  }, [loadRevenueReport, revenueFilters]);

  useEffect(() => {
    loadInventoryValuation();
  }, [loadInventoryValuation]);

  useEffect(() => {
    loadCategories();
  }, [loadCategories]);

  useEffect(() => {
    loadStockMovementReport(stockMovementFilters);
  }, [loadStockMovementReport, stockMovementFilters]);

  useEffect(() => {
    loadAgingReport(agingBucket);
  }, [agingBucket, loadAgingReport]);

  useEffect(() => {
    loadAbcReport(abcFilters);
  }, [abcFilters, loadAbcReport]);

  useEffect(() => {
    loadBestSellers(bestSellerFilters);
  }, [bestSellerFilters, loadBestSellers]);

  useEffect(() => {
    loadSupplierReport(supplierFilters);
  }, [loadSupplierReport, supplierFilters]);

  const hasStockMovementFilters = useMemo(() => {
    return (
      stockMovementFilters.fromDate !== defaultFromDate ||
      stockMovementFilters.toDate !== defaultToDate ||
      (stockMovementFilters.categoryId &&
        stockMovementFilters.categoryId !== "")
    );
  }, [stockMovementFilters, defaultFromDate, defaultToDate]);

  const revenueChartData = useMemo(() => {
    return (revenueSeries || []).map((item) => ({
      label: item.nhan,
      revenue: toNumber(item.doanhThu),
      cost: toNumber(item.chiPhi),
      profit: toNumber(item.loiNhuan),
    }));
  }, [revenueSeries]);

  const refreshAll = () => {
    loadRevenueReport(revenueFilters);
    loadInventoryValuation();
    loadStockMovementReport(stockMovementFilters);
    loadAgingReport(agingBucket);
    loadAbcReport(abcFilters);
    loadBestSellers(bestSellerFilters);
    loadSupplierReport(supplierFilters);
  };

  return (
    <Layout>
      {message && <div className="message">{message}</div>}
      <div className="dashboard-page bao-cao-page">
        <div className="dashboard-heading">
          <div>
            <h1>Báo cáo tổng hợp</h1>
            <p className="text-subtle">
              Theo dõi tồn kho, doanh thu, chi phí và trạng thái kho hàng của bạn
              trong một màn hình trực quan.
            </p>
          </div>
          <button
            type="button"
            className="btn btn-outline"
            onClick={refreshAll}
          >
            Làm mới
          </button>
        </div>

        {/* Revenue */}
        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Doanh thu & Lợi nhuận</h2>
              <p className="text-subtle">
                Tổng doanh thu bán hàng, chi phí nhập và lợi nhuận gộp trong
                khoảng thời gian chọn lọc
              </p>
            </div>
          </div>

          <form
            className="stock-movement-filter"
            onSubmit={(event) => {
              event.preventDefault();
              setRevenueFilters(revenueForm);
            }}
          >
            <div className="filter-field">
              <label htmlFor="rev-from">Từ ngày</label>
              <input
                id="rev-from"
                type="date"
                value={revenueForm.fromDate}
                onChange={(e) =>
                  setRevenueForm((prev) => ({
                    ...prev,
                    fromDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="rev-to">Đến ngày</label>
              <input
                id="rev-to"
                type="date"
                value={revenueForm.toDate}
                onChange={(e) =>
                  setRevenueForm((prev) => ({
                    ...prev,
                    toDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="rev-interval">Khoảng</label>
              <select
                id="rev-interval"
                value={revenueForm.interval}
                onChange={(e) =>
                  setRevenueForm((prev) => ({
                    ...prev,
                    interval: e.target.value,
                  }))
                }
              >
                <option value="DAY">Ngày</option>
                <option value="MONTH">Tháng</option>
              </select>
            </div>
            <div className="filter-actions">
              <button
                type="submit"
                className="btn btn-primary"
                disabled={loadingRevenue}
              >
                Áp dụng
              </button>
            </div>
          </form>
          <div className="summary-grid mini">
            <div className="stat-card">
              <p className="stat-label">Tổng doanh thu</p>
              <p className="stat-value">
                {formatCurrency(revenueSummary.tongDoanhThu || 0)}
              </p>
              <p className="stat-sub">
                Đơn bán: {revenueSummary.soDonBan ?? 0}
              </p>
            </div>
            <div className="stat-card">
              <p className="stat-label">Tổng chi phí</p>
              <p className="stat-value">
                {formatCurrency(revenueSummary.tongChiPhi || 0)}
              </p>
              <p className="stat-sub">
                Đơn nhập: {revenueSummary.soDonNhap ?? 0}
              </p>
            </div>
            <div className="stat-card">
              <p className="stat-label">Lợi nhuận gộp</p>
              <p className="stat-value">
                {formatCurrency(revenueSummary.loiNhuan || 0)}
              </p>
            </div>
          </div>

          {loadingRevenue ? (
            <p className="empty-text">Đang tai bieudo...</p>
          ) : (
            <div className="chart-container">
              <ResponsiveContainer width="100%" height={320}>
                <AreaChart data={revenueChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="label" />
                  <YAxis />
                  <Tooltip />
                  <Legend />
                  <Area
                    type="monotone"
                    dataKey="revenue"
                    stroke="#60a5fa"
                    fill="#bfdbfe"
                    name="Doanh thu"
                  />
                  <Area
                    type="monotone"
                    dataKey="cost"
                    stroke="#f87171"
                    fill="#fecaca"
                    name="Chi phí"
                  />
                  <Area
                    type="monotone"
                    dataKey="profit"
                    stroke="#34d399"
                    fill="#bbf7d0"
                    name="Lợi nhuận"
                  />
                </AreaChart>
              </ResponsiveContainer>
            </div>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Nhà cung cấp theo giá trị mua</h2>
              <p className="text-subtle">
                Tổng giá trị nhập và số giao dịch theo từng đối tác
              </p>
            </div>
            <span className="section-badge">
              {supplierRows.length} đối tác
            </span>
          </div>

          <form
            className="stock-movement-filter"
            onSubmit={(event) => {
              event.preventDefault();
              setSupplierFilters(supplierForm);
            }}
          >
            <div className="filter-field">
              <label htmlFor="sup-from">Từ ngày</label>
              <input
                id="sup-from"
                type="date"
                value={supplierForm.fromDate}
                onChange={(e) =>
                  setSupplierForm((prev) => ({ ...prev, fromDate: e.target.value }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="sup-to">Đến ngày</label>
              <input
                id="sup-to"
                type="date"
                value={supplierForm.toDate}
                onChange={(e) =>
                  setSupplierForm((prev) => ({ ...prev, toDate: e.target.value }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="sup-limit">Số lượng top</label>
              <input
                id="sup-limit"
                type="number"
                min="1"
                max="50"
                value={supplierForm.limit}
                onChange={(e) =>
                  setSupplierForm((prev) => ({
                    ...prev,
                    limit: Number(e.target.value),
                  }))
                }
              />
            </div>
            <div className="filter-actions">
              <button type="submit" disabled={loadingSupplier}>
                Áp dụng
              </button>
              <button
                type="button"
                onClick={() => {
                  const reset = {
                    fromDate: defaultFromDate,
                    toDate: defaultToDate,
                    limit: 10,
                  };
                  setSupplierForm(reset);
                  setSupplierFilters(reset);
                }}
                disabled={loadingSupplier}
              >
                Đặt lại
              </button>
            </div>
          </form>

          {loadingSupplier ? (
            <p className="empty-text">
              Đang tải báo cáo nhà cung cấp...
            </p>
          ) : (
            <div className="table-scroll">
              <table className="simple-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Nhà cung cấp</th>
                    <th>Liên hệ</th>
                    <th>Số giao dịch</th>
                    <th>Tổng giá trị</th>
                    <th>% dong gop</th>
                  </tr>
                </thead>
                <tbody>
                  {supplierRows.length === 0 && (
                    <tr>
                      <td colSpan="6" className="empty-text">
                        Chưa có dữ liệu
                      </td>
                    </tr>
                  )}
                  {supplierRows.map((item) => (
                    <tr key={item.nhaCungCapId}>
                      <td>{item.xepHang ?? "-"}</td>
                      <td>{item.tenNhaCungCap}</td>
                      <td>
                        {item.phone || item.email || item.contactInfo || "--"}
                      </td>
                      <td>{formatNumber(item.soGiaoDich || 0)}</td>
                      <td>{formatCurrency(item.tongGiaTri || 0)}</td>
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
              <h2>Giá trị tồn kho</h2>
              <p className="text-subtle">
                Tổng giá trị và các danh mục/kho đóng góp nhiều nhất
              </p>
            </div>
            <button
              type="button"
              onClick={loadInventoryValuation}
              disabled={loadingValuation}
            >
              Làm mới
            </button>
          </div>

          {loadingValuation ? (
            <p className="empty-text">Đang tinh toan...</p>
          ) : (
            <>
              <div className="summary-grid mini">
                <div className="stat-card">
                  <p className="stat-label">Tổng giá trị</p>
                  <p className="stat-value">
                    {formatCurrency(inventoryValuation.totalValue || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Tổng số lượng</p>
                  <p className="stat-value">
                    {formatNumber(inventoryValuation.totalQuantity || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Sản phẩm đang quản lý</p>
                  <p className="stat-value">
                    {inventoryValuation.productCount || 0}
                  </p>
                </div>
              </div>

              <div className="cards-grid">
                <div className="stat-card">
                  <p className="stat-label">Danh muc Giá trị cao</p>
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>Danh muc</th>
                        <th>Số lượng</th>
                        <th>Giá trị</th>
                      </tr>
                    </thead>
                    <tbody>
                      {categoryValuation.length === 0 && (
                        <tr>
                          <td colSpan="3" className="empty-text">
                            Chưa có dữ liệu
                          </td>
                        </tr>
                      )}
                      {categoryValuation.slice(0, 5).map((item, index) => {
                        const keyValue =
                          item.danhMucId !== undefined &&
                          item.danhMucId !== null
                            ? item.danhMucId
                            : `cat-${index}`;
                        return (
                          <tr key={keyValue}>
                            <td>{item.tenDanhMuc || "Khac"}</td>
                            <td>{formatNumber(item.tongSoLuong || 0)}</td>
                            <td>{formatCurrency(item.giaTriTon || 0)}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>

                <div className="stat-card">
                  <p className="stat-label">Kho Giá trị cao</p>
                  <table className="simple-table">
                    <thead>
                      <tr>
                        <th>Kho</th>
                        <th>Số lượng</th>
                        <th>Giá trị</th>
                      </tr>
                    </thead>
                    <tbody>
                      {warehouseValuation.length === 0 && (
                        <tr>
                          <td colSpan="3" className="empty-text">
                            Chưa có dữ liệu
                          </td>
                        </tr>
                      )}
                      {warehouseValuation.slice(0, 5).map((item, index) => {
                        const keyValue =
                          item.khoId !== undefined && item.khoId !== null
                            ? item.khoId
                            : `warehouse-${index}`;
                        return (
                          <tr key={keyValue}>
                            <td>{item.tenKho || "Kho"}</td>
                            <td>{formatNumber(item.tongSoLuong || 0)}</td>
                            <td>{formatCurrency(item.giaTriTon || 0)}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              </div>
            </>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Báo cáo Xuất nhập tồn</h2>
              <p className="text-subtle">
                Tồn dau + Nhập - Xuất = Tồn cuối kỳ
              </p>
            </div>
            <span className="section-badge">
              {stockMovementRows.length} hang muc
            </span>
          </div>

          <form
            className="stock-movement-filter"
            onSubmit={(event) => {
              event.preventDefault();
              setStockMovementFilters(stockMovementForm);
            }}
          >
            <div className="filter-field">
              <label htmlFor="stock-from">Từ ngày</label>
              <input
                id="stock-from"
                type="date"
                name="fromDate"
                value={stockMovementForm.fromDate}
                onChange={(e) =>
                  setStockMovementForm((prev) => ({
                    ...prev,
                    fromDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="stock-to">Đến ngày</label>
              <input
                id="stock-to"
                type="date"
                name="toDate"
                value={stockMovementForm.toDate}
                onChange={(e) =>
                  setStockMovementForm((prev) => ({
                    ...prev,
                    toDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="stock-category">Danh muc</label>
              <select
                id="stock-category"
                name="categoryId"
                value={stockMovementForm.categoryId}
                onChange={(e) =>
                  setStockMovementForm((prev) => ({
                    ...prev,
                    categoryId: e.target.value,
                  }))
                }
              >
                <option value="">Tat ca</option>
                {categories.map((category) => (
                  <option key={category.id} value={category.id}>
                    {category.Năme}
                  </option>
                ))}
              </select>
            </div>
            <div className="filter-actions">
              <button type="submit" disabled={loadingStockMovement}>
                Áp dụng
              </button>
              <button
                type="button"
                onClick={() => {
                  const resetFilters = {
                    fromDate: defaultFromDate,
                    toDate: defaultToDate,
                    categoryId: "",
                  };
                  setStockMovementForm(resetFilters);
                  setStockMovementFilters(resetFilters);
                }}
                disabled={!hasStockMovementFilters || loadingStockMovement}
              >
                Dat lai
              </button>
            </div>
          </form>

          {loadingStockMovement ? (
            <p className="empty-text">Đang tai Báo cáo Xuất nhập tồn...</p>
          ) : (
            <>
              <div className="summary-grid mini">
                <div className="stat-card">
                  <p className="stat-label">Tồn đầu kỳ</p>
                  <p className="stat-value">
                    {formatNumber(stockMovementSummary.tonDauKy || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Tổng Nhập</p>
                  <p className="stat-value">
                    {formatNumber(stockMovementSummary.tongNhap || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Tổng Xuất</p>
                  <p className="stat-value">
                    {formatNumber(stockMovementSummary.tongXuat || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Tồn cuối kỳ</p>
                  <p className="stat-value">
                    {formatNumber(stockMovementSummary.tonCuoiKy || 0)}
                  </p>
                </div>
              </div>

              <div className="stock-movement-grid">
                <div className="table-wrapper">
                  <div className="table-title">Theo Sản phẩm</div>
                  <div className="table-scroll">
                    <table className="simple-table">
                      <thead>
                        <tr>
                          <th>Sản phẩm</th>
                          <th>SKU</th>
                          <th>Danh muc</th>
                          <th>Tồn dau</th>
                          <th>Nhập</th>
                          <th>Xuất</th>
                          <th>Tồn cuoi</th>
                        </tr>
                      </thead>
                      <tbody>
                        {stockMovementRows.length === 0 && (
                          <tr>
                            <td colSpan="7" className="empty-text">
                              Chưa có dữ liệu trong ky nay
                            </td>
                          </tr>
                        )}
                        {stockMovementRows.map((item, index) => {
                          const keyValue =
                            item.sanPhamId !== undefined &&
                            item.sanPhamId !== null
                              ? item.sanPhamId
                              : `sp-${index}`;
                          return (
                            <tr key={keyValue}>
                              <td>{item.tenSanPham}</td>
                              <td>{item.sku}</td>
                              <td>{item.tenDanhMuc || "Khac"}</td>
                              <td>{formatNumber(item.tonDauKy || 0)}</td>
                              <td>{formatNumber(item.tongNhap || 0)}</td>
                              <td>{formatNumber(item.tongXuat || 0)}</td>
                              <td>{formatNumber(item.tonCuoiKy || 0)}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>

                <div className="table-wrapper">
                  <div className="table-title">Theo danh muc</div>
                  <div className="table-scroll">
                    <table className="simple-table">
                      <thead>
                        <tr>
                          <th>Danh muc</th>
                          <th>Tồn dau</th>
                          <th>Nhập</th>
                          <th>Xuất</th>
                          <th>Tồn cuoi</th>
                        </tr>
                      </thead>
                      <tbody>
                        {stockMovementCategoryRows.length === 0 && (
                          <tr>
                            <td colSpan="5" className="empty-text">
                              Chưa có dữ liệu Tổng hop
                            </td>
                          </tr>
                        )}
                        {stockMovementCategoryRows.map((item, index) => {
                          const keyValue =
                            item.danhMucId !== undefined &&
                            item.danhMucId !== null
                              ? item.danhMucId
                              : `cat-${index}`;
                          return (
                            <tr key={keyValue}>
                              <td>{item.tenDanhMuc || "Khac"}</td>
                              <td>{formatNumber(item.tonDauKy || 0)}</td>
                              <td>{formatNumber(item.tongNhap || 0)}</td>
                              <td>{formatNumber(item.tongXuat || 0)}</td>
                              <td>{formatNumber(item.tonCuoiKy || 0)}</td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
            </>
          )}
        </div>
        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Hang Tồn lau (FEFO)</h2>
              <p className="text-subtle">
                Lo Tồn > {agingBucket} ngay can uu tien xu ly
              </p>
            </div>
            <span className="section-badge">{agingRows.length} lo</span>
          </div>

          <div className="mini-filter">
            {[30, 60, 90, 180].map((bucket) => (
              <button
                key={bucket}
                type="button"
                className={bucket === agingBucket ? "active" : ""}
                onClick={() => setAgingBucket(bucket)}
                disabled={loadingAging && bucket === agingBucket}
              >
                {bucket}+ ngay
              </button>
            ))}
          </div>

          {loadingAging ? (
            <p className="empty-text">Đang tai danh sach Tồn lau...</p>
          ) : (
            <>
              <div className="summary-grid mini">
                <div className="stat-card">
                  <p className="stat-label">Tổng lo Đang theo doi</p>
                  <p className="stat-value">
                    {formatNumber(agingSummary.tongLo || 0)}
                  </p>
                </div>
                <div className="stat-card warn">
                  <p className="stat-label">>=30 ngay</p>
                  <p className="stat-value">
                    {formatNumber(agingSummary.tren30Ngay || 0)}
                  </p>
                </div>
                <div className="stat-card warn">
                  <p className="stat-label">>=60 ngay</p>
                  <p className="stat-value">
                    {formatNumber(agingSummary.tren60Ngay || 0)}
                  </p>
                </div>
                <div className="stat-card warn">
                  <p className="stat-label">>=90 ngay</p>
                  <p className="stat-value">
                    {formatNumber(agingSummary.tren90Ngay || 0)}
                  </p>
                </div>
                <div className="stat-card warn">
                  <p className="stat-label">>=180 ngay</p>
                  <p className="stat-value">
                    {formatNumber(agingSummary.tren180Ngay || 0)}
                  </p>
                </div>
              </div>

              <div className="table-scroll">
                <table className="simple-table">
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th>Lo</th>
                      <th>Danh muc</th>
                      <th>Ngay Nhập</th>
                      <th>So Ngày tồn</th>
                      <th>Số lượng</th>
                      <th>Nhom</th>
                    </tr>
                  </thead>
                  <tbody>
                    {agingRows.length === 0 && (
                      <tr>
                        <td colSpan="7" className="empty-text">
                          Không có lô hàng nào trong nhóm này
                        </td>
                      </tr>
                    )}
                    {agingRows.map((item, index) => {
                      const keyValue =
                        item.loHangId !== undefined && item.loHangId !== null
                          ? item.loHangId
                          : `aging-${index}`;
                      return (
                        <tr key={keyValue}>
                          <td>{item.tenSanPham || "Sản phẩm"}</td>
                          <td>{item.soLo || "--"}</td>
                          <td>{item.tenDanhMuc || "Khac"}</td>
                          <td>
                            {item.ngayNhap
                              ? new Date(item.ngayNhap).toLocaleDateString()
                              : "--"}
                          </td>
                          <td>{item.soNgayTồn ?? "--"}</td>
                          <td>{formatNumber(item.soLuongConLai || 0)}</td>
                          <td>{item.nhomTuoi || ""}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Phân tích ABC</h2>
              <p className="text-subtle">
                Xac dinh Sản phẩm dong gop Doanh thu cao nhat
              </p>
            </div>
            <span className="section-badge">{abcRows.length} Sản phẩm</span>
          </div>

          <form
            className="stock-movement-filter"
            onSubmit={(event) => {
              event.preventDefault();
              setAbcFilters(abcForm);
            }}
          >
            <div className="filter-field">
              <label htmlFor="abc-from">Từ ngày</label>
              <input
                id="abc-from"
                type="date"
                value={abcForm.fromDate}
                onChange={(e) =>
                  setAbcForm((prev) => ({ ...prev, fromDate: e.target.value }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="abc-to">Đến ngày</label>
              <input
                id="abc-to"
                type="date"
                value={abcForm.toDate}
                onChange={(e) =>
                  setAbcForm((prev) => ({ ...prev, toDate: e.target.value }))
                }
              />
            </div>
            <div className="filter-actions">
              <button type="submit" disabled={loadingABC}>
                Áp dụng
              </button>
              <button
                type="button"
                onClick={() => {
                  const reset = {
                    fromDate: defaultFromDate,
                    toDate: defaultToDate,
                  };
                  setAbcForm(reset);
                  setAbcFilters(reset);
                }}
                disabled={loadingABC}
              >
                Dat lai
              </button>
            </div>
          </form>

          {loadingABC ? (
            <p className="empty-text">Đang tinh ABC...</p>
          ) : (
            <>
              <div className="summary-grid mini">
                <div className="stat-card">
                  <p className="stat-label">Tổng Sản phẩm</p>
                  <p className="stat-value">
                    {formatNumber(abcSummary.tongSanPham || 0)}
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Nhom A</p>
                  <p className="stat-value">
                    {formatNumber(abcSummary.nhomA || 0)}
                  </p>
                  <p className="stat-sub">
                    {abcSummary.tyLeDoanhThuA?.toFixed(1) ?? 0}% Doanh thu
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Nhom B</p>
                  <p className="stat-value">
                    {formatNumber(abcSummary.nhomB || 0)}
                  </p>
                  <p className="stat-sub">
                    {abcSummary.tyLeDoanhThuB?.toFixed(1) ?? 0}% Doanh thu
                  </p>
                </div>
                <div className="stat-card">
                  <p className="stat-label">Nhom C</p>
                  <p className="stat-value">
                    {formatNumber(abcSummary.nhomC || 0)}
                  </p>
                  <p className="stat-sub">
                    {abcSummary.tyLeDoanhThuC?.toFixed(1) ?? 0}% Doanh thu
                  </p>
                </div>
              </div>

              <div className="table-scroll">
                <table className="simple-table">
                  <thead>
                    <tr>
                      <th>Sản phẩm</th>
                      <th>SKU</th>
                      <th>Danh muc</th>
                      <th>Doanh thu</th>
                      <th>%</th>
                      <th>Tich luy</th>
                      <th>Nhom</th>
                    </tr>
                  </thead>
                  <tbody>
                    {abcRows.length === 0 && (
                      <tr>
                        <td colSpan="7" className="empty-text">
                          Chưa có dữ liệu
                        </td>
                      </tr>
                    )}
                    {abcRows.map((item, index) => {
                      const keyValue =
                        item.sanPhamId !== undefined &&
                        item.sanPhamId !== null
                          ? item.sanPhamId
                          : `abc-${index}`;
                      return (
                        <tr key={keyValue}>
                          <td>{item.tenSanPham || "Sản phẩm"}</td>
                          <td>{item.sku || "--"}</td>
                          <td>{item.tenDanhMuc || "Khac"}</td>
                          <td>{formatCurrency(item.doanhThu || 0)}</td>
                          <td>{(item.tyLe ?? 0).toFixed(2)}%</td>
                          <td>{(item.tichLuy ?? 0).toFixed(2)}%</td>
                          <td>{item.nhom}</td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </>
          )}
        </div>

        <div className="dashboard-section">
          <div className="section-header">
            <div>
              <h2>Top sản phẩm bán chạy</h2>
              <p className="text-subtle">
                Theo doi top Sản phẩm theo {bestSellerFilters.metric === "revenue"
                  ? "Doanh thu"
                  : "Số lượng"}
              </p>
            </div>
            <span className="section-badge">
              {bestSellerRows.length} ket qua
            </span>
          </div>

          <form
            className="stock-movement-filter"
            onSubmit={(event) => {
              event.preventDefault();
              setBestSellerFilters(bestSellerForm);
            }}
          >
            <div className="filter-field">
              <label htmlFor="best-from">Từ ngày</label>
              <input
                id="best-from"
                type="date"
                value={bestSellerForm.fromDate}
                onChange={(e) =>
                  setBestSellerForm((prev) => ({
                    ...prev,
                    fromDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="best-to">Đến ngày</label>
              <input
                id="best-to"
                type="date"
                value={bestSellerForm.toDate}
                onChange={(e) =>
                  setBestSellerForm((prev) => ({
                    ...prev,
                    toDate: e.target.value,
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="best-limit">Số lượng top</label>
              <input
                id="best-limit"
                type="number"
                min="1"
                max="50"
                value={bestSellerForm.limit}
                onChange={(e) =>
                  setBestSellerForm((prev) => ({
                    ...prev,
                    limit: Number(e.target.value),
                  }))
                }
              />
            </div>
            <div className="filter-field">
              <label htmlFor="best-metric">Tieu chi</label>
              <select
                id="best-metric"
                value={bestSellerForm.metric}
                onChange={(e) =>
                  setBestSellerForm((prev) => ({
                    ...prev,
                    metric: e.target.value,
                  }))
                }
              >
                <option value="quantity">Số lượng</option>
                <option value="revenue">Doanh thu</option>
              </select>
            </div>
            <div className="filter-actions">
              <button type="submit" disabled={loadingBestSeller}>
                Áp dụng
              </button>
              <button
                type="button"
                onClick={() => {
                  const reset = {
                    fromDate: defaultFromDate,
                    toDate: defaultToDate,
                    limit: 10,
                    metric: "quantity",
                  };
                  setBestSellerForm(reset);
                  setBestSellerFilters(reset);
                }}
                disabled={loadingBestSeller}
              >
                Dat lai
              </button>
            </div>
          </form>

          {loadingBestSeller ? (
            <p className="empty-text">Đang tai top Sản phẩm...</p>
          ) : (
            <div className="table-scroll">
              <table className="simple-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Sản phẩm</th>
                    <th>SKU</th>
                    <th>Danh muc</th>
                    <th>Số lượng</th>
                    <th>Doanh thu</th>
                    <th>% dong gop</th>
                  </tr>
                </thead>
                <tbody>
                  {bestSellerRows.length === 0 && (
                    <tr>
                      <td colSpan="7" className="empty-text">
                        Chưa có dữ liệu
                      </td>
                    </tr>
                  )}
                  {bestSellerRows.map((item, idx) => (
                    <tr key={item.sanPhamId ?? `best-${idx}`}>
                      <td>{idx + 1}</td>
                      <td>{item.tenSanPham || "Sản phẩm"}</td>
                      <td>{item.sku || "--"}</td>
                      <td>{item.tenDanhMuc || "Khac"}</td>
                      <td>{formatNumber(item.tongSoLuong || 0)}</td>
                      <td>{formatCurrency(item.tongDoanhThu || 0)}</td>
                      <td>{(item.tyLeDongGop ?? 0).toFixed(2)}%</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
};

export default BaoCao;







