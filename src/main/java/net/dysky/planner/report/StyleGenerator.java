package net.dysky.planner.report;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
class StyleGenerator {

    private Map<CustomCellStyle, CellStyle> prepareStyle(Workbook wb) {
        var mainHeaderFont = Styles.mainHeaderFont(wb);
        var subHeaderFont = Styles.subHeader(wb);
        var textFont = Styles.text(wb);
        var blueHeaderFont = Styles.blueHeaderFont(wb);

        var mainHeaderStyle = Styles.cellStyle(wb, mainHeaderFont);
        var subHeaderStyle = Styles.cellStyle(wb, subHeaderFont);
        var textStyle = Styles.cellStyle(wb, textFont);
        var blueHeaderStyle = Styles.blueHeader(wb, blueHeaderFont);

        return Map.of(
                CustomCellStyle.MAIN_HEADER, mainHeaderStyle,
                CustomCellStyle.SUB_HEADER, subHeaderStyle,
                CustomCellStyle.TEXT, textStyle,
                CustomCellStyle.BLUE_HEADER, blueHeaderStyle
        );

    }


}
