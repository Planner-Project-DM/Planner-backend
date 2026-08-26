package net.dysky.planner.report;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.tripitinerary.TripItinerary;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
@Service
public class ReportService {

    private static final int NUMBER_OF_ROWS = 20;

    private final StyleGenerator styleGenerator;

    byte[] generateReport(Trip trip) throws Exception {
        Workbook wb = new HSSFWorkbook();

        var styles = styleGenerator.prepareStyle(wb);
        var sheet = wb.createSheet("Planner Report");

        generateRows(sheet);

        sheet.setColumnWidth(1, 30 * 256);
        sheet.setColumnWidth(2, 20 * 256);

        createHeaderRow(sheet, styles);
        createKeyStatistic(sheet, styles, trip);

        sheet.setColumnWidth(4, 25 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 20 * 256);
        createTop3(sheet, styles, trip);

        sheet.setColumnWidth(3, 20 * 256);
        createExpenseByCategory(sheet, styles);

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

    private void createKeyStatistic(Sheet sheet, Map<CustomCellStyle, CellStyle> styles, Trip trip) {
        createRow(sheet, 4, 1, "Kluczowe statystyki", styles.get(CustomCellStyle.SUB_HEADER));

        createRow(sheet, 5, 1, "Budzet:", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 6, 1, "Calkowite Wydatki:", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 7, 1, "Liczba Uczestnikow:", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 8, 1, "Liczba Transakcji:", styles.get(CustomCellStyle.TEXT));

        createRow(sheet, 5, 2, trip.getBudget(), styles.get(CustomCellStyle.TEXT_CURRENCY));
        createRow(sheet, 6, 2, trip.getActualCost(), styles.get(CustomCellStyle.TEXT_CURRENCY));
        createRow(sheet, 7, 2, trip.getTripGroup().getGroupUsers().size(), styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));
        createRow(sheet, 8, 2, 25, styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));

    }

    private void createTop3(Sheet sheet, Map<CustomCellStyle, CellStyle> styles, Trip trip) {
        createRow(sheet, 4, 4, "Top Wydatków", styles.get(CustomCellStyle.SUB_HEADER));

        createRow(sheet, 5, 4, "Miejsce / Wydatek", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 5, 5, "Kategoria", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 5, 6, "Kwota", styles.get(CustomCellStyle.BLUE_HEADER));

        AtomicInteger rowIndex = new AtomicInteger(6);
        trip.getTripItineraries().stream()
                .sorted(Comparator.comparingDouble(TripItinerary::getPrice).reversed())
                .limit(3)
                .forEach( (item) -> {
                    createRow(sheet, rowIndex.get(), 4, item.getTripItem().getName(), styles.get(CustomCellStyle.TEXT));
                    createRow(sheet, rowIndex.get(), 5, item.getTripItem().getCategory().name().charAt(0) + item.getTripItem().getCategory().name().substring(1).toLowerCase(), styles.get(CustomCellStyle.TEXT));
                    createRow(sheet, rowIndex.getAndIncrement(), 6, item.getPrice(), styles.get(CustomCellStyle.TEXT_CURRENCY));
                }
        );
    }

    private void createExpenseByCategory(Sheet sheet, Map<CustomCellStyle, CellStyle> styles) {
        createRow(sheet, 10, 1, "Wydatki wg Kategorii", styles.get(CustomCellStyle.SUB_HEADER));

        createRow(sheet, 11, 1, "Kategoria", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 11, 2, "Kwota (PLN)", styles.get(CustomCellStyle.BLUE_HEADER));
        createRow(sheet, 11, 3, "Udział (%)", styles.get(CustomCellStyle.BLUE_HEADER));

        createRow(sheet, 12, 1, "Nocleg", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 13, 1, "Rozrywka", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 14, 1, "Jedzenie", styles.get(CustomCellStyle.TEXT));
        createRow(sheet, 15, 1, "Inne", styles.get(CustomCellStyle.TEXT));

        createRow(sheet, 12, 2, "1 500,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 13, 2, "1 000,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 14, 2, "800,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 15, 2, "300,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));

        createRow(sheet, 12, 3, "41,67%", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 13, 3, "27,78%", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 14, 3, "22,22%", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));
        createRow(sheet, 15, 3, "8,33%", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT));

        createRow(sheet, 16, 1, "Suma", styles.get(CustomCellStyle.TEXT_BOLD));
        createRow(sheet, 16, 2, "3 600,00 PLN", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));
        createRow(sheet, 16, 3, "100%", styles.get(CustomCellStyle.RIGHT_ALIGNED_TEXT_BOLD));
    }

    private void createRow(Sheet sheet, int rowIndex, int cellIndex, String value, CellStyle style) {
        Row row = sheet.getRow(rowIndex);

        Cell cell = row.createCell(cellIndex);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createRow(Sheet sheet, int rowIndex, int cellIndex, Double value, CellStyle style) {
        Row row = sheet.getRow(rowIndex);

        Cell cell = row.createCell(cellIndex);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createRow(Sheet sheet, int rowIndex, int cellIndex, int value, CellStyle style) {
        Row row = sheet.getRow(rowIndex);

        Cell cell = row.createCell(cellIndex);

        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void groupColumn(Sheet sheet, int rowIndex, int startColumnIndex, int endColumnIndex) {
        sheet.addMergedRegion(new CellRangeAddress(rowIndex, rowIndex, startColumnIndex, endColumnIndex));
    }
}
