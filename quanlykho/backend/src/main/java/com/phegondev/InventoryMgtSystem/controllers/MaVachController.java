package com.phegondev.InventoryMgtSystem.controllers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.phegondev.InventoryMgtSystem.models.SanPham;
import com.phegondev.InventoryMgtSystem.repositories.SanPhamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.util.Optional;

import com.google.zxing.client.j2se.MatrixToImageWriter;

@RestController
@RequestMapping("/api/barcode")
@RequiredArgsConstructor
public class MaVachController {

    private final SanPhamRepository sanPhamRepository;

    @GetMapping("/{sku}.png")
    public ResponseEntity<byte[]> barcode(@PathVariable String sku,
                                          @RequestParam(defaultValue = "BARCODE") String type) throws Exception {
        BarcodeFormat format = resolveFormat(type);
        byte[] bytes = createBarcodePng(sku, format);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=barcode-" + sku + ".png")
                .contentType(MediaType.IMAGE_PNG)
                .body(bytes);
    }

    @GetMapping("/tem.pdf")
    public ResponseEntity<byte[]> barcodePdf(@RequestParam String sku,
                                             @RequestParam(defaultValue = "BARCODE") String type,
                                             @RequestParam(defaultValue = "1") int quantity) throws Exception {
        if (quantity < 1) {
            quantity = 1;
        }
        if (quantity > 100) {
            quantity = 100;
        }
        BarcodeFormat format = resolveFormat(type);
        byte[] barcodeImage = createBarcodePng(sku, format);
        Optional<SanPham> sanPham = sanPhamRepository.findBySkuIgnoreCase(sku);
        String tenHienThi = sanPham.map(SanPham::getName).orElse(sku);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, baos);
        document.open();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);

        for (int i = 0; i < quantity; i++) {
            table.addCell(buildLabelCell(tenHienThi, sku, barcodeImage));
        }
        if (quantity % 2 != 0) {
            PdfPCell empty = new PdfPCell(new Paragraph(""));
            empty.setBorder(Rectangle.NO_BORDER);
            table.addCell(empty);
        }

        document.add(table);
        document.close();

        byte[] pdfBytes = baos.toByteArray();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=barcode-" + sku + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    private PdfPCell buildLabelCell(String tenSanPham, String sku, byte[] imageBytes) throws Exception {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(12);

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, BaseColor.BLACK);
        Paragraph title = new Paragraph(tenSanPham, titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(title);

        Image barcode = Image.getInstance(imageBytes);
        barcode.scalePercent(60);
        barcode.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(barcode);

        Font skuFont = FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.DARK_GRAY);
        Paragraph skuParagraph = new Paragraph(sku, skuFont);
        skuParagraph.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(skuParagraph);
        return cell;
    }

    private BarcodeFormat resolveFormat(String type) {
        if ("QR".equalsIgnoreCase(type) || "QRCODE".equalsIgnoreCase(type)) {
            return BarcodeFormat.QR_CODE;
        }
        return BarcodeFormat.CODE_128;
    }

    private byte[] createBarcodePng(String text, BarcodeFormat format) throws Exception {
        int width = format == BarcodeFormat.QR_CODE ? 300 : 400;
        int height = format == BarcodeFormat.QR_CODE ? 300 : 120;
        BitMatrix matrix = new MultiFormatWriter().encode(text, format, width, height);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            MatrixToImageWriter.writeToStream(matrix, "PNG", out);
            return out.toByteArray();
        }
    }
}
