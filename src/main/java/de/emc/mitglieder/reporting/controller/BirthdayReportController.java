package de.emc.mitglieder.reporting.controller;

import de.emc.mitglieder.reporting.dto.BirthdayReportRequest;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import de.emc.mitglieder.reporting.service.BirthdayReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import de.emc.mitglieder.reporting.export.pdf.BirthdayReportPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/reporting/birthday-list")
public class BirthdayReportController {

    private final BirthdayReportService birthdayReportService;

    private final BirthdayReportPdfService birthdayReportPdfService;

    public BirthdayReportController(
            BirthdayReportService birthdayReportService,
            BirthdayReportPdfService birthdayReportPdfService
    ) {
        this.birthdayReportService = birthdayReportService;
        this.birthdayReportPdfService = birthdayReportPdfService;
    }

    @PostMapping("/query")
    public BirthdayReportResponse query(
            @RequestBody BirthdayReportRequest request
    ) {
        return birthdayReportService.createReport(request);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestBody BirthdayReportRequest request
    ) {
        BirthdayReportResponse report =
                birthdayReportService.createReport(request);

        byte[] pdf =
                birthdayReportPdfService.createPdf(report);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=geburtstagsliste.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}