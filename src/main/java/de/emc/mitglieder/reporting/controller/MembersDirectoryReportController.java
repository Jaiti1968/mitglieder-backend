package de.emc.mitglieder.reporting.controller;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportRequest;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import de.emc.mitglieder.reporting.export.excel.MembersDirectoryExcelService;
import de.emc.mitglieder.reporting.service.MembersDirectoryReportService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.emc.mitglieder.reporting.export.pdf.MembersDirectoryPdfService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/reporting/members-directory")
public class MembersDirectoryReportController {

    private final MembersDirectoryReportService membersDirectoryReportService;

    private final MembersDirectoryPdfService membersDirectoryPdfService;

    private final MembersDirectoryExcelService membersDirectoryExcelService;

    public MembersDirectoryReportController(
            MembersDirectoryReportService membersDirectoryReportService,
            MembersDirectoryPdfService membersDirectoryPdfService,
            MembersDirectoryExcelService membersDirectoryExcelService
    ) {
        this.membersDirectoryReportService = membersDirectoryReportService;
        this.membersDirectoryPdfService = membersDirectoryPdfService;
        this.membersDirectoryExcelService = membersDirectoryExcelService;
    }

    @PostMapping("/query")
    public MembersDirectoryReportResponse query(@RequestBody MembersDirectoryReportRequest request) {
        return membersDirectoryReportService.createReport(request);
    }

    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(
            @RequestBody MembersDirectoryReportRequest request
    ) {
        MembersDirectoryReportResponse report =
                membersDirectoryReportService.createReport(request);

        byte[] pdf = membersDirectoryPdfService.createPdf(report);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=mitgliederverzeichnis.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(
            @RequestBody MembersDirectoryReportRequest request
    ) {
        MembersDirectoryReportResponse report =
                membersDirectoryReportService.createReport(request);

        byte[] excel = membersDirectoryExcelService.createExcel(report);

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=mitgliederverzeichnis.xlsx"
                )
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                ))
                .body(excel);
    }
}