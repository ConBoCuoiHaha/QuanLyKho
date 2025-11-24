import axios from "axios";
import CryptoJS from "crypto-js";

export default class ApiService {

    static BASE_URL = process.env.REACT_APP_API_BASE_URL || "http://localhost:5050/api";
    static ENCRYPTION_KEY = "phegon-dev-inventory";


    //encrypt data using cryptoJs
    static encrypt(data) {
        return CryptoJS.AES.encrypt(data, this.ENCRYPTION_KEY.toString());
    }

    //decrypt data using cryptoJs
    static decrypt(data) {
        const bytes = CryptoJS.AES.decrypt(data, this.ENCRYPTION_KEY);
        return bytes.toString(CryptoJS.enc.Utf8);
    }

    //save token with encryption
    static saveToken(token) {
        const encryptedToken = this.encrypt(token);
        localStorage.setItem("token", encryptedToken)
    }

    // retreive the token
    static getToken() {
        const encryptedToken = localStorage.getItem("token");
        if (!encryptedToken) return null;
        return this.decrypt(encryptedToken);
    }

    //save Role with encryption
    static saveRole(role) {
        const encryptedRole = this.encrypt(role);
        localStorage.setItem("role", encryptedRole)
    }

    // retreive the role
    static getRole() {
        const encryptedRole = localStorage.getItem("role");
        if (!encryptedRole) return null;
        return this.decrypt(encryptedRole);
    }

    static clearAuth() {
        localStorage.removeItem("token");
        localStorage.removeItem("role");
    }


    static getHeader() {
        const token = this.getToken();
        return {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json"
        }
    }

    static buildYeuCauGiaoDich(payload = {}) {
        const normalize = (value) => (value === undefined || value === null || value === "" ? null : value);
        const normalizeNumber = (value) => {
            const normalized = normalize(value);
            return normalized === null ? null : Number(normalized);
        };

        return {
            sanPhamId: normalize(payload.sanPhamId ?? payload.productId),
            soLuong: normalizeNumber(payload.soLuong ?? payload.quantity),
            nhaCungCapId: normalize(payload.nhaCungCapId ?? payload.supplierId),
            khachHangId: normalize(payload.khachHangId ?? payload.customerId),
            khoId: normalize(payload.khoId ?? payload.warehouseId ?? payload.fromWarehouseId),
            khoDichId: normalize(payload.khoDichId ?? payload.toWarehouseId),
            moTa: payload.moTa ?? payload.description ?? "",
            ghiChu: payload.ghiChu ?? payload.note ?? "",
            soLo: normalize(payload.soLo ?? payload.lotNumber),
            ngayNhap: normalize(payload.ngayNhap ?? payload.receivedDate),
        };
    }

    /**  AUTH && USERS API */

    static async registerUser(registerData) {
        const response = await axios.post(`${this.BASE_URL}/auth/register`, registerData)
        return response.data;
    }


    static async loginUser(loginData) {
        const response = await axios.post(`${this.BASE_URL}/auth/login`, loginData)
        return response.data;
    }

    static async requestPasswordOtp(email) {
        const response = await axios.post(`${this.BASE_URL}/auth/quen-mat-khau/otp`, { email });
        return response.data;
    }

    static async resetPasswordWithOtp(body) {
        const response = await axios.post(`${this.BASE_URL}/auth/quen-mat-khau/dat-mat-khau`, body);
        return response.data;
    }


    static async getAllUsers() {
        const response = await axios.get(`${this.BASE_URL}/nguoi-dung/tat-ca`, {
            headers: this.getHeader()
        });
        return response.data;
    }


    static async getLoggedInUserInfo() {
        const response = await axios.get(`${this.BASE_URL}/nguoi-dung/current`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async changePassword(body) {
        const response = await axios.post(`${this.BASE_URL}/nguoi-dung/doi-mat-khau`, body, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getBackupList() {
        const response = await axios.get(`${this.BASE_URL}/backup/danh-sach`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async exportBackup() {
        const response = await axios.post(`${this.BASE_URL}/backup/xuat`, null, {
            headers: this.getHeader(),
            responseType: "blob"
        });
        return response;
    }

    static async downloadBackup(fileName) {
        const response = await axios.get(`${this.BASE_URL}/backup/tai/${encodeURIComponent(fileName)}`, {
            headers: this.getHeader(),
            responseType: "blob"
        });
        return response;
    }

    static async restoreBackup(file) {
        const formData = new FormData();
        formData.append("file", file);
        const response = await axios.post(`${this.BASE_URL}/backup/khoi-phuc`, formData, {
            headers: {
                ...this.getHeader(),
                "Content-Type": "multipart/form-data"
            }
        });
        return response.data;
    }

    static async triggerSeed(force = false) {
        const response = await axios.post(`${this.BASE_URL}/seed/demo`, null, {
            headers: this.getHeader(),
            params: { force }
        });
        return response.data;
    }

    static async getUserById(userId) {
        const response = await axios.get(`${this.BASE_URL}/nguoi-dung/${userId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async updateUser(userId, userData) {
        const response = await axios.put(`${this.BASE_URL}/nguoi-dung/cap-nhat/${userId}`, userData, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteUser(userId) {
        const response = await axios.delete(`${this.BASE_URL}/nguoi-dung/xoa/${userId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }




    /**PRODUCT ENDPOINTS */

    static async addProduct(formData) {

        const response = await axios.post(`${this.BASE_URL}/san-pham/them`, formData, {
            headers: {
                ...this.getHeader(),
                "Content-Type": "multipart/form-data"
            }
        });
        return response.data;
    }

    static async updateProduct(formData) {

        const response = await axios.put(`${this.BASE_URL}/san-pham/cap-nhat`, formData, {
            headers: {
                ...this.getHeader(),
                "Content-Type": "multipart/form-data"
            }
        });
        return response.data;
    }

    static async getAllProducts() {
        const response = await axios.get(`${this.BASE_URL}/san-pham/tat-ca`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async getProductById(productId) {
        const response = await axios.get(`${this.BASE_URL}/san-pham/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }

    static async searchProduct(searchValue) {
        const response = await axios.get(`${this.BASE_URL}/san-pham/tim-kiem`, {
            params: { input: searchValue },
            headers: this.getHeader()
        });
        return response.data;
    }

    static async deleteProduct(productId) {
        const response = await axios.delete(`${this.BASE_URL}/san-pham/xoa/${productId}`, {
            headers: this.getHeader()
        });
        return response.data;
    }



    /**CATEGOTY EDNPOINTS */
    static async createCategory(category) {
        const response = await axios.post(`${this.BASE_URL}/danh-muc/them`, category, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getAllCategory() {
        const response = await axios.get(`${this.BASE_URL}/danh-muc/tat-ca`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getCategoryById(categoryId) {
        const response = await axios.get(`${this.BASE_URL}/danh-muc/${categoryId}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updateCategory(categoryId, categoryData) {
        const response = await axios.put(`${this.BASE_URL}/danh-muc/cap-nhat/${categoryId}`, categoryData, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async deleteCategory(categoryId) {
        const response = await axios.delete(`${this.BASE_URL}/danh-muc/xoa/${categoryId}`, {
            headers: this.getHeader()
        })
        return response.data;
    }


    /** Dashboard */
    static async getDashboardSummary({ year, month, lowLimit = 6 } = {}) {
        const params = {};
        if (year) params.year = year;
        if (month) params.month = month;
        if (lowLimit) params.lowLimit = lowLimit;
        const response = await axios.get(`${this.BASE_URL}/dashboard/tong-quan`, {
            headers: this.getHeader(),
            params
        });
        return response.data;
    }

    /**Supplier EDNPOINTS */
    static async addSupplier(supplierData) {
        const response = await axios.post(`${this.BASE_URL}/nha-cung-cap/them`, supplierData, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getAllSuppliers() {
        const response = await axios.get(`${this.BASE_URL}/nha-cung-cap/tat-ca`, {
            headers: this.getHeader()
        })
        return response.data;
    }


    static async getSupplierById(supplierId) {
        const response = await axios.get(`${this.BASE_URL}/nha-cung-cap/${supplierId}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updateSupplier(supplierId, supplierData) {
        const response = await axios.put(`${this.BASE_URL}/nha-cung-cap/cap-nhat/${supplierId}`, supplierData, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async deleteSupplier(supplierId) {
        const response = await axios.delete(`${this.BASE_URL}/nha-cung-cap/xoa/${supplierId}`, {
            headers: this.getHeader()
        })
        return response.data;
    }







    /**Transactions EDNPOINTS */
    static async purchaseProduct(body) {
        const payload = this.buildYeuCauGiaoDich(body);
        const response = await axios.post(`${this.BASE_URL}/giao-dich/nhap`, payload, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async sellProduct(body) {
        const payload = this.buildYeuCauGiaoDich(body);
        const response = await axios.post(`${this.BASE_URL}/giao-dich/ban`, payload, {
            headers: this.getHeader()
        })
        return response.data;
    }
    
    static async returnFromCustomer(body) {
        const payload = this.buildYeuCauGiaoDich(body);
        const response = await axios.post(`${this.BASE_URL}/giao-dich/khach-tra`, payload, {
            headers: this.getHeader()
        })
        return response.data;
    }
    

    static async returnToSupplier(body) {
        const payload = this.buildYeuCauGiaoDich(body);
        const response = await axios.post(`${this.BASE_URL}/giao-dich/tra-nha-cung-cap`, payload, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getAllTransactions(filter) {
        const response = await axios.get(`${this.BASE_URL}/giao-dich/tat-ca`, {
            headers: this.getHeader(),
            params: {filter}
        })
        return response.data;
    }

    static async geTransactionsByMonthAndYear(month, year) {
        const response = await axios.get(`${this.BASE_URL}/giao-dich/theo-thang-nam`, {
            headers: this.getHeader(),
            params: {
                month:month,
                year:year

            }
        })
        return response.data;
    }

    static async getTransactionById(transactionId) {
        const response = await axios.get(`${this.BASE_URL}/giao-dich/${transactionId}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updateTransactionStatus(transactionId, status) {
        const response = await axios.put(`${this.BASE_URL}/giao-dich/cap-nhat-trang-thai/${transactionId}`, status, {
            headers: this.getHeader()
        })
        return response.data;
    }

    // Reports
    static async getInventoryValuationSummary() {
        const response = await axios.get(`${this.BASE_URL}/bao-cao/gia-tri-ton/tong`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getInventoryValuationByCategory() {
        const response = await axios.get(`${this.BASE_URL}/bao-cao/gia-tri-ton/danh-muc`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getInventoryValuationByWarehouse() {
        const response = await axios.get(`${this.BASE_URL}/bao-cao/gia-tri-ton/kho`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getStockMovementReport({ from, to, categoryId } = {}) {
        const params = { tuNgay: from, denNgay: to };
        if (categoryId) {
            params.danhMucId = categoryId;
        }
        const response = await axios.get(`${this.BASE_URL}/bao-cao/xuat-nhap-ton`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async getAgingInventoryReport(minDays = 30) {
        const response = await axios.get(`${this.BASE_URL}/bao-cao/hang-ton-lau`, {
            headers: this.getHeader(),
            params: { minDays }
        })
        return response.data;
    }

    static async getABCReport({ from, to } = {}) {
        const params = {};
        if (from) params.tuNgay = from;
        if (to) params.denNgay = to;
        const response = await axios.get(`${this.BASE_URL}/bao-cao/abc`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async getBestSellers({ from, to, limit = 10, metric = "quantity" } = {}) {
        const params = { limit, metric };
        if (from) params.tuNgay = from;
        if (to) params.denNgay = to;
        const response = await axios.get(`${this.BASE_URL}/bao-cao/san-pham-ban-chay`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async getRevenueReport({ from, to, interval = "DAY" } = {}) {
        const params = { interval };
        if (from) params.tuNgay = from;
        if (to) params.denNgay = to;
        const response = await axios.get(`${this.BASE_URL}/bao-cao/doanh-thu-loi-nhuan`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async getSupplierReport({ from, to, limit = 10 } = {}) {
        const params = { limit };
        if (from) params.tuNgay = from;
        if (to) params.denNgay = to;
        const response = await axios.get(`${this.BASE_URL}/bao-cao/nha-cung-cap`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    // Warehouses
    static async createWarehouse({name, address, manager}) {
        const params = { name, address, manager };
        const response = await axios.post(`${this.BASE_URL}/kho/them`, null, {
            params,
            headers: this.getHeader()
        })
        return response.data;
    }

    static async transferBetweenWarehouses({productId, fromWarehouseId, toWarehouseId, quantity}) {
        const params = { sanPhamId: productId, fromWarehouseId, toWarehouseId, quantity };
        const response = await axios.post(`${this.BASE_URL}/kho/chuyen-kho`, null, {
            params,
            headers: this.getHeader()
        })
        return response.data;
    }

    // Product utilities
    static async getLowStockProducts() {
        const response = await axios.get(`${this.BASE_URL}/san-pham/sap-het-hang`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getProductsPage({ page = 0, size = 20, keyword, categoryId } = {}) {
        const params = { page, size };
        if (keyword && keyword.trim().length > 0) {
            params.keyword = keyword.trim();
        }
        if (categoryId) {
            params.categoryId = categoryId;
        }
        const response = await axios.get(`${this.BASE_URL}/san-pham/phan-trang`, {
            params,
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getExpiringProducts(days = 30) {
        const response = await axios.get(`${this.BASE_URL}/lo-hang/sap-het-han`, {
            params: { days },
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getExpiredProducts() {
        const response = await axios.get(`${this.BASE_URL}/san-pham/het-han`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getExpiredLots() {
        const response = await axios.get(`${this.BASE_URL}/lo-hang/het-han`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async discardLot(loHangId) {
        const response = await axios.post(`${this.BASE_URL}/lo-hang/huy/${loHangId}`, null, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async fetchBarcodeImage({ sku, type = "BARCODE" }) {
        const response = await axios.get(`${this.BASE_URL}/barcode/${encodeURIComponent(sku)}.png`, {
            responseType: "blob",
            headers: this.getHeader(),
            params: { type }
        })
        return response.data;
    }

    static async downloadBarcodePdf({ sku, quantity = 1, type = "BARCODE" }) {
        const response = await axios.get(`${this.BASE_URL}/barcode/tem.pdf`, {
            responseType: "blob",
            headers: this.getHeader(),
            params: { sku, quantity, type }
        })
        return response.data;
    }

    static async getProductBySku(sku) {
        const response = await axios.get(`${this.BASE_URL}/san-pham/ma-vach/${encodeURIComponent(sku)}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getWarehouses() {
        const response = await axios.get(`${this.BASE_URL}/kho/tat-ca`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getCustomers() {
        const response = await axios.get(`${this.BASE_URL}/khach-hang/all`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async createCustomer(body) {
        const response = await axios.post(`${this.BASE_URL}/khach-hang/add`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updateCustomer(id, body) {
        const response = await axios.put(`${this.BASE_URL}/khach-hang/update/${id}`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async deleteCustomer(id) {
        const response = await axios.delete(`${this.BASE_URL}/khach-hang/delete/${id}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getCustomerDetail(id) {
        const response = await axios.get(`${this.BASE_URL}/khach-hang/chi-tiet/${id}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    /** Purchase Orders */
    static async getPurchaseOrders({ page = 0, size = 10, status, nhaCungCapId } = {}) {
        const params = { page, size };
        if (status) params.status = status;
        if (nhaCungCapId) params.nhaCungCapId = nhaCungCapId;
        const response = await axios.get(`${this.BASE_URL}/don-dat-hang`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async createPurchaseOrder(body) {
        const response = await axios.post(`${this.BASE_URL}/don-dat-hang`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getPurchaseOrderDetail(id) {
        const response = await axios.get(`${this.BASE_URL}/don-dat-hang/${id}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updatePurchaseOrderStatus(id, status) {
        const response = await axios.put(`${this.BASE_URL}/don-dat-hang/${id}/trang-thai`, { trangThai: status }, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async receivePurchaseOrder(id, body) {
        const response = await axios.post(`${this.BASE_URL}/don-dat-hang/${id}/nhan-hang`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async exportProductsExcel() {
        return axios.get(`${this.BASE_URL}/xuat-excel/san-pham`, {
            headers: this.getHeader(),
            responseType: "blob"
        });
    }

    static async exportTransactionsExcel({ tuNgay, denNgay } = {}) {
        const params = {};
        if (tuNgay) params.tuNgay = tuNgay;
        if (denNgay) params.denNgay = denNgay;
        return axios.get(`${this.BASE_URL}/xuat-excel/giao-dich`, {
            headers: this.getHeader(),
            responseType: "blob",
            params
        });
    }

    static async exportTransactionPdf(id) {
        return axios.get(`${this.BASE_URL}/xuat-pdf/hoa-don-giao-dich/${id}`, {
            headers: this.getHeader(),
            responseType: "blob"
        });
    }

    static async importProductsExcel(formData) {
        const response = await axios.post(`${this.BASE_URL}/nhap-excel/san-pham`, formData, {
            headers: {
                ...this.getHeader(),
                "Content-Type": "multipart/form-data"
            }
        })
        return response.data;
    }

    /** Sales Orders */
    static async getSalesOrders({ page = 0, size = 10, status, khachHangId } = {}) {
        const params = { page, size };
        if (status) params.status = status;
        if (khachHangId) params.khachHangId = khachHangId;
        const response = await axios.get(`${this.BASE_URL}/don-ban-hang`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async createSalesOrder(body) {
        const response = await axios.post(`${this.BASE_URL}/don-ban-hang`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getSalesOrderDetail(id) {
        const response = await axios.get(`${this.BASE_URL}/don-ban-hang/${id}`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async updateSalesOrderStatus(id, status) {
        const response = await axios.put(`${this.BASE_URL}/don-ban-hang/${id}/trang-thai`, { trangThai: status }, {
            headers: this.getHeader()
        })
        return response.data;
    }

    /** Audit logs */
    static async getAuditLogs({
        page = 0,
        size = 10,
        module,
        hanhDong,
        userId,
        doiTuongLoai,
        doiTuongId,
        tuNgay,
        denNgay
    } = {}) {
        const params = { page, size };
        if (module) params.module = module;
        if (hanhDong) params.hanhDong = hanhDong;
        if (userId) params.userId = userId;
        if (doiTuongLoai) params.doiTuongLoai = doiTuongLoai;
        if (doiTuongId) params.doiTuongId = doiTuongId;
        if (tuNgay) params.tuNgay = tuNgay;
        if (denNgay) params.denNgay = denNgay;

        const response = await axios.get(`${this.BASE_URL}/nhat-ky`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    // Inventory count
    static async getWarehouseStock({ sanPhamId, khoId } = {}) {
        const params = {};
        if (sanPhamId) params.sanPhamId = sanPhamId;
        if (khoId) params.khoId = khoId;
        const response = await axios.get(`${this.BASE_URL}/kho/ton-kho`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async getWarehouseReport() {
        const response = await axios.get(`${this.BASE_URL}/kho/thong-ke`, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getStockSnapshot(productId, warehouseId) {
        const params = { sanPhamId: productId };
        if (warehouseId) {
            params.khoId = warehouseId;
        }
        const response = await axios.get(`${this.BASE_URL}/kiem-ke/hien-trang`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }

    static async performStockTake(body) {
        const response = await axios.post(`${this.BASE_URL}/kiem-ke/thuc-hien`, body, {
            headers: this.getHeader()
        })
        return response.data;
    }

    static async getStockTakeHistory({ sanPhamId, khoId, page = 0, size = 10 } = {}) {
        const params = { page, size };
        if (sanPhamId) params.sanPhamId = sanPhamId;
        if (khoId) params.khoId = khoId;
        const response = await axios.get(`${this.BASE_URL}/kiem-ke/lich-su`, {
            headers: this.getHeader(),
            params
        })
        return response.data;
    }


    /**AUTHENTICATION CHECKER */
    static logout(){
        this.clearAuth()
    }

    static isAuthenticated(){
        const token = this.getToken();
        return !!token;
    }

    static hasRole(role) {
        const currentRole = this.getRole();
        if (!currentRole || !role) return false;
        return currentRole.toUpperCase() === role.toUpperCase();
    }

    static hasAnyRole(roles = []) {
        if (!Array.isArray(roles) || roles.length === 0) {
            return false;
        }
        const currentRole = this.getRole();
        if (!currentRole) {
            return false;
        }
        const normalized = currentRole.toUpperCase();
        return roles.some((role) => role && role.toUpperCase() === normalized);
    }

    static isAdmin(){
        return this.hasRole("ADMIN");
    }

}





