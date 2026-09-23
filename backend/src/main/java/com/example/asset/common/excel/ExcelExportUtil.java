package com.example.asset.common.excel;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Excel 导出通用工具
 */
public class ExcelExportUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 创建 SXSSFWorkbook（流式写入，适合大数据量）
     */
    public static SXSSFWorkbook createWorkbook() {
        SXSSFWorkbook wb = new SXSSFWorkbook(100);
        wb.setCompressTempFiles(true);
        return wb;
    }

    /**
     * 写入标题行（合并单元格）
     */
    public static void writeTitle(Sheet sheet, String title, int columnCount) {
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(createTitleStyle(sheet.getWorkbook()));
        if (columnCount > 1) {
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, columnCount - 1));
        }
        row.setHeightInPoints(28);
    }

    /**
     * 写入表头行
     */
    public static void writeHeader(Sheet sheet, String[] headers, int headerRowIndex) {
        Row row = sheet.createRow(headerRowIndex);
        CellStyle style = createHeaderStyle(sheet.getWorkbook());
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
        row.setHeightInPoints(22);
    }

    /**
     * 写入数据行
     */
    public static void writeRow(Sheet sheet, int rowIndex, Object[] values) {
        Workbook wb = sheet.getWorkbook();
        CellStyle dataStyle = createDataStyle(wb);
        CellStyle moneyStyle = createMoneyStyle(wb);
        CellStyle dateStyle = createDateStyle(wb);

        Row row = sheet.createRow(rowIndex);
        for (int i = 0; i < values.length; i++) {
            Cell cell = row.createCell(i);
            Object val = values[i];
            if (val == null) {
                cell.setCellValue("");
                cell.setCellStyle(dataStyle);
            } else if (val instanceof java.math.BigDecimal) {
                cell.setCellValue(((java.math.BigDecimal) val).doubleValue());
                cell.setCellStyle(moneyStyle);
            } else if (val instanceof Number) {
                cell.setCellValue(((Number) val).doubleValue());
                cell.setCellStyle(dataStyle);
            } else if (val instanceof LocalDateTime) {
                cell.setCellValue(((LocalDateTime) val).format(DATETIME_FMT));
                cell.setCellStyle(dateStyle);
            } else if (val instanceof LocalDate) {
                cell.setCellValue(((LocalDate) val).format(DATE_FMT));
                cell.setCellStyle(dateStyle);
            } else if (val instanceof Date) {
                cell.setCellValue(val.toString());
                cell.setCellStyle(dateStyle);
            } else {
                cell.setCellValue(val.toString());
                cell.setCellStyle(dataStyle);
            }
        }
    }

    /**
     * 设置列宽
     */
    public static void setColumnWidths(Sheet sheet, int[] widths) {
        for (int i = 0; i < widths.length; i++) {
            sheet.setColumnWidth(i, widths[i] * 256);
        }
    }

    /**
     * 冻结表头行
     */
    public static void freezeHeader(Sheet sheet, int headerRowIndex) {
        sheet.createFreezePane(0, headerRowIndex + 1);
    }

    /**
     * 写入 HttpServletResponse
     */
    public static void writeToResponse(HttpServletResponse response, String filename, Workbook workbook) {
        try {
            String encodedFilename = URLEncoder.encode(filename, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedFilename);
            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");
            OutputStream os = response.getOutputStream();
            workbook.write(os);
            os.flush();
            if (workbook instanceof SXSSFWorkbook) {
                ((SXSSFWorkbook) workbook).dispose();
            }
            workbook.close();
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    // ===== 样式创建方法 =====

    public static CellStyle createTitleStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 14);
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.ROYAL_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    public static CellStyle createHeaderStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 11);
        font.setBold(true);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        setBorders(style);
        return style;
    }

    public static CellStyle createDataStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    public static CellStyle createMoneyStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00"));
        setBorders(style);
        return style;
    }

    public static CellStyle createDateStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setFontName("Microsoft YaHei");
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        setBorders(style);
        return style;
    }

    private static void setBorders(CellStyle style) {
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
    }
}
