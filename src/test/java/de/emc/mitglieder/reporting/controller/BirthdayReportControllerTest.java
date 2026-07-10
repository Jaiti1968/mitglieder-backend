package de.emc.mitglieder.reporting.controller;

import de.emc.mitglieder.reporting.dto.BirthdayReportMonthGroupDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import de.emc.mitglieder.reporting.export.pdf.BirthdayReportPdfService;
import de.emc.mitglieder.reporting.service.BirthdayReportService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BirthdayReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class BirthdayReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BirthdayReportService birthdayReportService;

    @MockitoBean
    private BirthdayReportPdfService birthdayReportPdfService;

    @Test
    void query_shouldReturnBirthdayReportResponse() throws Exception {

        BirthdayReportResponse response =
                new BirthdayReportResponse(
                        2026,
                        List.of(
                                new BirthdayReportMonthGroupDto(
                                        1,
                                        "Januar",
                                        List.of()
                                )
                        ),
                        Instant.parse("2026-07-10T12:00:00Z")
                );

        when(birthdayReportService.createReport(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/reporting/birthday-list/query")
                        .contentType(APPLICATION_JSON)
                        .content("""
                                {
                                  "mitgliedsstatusIds": [1],
                                  "stimmeIds": [2]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.geschaeftsjahr").value(2026))
                .andExpect(jsonPath("$.monthGroups").isArray())
                .andExpect(jsonPath("$.monthGroups[0].monat").value(1))
                .andExpect(jsonPath("$.monthGroups[0].monatsname")
                        .value("Januar"))
                .andExpect(jsonPath("$.monthGroups[0].mitglieder").isArray())
                .andExpect(jsonPath("$.generatedAt")
                        .value("2026-07-10T12:00:00Z"));

        verify(birthdayReportService).createReport(any());
    }
}