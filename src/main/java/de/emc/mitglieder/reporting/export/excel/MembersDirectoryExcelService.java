package de.emc.mitglieder.reporting.export.excel;

import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class MembersDirectoryExcelService {

    public byte[] createExcel(MembersDirectoryReportResponse report) {
        try (
                Workbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            Sheet sheet = workbook.createSheet("Mitgliederverzeichnis");

            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Mitgliederstatus");
            header.createCell(1).setCellValue("Stimme");
            header.createCell(2).setCellValue("Mitgliedsnummer");
            header.createCell(3).setCellValue("Nachname");
            header.createCell(4).setCellValue("Vorname");
            header.createCell(5).setCellValue("PLZ");
            header.createCell(6).setCellValue("Ort");
            header.createCell(7).setCellValue("Straße");
            header.createCell(8).setCellValue("Geburtsdatum");
            header.createCell(9).setCellValue("Telefon privat");
            header.createCell(10).setCellValue("Telefon geschäftlich");
            header.createCell(11).setCellValue("Mobiltelefon");
            header.createCell(12).setCellValue("E-Mail");

            DateTimeFormatter dateFormatter =
                    DateTimeFormatter.ofPattern("dd.MM.yyyy");

            int rowIndex = 1;

            for (var group : report.getGroups()) {
                for (var stimmgruppe : group.getStimmgruppen()) {
                    for (var mitglied : stimmgruppe.getMitglieder()) {
                        Row row = sheet.createRow(rowIndex++);

                        row.createCell(0).setCellValue(nullToEmpty(group.getMitgliedsstatus()));
                        row.createCell(1).setCellValue(formatStimme(stimmgruppe.getStimme()));
                        row.createCell(2).setCellValue(nullToEmpty(mitglied.getMitgliedsnummer()));
                        row.createCell(3).setCellValue(nullToEmpty(mitglied.getNachname()));
                        row.createCell(4).setCellValue(nullToEmpty(mitglied.getVorname()));
                        row.createCell(5).setCellValue(nullToEmpty(mitglied.getPlz()));
                        row.createCell(6).setCellValue(nullToEmpty(mitglied.getOrt()));
                        row.createCell(7).setCellValue(nullToEmpty(mitglied.getStrasseHausNr()));
                        row.createCell(8).setCellValue(
                                mitglied.getGeburtsdatum() != null
                                        ? mitglied.getGeburtsdatum().format(dateFormatter)
                                        : ""
                        );
                        row.createCell(9).setCellValue(nullToEmpty(mitglied.getTelefonPrivat()));
                        row.createCell(10).setCellValue(nullToEmpty(mitglied.getTelefonGeschaeftlich()));
                        row.createCell(11).setCellValue(nullToEmpty(mitglied.getMobiltelefon()));
                        row.createCell(12).setCellValue(nullToEmpty(mitglied.getEmail()));
                    }
                }
            }

            for (int column = 0; column <= 12; column++) {
                sheet.autoSizeColumn(column);
            }

            workbook.write(outputStream);
            return outputStream.toByteArray();

        } catch (Exception ex) {
            throw new IllegalStateException("Excel-Datei konnte nicht erzeugt werden", ex);
        }
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private String formatStimme(String stimme) {
        if (stimme == null || stimme.isBlank() || "keine".equalsIgnoreCase(stimme)) {
            return "Ohne Stimme";
        }

        return stimme;
    }
}