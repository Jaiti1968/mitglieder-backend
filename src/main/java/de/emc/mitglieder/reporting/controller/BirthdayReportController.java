package de.emc.mitglieder.reporting.controller;

import de.emc.mitglieder.reporting.dto.BirthdayReportRequest;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import de.emc.mitglieder.reporting.service.BirthdayReportService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reporting/birthday-list")
public class BirthdayReportController {

    private final BirthdayReportService birthdayReportService;

    public BirthdayReportController(
            BirthdayReportService birthdayReportService
    ) {
        this.birthdayReportService = birthdayReportService;
    }

    @PostMapping("/query")
    public BirthdayReportResponse query(
            @RequestBody BirthdayReportRequest request
    ) {
        return birthdayReportService.createReport(request);
    }
}