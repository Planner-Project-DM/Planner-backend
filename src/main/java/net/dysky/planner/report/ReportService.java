package net.dysky.planner.report;

import lombok.RequiredArgsConstructor;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final int NUMBER_OF_ROWS = 20;

    private final StyleGenerator styleGenerator;

    byte[] generateReport() throws Exception {
        Workbook wb = new HSSFWorkbook();

        var styles = styleGenerator.prepareStyle(wb);
        var sheet = wb.createSheet("Planner Report");

        generateRows(sheet);

        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 20 * 256);

        createHeaderRow(sheet, styles);
        createKeyStatistic(sheet, styles);

        sheet.setColumnWidth(4, 25 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 20 * 256);
        createTop3(sheet, styles);

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        wb.write(out);

        out.close();
        wb.close();

        return out.toByteArray();
    }

    private void generateRows(Sheet sheet) {
        for(int i = 0; i < NUMBER_OF_ROWS; i++) {
            sheet.createRow(i);
        }
    }

    private void createHeaderRow(Sheet sheet, Map<CustomCellStyle, CellStyle> styles) {
        createRow(sheet, 1, 1, "NAME - PODSUMOWANIE FUNDUSZY", styles.get(CustomCellStyle.MAIN_HEADER));
        groupColumn(sheet, 1, 1, 3);

        createRow(sheet, 2, 1, "Podsumowanie funduszy", styles.get(CustomCellStyle.TEXT));
    }

    private void createKeyStatistic(Sheet sheet, Map<CustomCellStyle, CellStyle> styles) {
        createRow(sheet, 4, 1, "Kluczowe statystyki", styles.get(CustomCellStyle.SUB_HEADER));

        createRow(sheet, 5, 1, "Calkowite Wydatki:", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 6, 1, "Liczba Uczestnikow:", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 7, 1, "Liczba Transakcji:", styles.get(CustomCellStyle.TEXT));

        createRow(sheet, 5, 2, "3 600,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));
        createRow(sheet, 6, 2, "15", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));
        createRow(sheet, 7, 2, "25", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));

    }

    private void createTop3(Sheet sheet, Map<CustomCellStyle, CellStyle> styles) {
        createRow(sheet, 4, 4, "Top Wydatków", styles.get(CustomCellStyle.SUB_HEADER));

        createRow(sheet, 5, 4, "Miejsce / Wydatek", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 5, 5, "Kategoria", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 5, 6, "Kwota", styles.get(CustomCellStyle.BLUE_HEADER));

        createRow(sheet, 6, 4, "Wydatek A", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 7, 4, "Wydatek B", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 8, 4, "Wydatek C", styles.get(CustomCellStyle.TEXT));

        createRow(sheet, 6, 5, "Kategoria A", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 7, 5, "Kategoria B", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 8, 5, "Kategoria C", styles.get(CustomCellStyle.TEXT));

        createRow(sheet, 6, 6, "1 000,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 7, 6, "800,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 8, 6, "600,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
    }

    private void createRow(Sheet sheet, int rowIndex, int cellIndex, String value, CellStyle style) {
        Row row = sheet.getRow(rowIndex);

        Cell cell = row.createCell(cellIndex);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void groupColumn(Sheet sheet, int rowIndex, int startColumnIndex, int endColumnIndex) {
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startColumnIndex, endColumnIndex));
    }
}
