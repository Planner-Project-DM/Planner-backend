package net.dysky.planner.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
class ReportController {

    private final ReportService reportService;

    @GetMapping("/funds-summary")
    public ResponseEntity<byte[]> generateFundsSummaryReport() {
        byte[] reportFundsSummary;
        try {
            reportFundsSummary = reportService.generateReport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return createResponseEntity(reportFundsSummary, "funds_summary_report.xls");
    }

    private ResponseEntity<byte[]> createResponseEntity(byte[] reportData, String fileName) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                .body(reportData);
    }
}
