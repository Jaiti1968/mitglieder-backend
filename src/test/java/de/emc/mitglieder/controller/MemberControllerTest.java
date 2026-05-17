package de.emc.mitglieder.controller;

import de.emc.mitglieder.dto.error.ValidationErrorDto;
import de.emc.mitglieder.dto.member.*;
import de.emc.mitglieder.dto.request.UpdateMitgliedschaftRequest;
import de.emc.mitglieder.exception.BusinessValidationException;
import de.emc.mitglieder.exception.NotFoundException;
import de.emc.mitglieder.service.member.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MemberService memberService;

    @Test
    void getMemberById_shouldReturnMemberDetail() throws Exception {
        MemberDetailDto response = new MemberDetailDto(
                "N1234",
                new StammdatenDto(
                        false,
                        "Herr",
                        "Dr.",
                        "Max",
                        "Mustermann",
                        "99084",
                        "Erfurt",
                        "Musterstraße 1",
                        LocalDate.of(1980, 5, 20)
                ),
                new KontaktDto(
                        "0361...",
                        null,
                        "0151...",
                        "max@example.de",
                        null,
                        "Lieber Sangesfreund Max Mustermann"
                ),
                new MitgliedschaftDto(
                        LocalDate.of(2024, 1, 1),
                        null,
                        4,
                        "Kandidat",
                        6,
                        "keine",
                        false
                )
        );

        when(memberService.getMemberById("N1234")).thenReturn(response);

        mockMvc.perform(get("/api/members/N1234"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1234"))
                .andExpect(jsonPath("$.stammdaten.vorname").value("Max"))
                .andExpect(jsonPath("$.stammdaten.nachname").value("Mustermann"))
                .andExpect(jsonPath("$.stammdaten.geburtsdatum").value("1980-05-20"))
                .andExpect(jsonPath("$.kontakt.email").value("max@example.de"))
                .andExpect(jsonPath("$.mitgliedschaft.eintritt").value("2024-01-01"))
                .andExpect(jsonPath("$.mitgliedschaft.mitgliedsstatus").value("Kandidat"));

        verify(memberService).getMemberById("N1234");
    }

    @Test
    void updateMitgliedschaft_shouldBindDateOnlyJsonAndCallService() throws Exception {
        MemberDetailDto response = new MemberDetailDto(
                "N1234",
                null,
                null,
                new MitgliedschaftDto(
                        LocalDate.of(2024, 1, 1),
                        null,
                        4,
                        "Kandidat",
                        6,
                        "keine",
                        false
                )
        );

        when(memberService.updateMitgliedschaft(eq("N1234"), any(UpdateMitgliedschaftRequest.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/members/N1234/mitgliedschaft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eintritt": "2024-01-01",
                                  "austritt": null,
                                  "mitgliedsstatusId": 4,
                                  "stimmeId": 6,
                                  "kammerchor": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mitgliedsnummer").value("N1234"))
                .andExpect(jsonPath("$.mitgliedschaft.eintritt").value("2024-01-01"))
                .andExpect(jsonPath("$.mitgliedschaft.mitgliedsstatusId").value(4))
                .andExpect(jsonPath("$.mitgliedschaft.stimmeId").value(6));

        verify(memberService).updateMitgliedschaft(
                eq("N1234"),
                org.mockito.ArgumentMatchers.argThat(request ->
                        LocalDate.of(2024, 1, 1).equals(request.getEintritt())
                                && request.getAustritt() == null
                                && Integer.valueOf(4).equals(request.getMitgliedsstatusId())
                                && Integer.valueOf(6).equals(request.getStimmeId())
                                && Boolean.FALSE.equals(request.getKammerchor())
                )
        );
    }

    @Test
    void getMemberById_shouldReturn404WhenNotFound() throws Exception {
        when(memberService.getMemberById("N9999"))
                .thenThrow(new NotFoundException("Mitglied nicht gefunden"));

        mockMvc.perform(get("/api/members/N9999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Mitglied nicht gefunden"))
                .andExpect(jsonPath("$.path").value("/api/members/N9999"));
    }

    @Test
    void updateMitgliedschaft_shouldReturn400ForDtoValidationErrors() throws Exception {
        mockMvc.perform(put("/api/members/N1234/mitgliedschaft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eintritt": "2024-01-01",
                                  "austritt": null,
                                  "kammerchor": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validierungsfehler"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors[0].field").exists())
                .andExpect(jsonPath("$.validationErrors[0].message").exists());
    }

    @Test
    void updateMitgliedschaft_shouldReturn400ForBusinessValidationErrors() throws Exception {
        when(memberService.updateMitgliedschaft(eq("N1234"), any(UpdateMitgliedschaftRequest.class)))
                .thenThrow(new BusinessValidationException(
                        List.of(new ValidationErrorDto(
                                "austritt",
                                "Austritt darf nicht vor Eintritt liegen"
                        ))
                ));

        mockMvc.perform(put("/api/members/N1234/mitgliedschaft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "eintritt": "2025-05-10",
                                  "austritt": "2025-05-01",
                                  "mitgliedsstatusId": 4,
                                  "stimmeId": 6,
                                  "kammerchor": false
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validierungsfehler"))
                .andExpect(jsonPath("$.validationErrors[0].field").value("austritt"))
                .andExpect(jsonPath("$.validationErrors[0].message")
                        .value("Austritt darf nicht vor Eintritt liegen"));
    }

    @Test
    void getMembers_shouldBindQueryParamsAndReturnListResponse() throws Exception {
        MemberListResponse response = new MemberListResponse(
                List.of(
                        new MemberListItemDto(
                                "N1234",
                                false,
                                "Max",
                                "Mustermann",
                                "Erfurt",
                                4,
                                "Kandidat",
                                6,
                                "keine"
                        )
                ),
                new PaginationDto(
                        2,
                        10,
                        1,
                        1
                )
        );

        when(memberService.getMembers(
                eq("max"),
                eq(List.of(1, 4)),
                eq(List.of(2, 6)),
                eq(2),
                eq(10)
        )).thenReturn(response);

        mockMvc.perform(get("/api/members")
                        .param("search", "max")
                        .param("statusId", "1", "4")
                        .param("stimmeId", "2", "6")
                        .param("page", "2")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].mitgliedsnummer").value("N1234"))
                .andExpect(jsonPath("$.items[0].vorname").value("Max"))
                .andExpect(jsonPath("$.items[0].nachname").value("Mustermann"))
                .andExpect(jsonPath("$.items[0].mitgliedsstatusId").value(4))
                .andExpect(jsonPath("$.items[0].stimmeId").value(6))
                .andExpect(jsonPath("$.pagination.page").value(2))
                .andExpect(jsonPath("$.pagination.pageSize").value(10))
                .andExpect(jsonPath("$.pagination.totalItems").value(1))
                .andExpect(jsonPath("$.pagination.totalPages").value(1));

        verify(memberService).getMembers(
                "max",
                List.of(1, 4),
                List.of(2, 6),
                2,
                10
        );
    }

    @Test
    void createMember_shouldReturn400WhenRequiredSectionsAreMissing() throws Exception {
        mockMvc.perform(post("/api/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "kontakt": {}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validierungsfehler"))
                .andExpect(jsonPath("$.validationErrors").isArray());
    }
}