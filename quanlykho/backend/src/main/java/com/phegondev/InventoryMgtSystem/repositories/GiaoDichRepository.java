package com.phegondev.InventoryMgtSystem.repositories;

import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.repositories.view.ThongKeKhachHangView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface GiaoDichRepository extends JpaRepository<GiaoDich, Long>, JpaSpecificationExecutor<GiaoDich> {
    List<GiaoDich> findByCreatedAtBetween(LocalDateTime from, LocalDateTime to);

    List<GiaoDich> findByCustomer_IdOrderByCreatedAtDesc(Long customerId);

    @Query("""
            SELECT g.customer.id AS customerId,
                   COUNT(g) AS totalOrders,
                   SUM(CASE WHEN g.transactionType = com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich.SALE THEN g.totalPrice
                            WHEN g.transactionType = com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich.RETURN_FROM_CUSTOMER THEN -g.totalPrice
                            ELSE 0 END) AS totalSpent,
                   SUM(CASE WHEN g.transactionType = com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich.SALE
                                AND g.status <> com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich.COMPLETED THEN g.totalPrice
                            WHEN g.transactionType = com.phegondev.InventoryMgtSystem.enums.LoaiGiaoDich.RETURN_FROM_CUSTOMER
                                AND g.status <> com.phegondev.InventoryMgtSystem.enums.TrangThaiGiaoDich.COMPLETED THEN -g.totalPrice
                            ELSE 0 END) AS outstanding
            FROM GiaoDich g
            WHERE g.customer.id IN :customerIds
            GROUP BY g.customer.id
            """)
    List<ThongKeKhachHangView> thongKeTheoKhachHang(@Param("customerIds") List<Long> customerIds);
}
