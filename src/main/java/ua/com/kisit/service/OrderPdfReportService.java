package ua.com.kisit.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import ua.com.kisit.entity.Order;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class OrderPdfReportService {

    public ByteArrayInputStream generateFinancialReport(List<Order> orders, BigDecimal totalRevenue, String restaurantName) {
        // Збільшуємо поля (margins), щоб документ виглядав просторіше
        Document document = new Document(PageSize.A4, 45, 45, 50, 50);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter.getInstance(document, out);
            document.open();

            // Підключаємо Arial для ідеальної кирилиці
            BaseFont bf = BaseFont.createFont("c:/windows/fonts/arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);

            // Преміальна стримана палітра кольорів
            Color textDark = new Color(28, 25, 23);       // Глибокий графітовий (замість чистого чорного)
            Color textMuted = new Color(120, 113, 108);   // Елегантний сірий для метаданих
            Color accentAmber = new Color(180, 83, 9);    // Благородний бурштиновий
            Color bgLight = new Color(245, 245, 244);     // Ніжний світло-сірий фон для капелюшка таблиці
            Color borderLight = new Color(231, 229, 228); // Тонкі ледь помітні лінії меж
            Color successMuted = new Color(21, 128, 61);  // Стримана пастельна зелень для фінансів

            // Шрифти
            Font fontTitle = new Font(bf, 20, Font.BOLD, textDark);
            Font fontSubtitle = new Font(bf, 13, Font.BOLD, accentAmber);
            Font fontMeta = new Font(bf, 9, Font.NORMAL, textMuted);
            Font fontHeader = new Font(bf, 10, Font.BOLD, textDark);
            Font fontBody = new Font(bf, 9, Font.NORMAL, textDark);
            Font fontBodyBold = new Font(bf, 9, Font.BOLD, textDark);
            Font fontSuccess = new Font(bf, 9, Font.BOLD, successMuted);

            // 1. ШАПКА ЗАКЛАДУ (Ліве вирівнювання, сучасний стиль)
            Paragraph title = new Paragraph("ФІНАНСОВИЙ ЗВІТ ЗАКЛАДУ", fontTitle);
            title.setSpacingAfter(2);
            document.add(title);

            Paragraph restName = new Paragraph(restaurantName, fontSubtitle);
            restName.setSpacingAfter(8);
            document.add(restName);

            String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            Paragraph meta = new Paragraph("Період: Сформовано автоматично • " + currentDate, fontMeta);
            meta.setSpacingAfter(25);
            document.add(meta);

            // 2. ДВОКОЛОНКОВА ПАНЕЛЬ KPI (Виручка та Чеки)
            PdfPTable kpiTable = new PdfPTable(2);
            kpiTable.setWidthPercentage(100);
            kpiTable.setWidths(new int[]{50, 50});
            kpiTable.setSpacingAfter(30);

            // Картка 1: Виручка
            PdfPCell kpi1 = new PdfPCell();
            kpi1.setBorder(Rectangle.NO_BORDER);
            kpi1.addElement(new Paragraph("ЗАГАЛЬНА ВИРУЧКА", fontMeta));
            kpi1.addElement(new Paragraph(totalRevenue + " UAH", new Font(bf, 16, Font.BOLD, successMuted)));
            kpiTable.addCell(kpi1);

            // Картка 2: Кількість чеків
            long closedCount = orders.stream().filter(o -> o.getStatus() == Order.Status.CLOSED).count();
            PdfPCell kpi2 = new PdfPCell();
            kpi2.setBorder(Rectangle.NO_BORDER);
            kpi2.addElement(new Paragraph("ОПЛАЧЕНІ ЧЕКИ", fontMeta));
            kpi2.addElement(new Paragraph(closedCount + " замовлень", new Font(bf, 16, Font.BOLD, textDark)));
            kpiTable.addCell(kpi2);

            document.add(kpiTable);

            // Заголовок журналу замовлень
            Paragraph tableTitle = new Paragraph("РЕЄСТР ОПЕРАЦІЙ", new Font(bf, 11, Font.BOLD, textDark));
            tableTitle.setSpacingAfter(10);
            document.add(tableTitle);

            // 3. МІНІМАЛІСТИЧНА ТАБЛИЦЯ ТРАНЗАКЦІЙ
            PdfPTable table = new PdfPTable(5);
            table.setWidthPercentage(100);
            table.setWidths(new int[]{12, 18, 25, 25, 20});

            // Заголовки стовпців
            String[] headers = {"ID чеку", "Локація", "Метод оплати", "Статус", "Сума"};
            for (String columnHeader : headers) {
                PdfPCell hCell = new PdfPCell(new Paragraph(columnHeader, fontHeader));
                hCell.setBackgroundColor(bgLight);
                hCell.setBorderColor(borderLight);
                hCell.setPaddingTop(10);
                hCell.setPaddingBottom(10);
                hCell.setPaddingLeft(8);
                hCell.setPaddingRight(8);
                if (columnHeader.equals("ID чеку")) hCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                if (columnHeader.equals("Сума")) hCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(hCell);
            }

            // Рядки з даними
            for (Order order : orders) {
                // ID
                PdfPCell cellId = new PdfPCell(new Paragraph("#" + order.getId(), fontBody));
                cellId.setBorderColor(borderLight);
                cellId.setPadding(8);
                cellId.setHorizontalAlignment(Element.ALIGN_CENTER);
                table.addCell(cellId);

                // Стіл
                PdfPCell cellTable = new PdfPCell(new Paragraph("Стіл № " + order.getTableNumber(), fontBody));
                cellTable.setBorderColor(borderLight);
                cellTable.setPadding(8);
                table.addCell(cellTable);

                // Метод
                String payMethod = order.getPaymentMethod() == Order.PaymentMethod.CASH ? "Готівковий розрахунок" : "Безготівковий (Термінал)";
                PdfPCell cellMethod = new PdfPCell(new Paragraph(payMethod, fontBody));
                cellMethod.setBorderColor(borderLight);
                cellMethod.setPadding(8);
                table.addCell(cellMethod);

                // Статус
                boolean isClosed = order.getStatus() == Order.Status.CLOSED;
                String statusStr = isClosed ? "Виконано (Оплачено)" : "В роботі (Активне)";
                PdfPCell cellStatus = new PdfPCell(new Paragraph(statusStr, isClosed ? fontSuccess : fontBody));
                cellStatus.setBorderColor(borderLight);
                cellStatus.setPadding(8);
                table.addCell(cellStatus);

                // Сума
                PdfPCell cellPrice = new PdfPCell(new Paragraph(order.getTotalPrice() + " ₴", fontBodyBold));
                cellPrice.setBorderColor(borderLight);
                cellPrice.setPadding(8);
                cellPrice.setHorizontalAlignment(Element.ALIGN_RIGHT);
                table.addCell(cellPrice);
            }

            document.add(table);
            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return new ByteArrayInputStream(out.toByteArray());
    }
}