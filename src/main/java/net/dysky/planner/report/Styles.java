package net.dysky.planner.report;

import org.apache.poi.ss.usermodel.*;

public class Styles {

    public static Font mainHeaderFont(Workbook wb) {
        var font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        return font;
    }

    public static Font subHeader(Workbook wb) {
        var font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        return font;
    }

    public static Font text(Workbook wb) {
        var font = wb.createFont();
        font.setFontName("Arial");
        font.setColor(IndexedColors.GREY_80_PERCENT.getIndex());
        return font;
    }

    public static Font textBold(Workbook wb) {
        var font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setColor(IndexedColors.BLACK.getIndex());
        return font;
    }

    public static Font blueHeaderFont(Workbook wb) {
        var font = wb.createFont();
        font.setFontName("Arial");
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        font.setFontHeightInPoints((short) 12);
        return font;
    }

    public static CellStyle centerAlignedCellStyle(Workbook wb) {
        var style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    public static CellStyle rightAlignedCellStyle(Workbook wb, Font font) {
        var style = wb.createCellStyle();
        style.setAlignment(HorizontalAlignment.RIGHT);
        style.setFont(font);
        return style;
    }

    public static CellStyle blueHeader(Workbook wb, Font BoldFont) {
        var style = centerAlignedCellStyle(wb);

        style.setFont(BoldFont);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        return style;
    }

    public static CellStyle cellStyle(Workbook wb, Font font) {
        var style = wb.createCellStyle();
        style.setFont(font);
        return style;
    }

    public static CellStyle rightAlignedCurrencyCellStyle(Workbook wb, Font font) {
        var style = rightAlignedCellStyle(wb, font);
        DataFormat format = wb.createDataFormat();
        style.setDataFormat(format.getFormat("#,##0.00\" PLN\""));
        return style;
    }

}