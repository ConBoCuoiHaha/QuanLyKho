package com.phegondev.InventoryMgtSystem.services.impl;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.phegondev.InventoryMgtSystem.exceptions.NotFoundException;
import com.phegondev.InventoryMgtSystem.models.GiaoDich;
import com.phegondev.InventoryMgtSystem.repositories.GiaoDichRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class XuatPdfService {

    private final GiaoDichRepository giaoDichRepository;

    @Transactional(readOnly = true)
    public byte[] xuatHoaDonGiaoDich(Long giaoDichId) {
        GiaoDich giaoDich = giaoDichRepository.findById(giaoDichId)
                .orElseThrow(() -> new NotFoundException("Khong tim thay giao dich"));
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, baos);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
            Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

            document.add(new Paragraph("Hoa don giao dich #" + giaoDich.getId(), titleFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);

            addRow(table, "Loai giao dich", safeName(giaoDich.getTransactionType() != null ? giaoDich.getTransactionType().name() : ""), labelFont, valueFont);
            addRow(table, "Trang thai", safeName(giaoDich.getStatus() != null ? giaoDich.getStatus().name() : ""), labelFont, valueFont);
            addRow(table, "San pham", safeName(giaoDich.getSanPham() != null ? giaoDich.getSanPham().getName() : ""), labelFont, valueFont);
            addRow(table, "So luong", String.valueOf(defaultInt(giaoDich.getTotalProducts())), labelFont, valueFont);
            addRow(table, "Tong gia", defaultCurrency(giaoDich.getTotalPrice()), labelFont, valueFont);
            addRow(table, "Nha cung cap", safeName(giaoDich.getNhaCungCap() != null ? giaoDich.getNhaCungCap().getName() : "--"), labelFont, valueFont);
            addRow(table, "Khach hang", safeName(giaoDich.getCustomer() != null ? giaoDich.getCustomer().getName() : "--"), labelFont, valueFont);
            addRow(table, "Nguoi thao tac", safeName(giaoDich.getUser() != null ? giaoDich.getUser().getName() : "--"), labelFont, valueFont);
            addRow(table, "Thoi gian", formatDate(giaoDich.getCreatedAt()), labelFont, valueFont);
            addRow(table, "Mo ta", safeName(giaoDich.getDescription()), labelFont, valueFont);
            addRow(table, "Ghi chu", safeName(giaoDich.getNote()), labelFont, valueFont);

            document.add(table);
            document.close();
            return baos.toByteArray();
        } catch (DocumentException | IOException e) {
            throw new IllegalStateException("Khong the tao PDF", e);
        }
    }

    private void addRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        table.addCell(new Paragraph(label, labelFont));
        table.addCell(new Paragraph(value, valueFont));
    }

    private String safeName(String value) {
        return value == null ? "--" : value;
    }

    private int defaultInt(Integer value) {
        return value == null ? 0 : value;
    }

    private String defaultCurrency(BigDecimal value) {
        return value == null ? "0" : value.toPlainString();
    }

    private String formatDate(java.time.LocalDateTime dateTime) {
        if (dateTime == null) {
            return "--";
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        return dateTime.format(formatter);
    }
}
