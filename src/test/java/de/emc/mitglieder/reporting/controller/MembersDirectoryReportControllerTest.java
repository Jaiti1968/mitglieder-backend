package de.emc.mitglieder.reporting.controller;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import de.emc.mitglieder.reporting.export.excel.MembersDirectoryExcelService;
import de.emc.mitglieder.reporting.service.MembersDirectoryReportService;
import de.emc.mitglieder.reporting.export.pdf.MembersDirectoryPdfService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembersDirectoryReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class MembersDirectoryReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MembersDirectoryReportService membersDirectoryReportService;

    @MockitoBean
    private MembersDirectoryPdfService membersDirectoryPdfService;

    @MockitoBean
    private MembersDirectoryExcelService membersDirectoryExcelService;

    @Test
    void query_shouldReturnReportResponse() throws Exception {

        MembersDirectoryReportResponse response =
                new MembersDirectoryReportResponse(
                        List.of(),
                        Instant.parse("2026-06-20T12:00:00Z")
                );

        when(membersDirectoryReportService.createReport(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reporting/members-directory/query")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups").isArray())
                .andExpect(jsonPath("$.generatedAt")
                        .value("2026-06-20T12:00:00Z"));

        verify(membersDirectoryReportService).createReport(any());
    }
}