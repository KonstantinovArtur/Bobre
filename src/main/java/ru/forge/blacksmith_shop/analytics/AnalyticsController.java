package ru.forge.blacksmith_shop.analytics;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

@Controller
@RequestMapping("/admin/analytics")
public class AnalyticsController {

    private final AnalyticsDao dao;

    public AnalyticsController(AnalyticsDao dao) {
        this.dao = dao;
    }

    @GetMapping
    public String analytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            Model model
    ) {
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("monthlySales", dao.getMonthlySales(from, to));
        model.addAttribute("topProducts",  dao.getTopProducts(from, to, 10));
        model.addAttribute("revByCat", dao.getRevenueByCategory(from, to));
        return "admin/analytics";
    }

    @GetMapping("/export/monthly")
    public void exportMonthly(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse resp) throws Exception {

        var rows = dao.getMonthlySales(from, to);
        startCsv(resp, "monthly_sales.csv");
        try (var pw = csvWriter(resp)) {
            pw.println("Месяц;Сумма продаж (₽)");
            for (var r : rows) {
                pw.println(csv(r.getMonth()) + ";" + r.getTotal());
            }
        }
    }

    @GetMapping("/export/top")
    public void exportTop(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "10") int limit,
            HttpServletResponse resp) throws Exception {

        var rows = dao.getTopProducts(from, to, limit);
        startCsv(resp, "top_products.csv");
        try (var pw = csvWriter(resp)) {
            pw.println("Товар;Продано (шт);Выручка (₽)");
            for (var r : rows) {
                pw.println(csv(r.getItemName()) + ";" + r.getTotalSold() + ";" + r.getRevenue());
            }

        }
    }

    @GetMapping("/export/categories")
    public void exportCategories(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            HttpServletResponse resp) throws Exception {

        var rows = dao.getRevenueByCategory(from, to);
        startCsv(resp, "category_revenue.csv");
        try (var pw = csvWriter(resp)) {
            pw.println("Категория;Выручка (₽)");
            for (var r : rows) {
                pw.println(csv(r.getCategory()) + ";" + r.getRevenue());
            }

        }
    }

    /* ---------- Вспомогательные методы для CSV ---------- */

    // Устанавливаем заголовки и тип (с BOM, чтобы Excel открыл по-русски)
    private void startCsv(HttpServletResponse resp, String filename) throws Exception {
        resp.setContentType("text/csv; charset=UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        // BOM для Excel
        resp.getOutputStream().write(new byte[] {(byte)0xEF, (byte)0xBB, (byte)0xBF});
    }

    // Пишем UTF-8 поверх того же OutputStream (после BOM)
    private PrintWriter csvWriter(HttpServletResponse resp) throws Exception {
        return new PrintWriter(new OutputStreamWriter(resp.getOutputStream(), StandardCharsets.UTF_8), true);
    }

    // Экранируем значение (кавычки удваиваем, оборачиваем в ")
    private String csv(String v) {
        if (v == null) return "";
        String s = v.replace("\"", "\"\"");
        return "\"" + s + "\"";
    }
}
