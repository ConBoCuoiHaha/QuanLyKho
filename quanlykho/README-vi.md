# Quan Ly Kho (ban tieng Viet)

Du an da duoc sao chep tu IMS-react-master vao thu muc `quanlykho/` gom 2 phan:
- Backend: `quanlykho/backend` (Spring Boot + MySQL + JWT)
- Frontend: `quanlykho/frontend` (React)

## Chay Backend

1) Tao file moi `.env` trong `quanlykho/backend/` tu file mau `.env.example` va cap nhat cac bien:
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`
- `SERVER_PORT` (mac dinh 5050)
- `JWT_SECRET` (chuoi dai, ngau nhien)
- `FRONTEND_PUBLIC_DIR` (mac dinh `../frontend/public`)

2) Chay backend bang Maven:
```
cd quanlykho/backend
./mvnw spring-boot:run
```

## Chay Frontend
```
cd quanlykho/frontend
npm install
npm start
```

## Nhung sua loi quan trong da thuc hien (Nhom 1)
- Di chuyen mat khau DB va JWT secret sang ENV, khong con hard-code.
- Duong dan luu anh san pham la dong, mac dinh luu vao `frontend/public/products` (config qua `FRONTEND_PUBLIC_DIR`).
- Kiem tra so luong ton khi ban: chan ban neu khong du hang.
- Chan xoa danh muc neu con san pham (bo cascade xoa).
- Them `@Transactional` cho giao dich nhap/ban/tra, tu dong rollback khi loi.
- Dashboard chi fetch giao dich theo thang/nam (FE su dung API by-month-year), giam tai.

## Viet hoa
- Backend: message loi va thong bao da chuyen sang Tieng Viet.
- Frontend: da viet hoa Sidebar, Dashboard va su dung API by-month-year.

## Buoc tiep theo de xay dung tinh nang
- Viet hoa toan bo giao dien (pages, label, thong bao).
- Them canh bao sap het hang, lo hang, kiem ke, ma vach, phan trang san pham, ...
- Bo sung phan quyen chi tiet hon va doi/quen mat khau.
- Bao cao gia tri ton kho, xuat nhap ton, best sellers, doanh thu loi nhuan, ...

Hay cho biet uu tien cua ban de minh lam tiep theo thu tu mong muon.
