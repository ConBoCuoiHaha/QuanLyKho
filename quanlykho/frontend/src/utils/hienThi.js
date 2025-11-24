const TRANSACTION_TYPE_LABELS = {
  SALE: "Bán hàng",
  PURCHASE: "Nhập hàng",
  RETURN_TO_SUPPLIER: "Trả về nhà cung cấp",
  RETURN_FROM_CUSTOMER: "Khách trả hàng",
};

const TRANSACTION_STATUS_LABELS = {
  PENDING: "Chờ xử lý",
  PROCESSING: "Đang xử lý",
  COMPLETED: "Hoàn tất",
  CANCELLED: "Đã hủy",
};

export const hienThiLoaiGiaoDich = (value) =>
  TRANSACTION_TYPE_LABELS[value] ?? value;

export const hienThiTrangThaiGiaoDich = (value) =>
  TRANSACTION_STATUS_LABELS[value] ?? value;
