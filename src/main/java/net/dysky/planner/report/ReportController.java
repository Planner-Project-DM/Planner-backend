package net.dysky.planner.report;

import lombok.RequiredArgsConstructor;
import net.dysky.planner.trip.Trip;
import net.dysky.planner.trip.TripService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reports")
class ReportController {

    private final ReportService reportService;

    private final TripService tripService;

    @GetMapping("{id}/funds-summary")
    public ResponseEntity<byte[]> generateFundsSummaryReport(@PathVariable("id") UUID id) {
        Trip trip = tripService.getTripById(id);

        byte[] reportFundsSummary;
        try {
            reportFundsSummary = reportService.generateReport(trip);
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
