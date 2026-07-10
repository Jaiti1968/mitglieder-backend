package de.emc.mitglieder.reporting.export.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportGroupDto;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportResponse;
import de.emc.mitglieder.reporting.dto.MembersDirectoryReportSubGroupDto;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class MembersDirectoryPdfService {

    public byte[] createPdf(MembersDirectoryReportResponse report) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        Document document =
                new Document(PageSize.A4.rotate(), 15, 15, 120, 45);

        PdfWriter writer =
                PdfWriter.getInstance(document, outputStream);

        writer.setPageEvent(
                new LandscapeReportTemplate(
                        "Mitgliederverzeichnis"
                )
        );

        Font headerFont =
                new Font(Font.TIMES_ROMAN, 9, Font.BOLD);

        Font tableFont =
                new Font(Font.HELVETICA, 8);

        Font groupFont =
                new Font(Font.TIMES_ROMAN, 10, Font.BOLD);

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern("dd.MM.yyyy");

        document.open();

        for (MembersDirectoryReportGroupDto group : report.getGroups()) {

            int statusCount =
                    group.getStimmgruppen()
                            .stream()
                            .mapToInt(stimmgruppe -> stimmgruppe.getMitglieder().size())
                            .sum();

            String mitgliedsstatus =
                    group.getMitgliedsstatus() == null
                            || group.getMitgliedsstatus().isBlank()
                            ? "Ohne Mitgliederstatus"
                            : group.getMitgliedsstatus();

            for (MembersDirectoryReportSubGroupDto stimmgruppe : group.getStimmgruppen()) {

                String stimme =
                        stimmgruppe.getStimme() == null
                                || stimmgruppe.getStimme().isBlank()
                                || "keine".equalsIgnoreCase(stimmgruppe.getStimme())
                                ? "Ohne Stimme"
                                : stimmgruppe.getStimme();

                PdfPTable table = new PdfPTable(10);
                table.setWidthPercentage(100);

                table.setWidths(new float[]{
                        15f, 14f, 5f, 12f, 19f,
                        8f, 12f, 12f, 12f, 20f
                });

                PdfPCell statusCell =
                        createGroupCell(
                                mitgliedsstatus
                                        + " ("
                                        + statusCount
                                        + ")",
                                groupFont
                        );
                statusCell.setColspan(10);
                table.addCell(statusCell);

                PdfPCell stimmeCell =
                        createGroupCell(
                                stimme
                                        + " ("
                                        + stimmgruppe.getMitglieder().size()
                                        + ")",
                                groupFont
                        );
                stimmeCell.setColspan(10);
                table.addCell(stimmeCell);

                table.addCell(createHeaderCell("Nachname", headerFont));
                table.addCell(createHeaderCell("Vorname", headerFont));
                table.addCell(createHeaderCell("PLZ", headerFont));
                table.addCell(createHeaderCell("Ort", headerFont));
                table.addCell(createHeaderCell("Straße", headerFont));
                table.addCell(createHeaderCell("Geboren", headerFont));
                table.addCell(createHeaderCell("Tel. privat", headerFont));
                table.addCell(createHeaderCell("Tel. gesch.", headerFont));
                table.addCell(createHeaderCell("Mobil", headerFont));
                table.addCell(createHeaderCell("E-Mail", headerFont));

                table.setHeaderRows(3);

                for (var mitglied : stimmgruppe.getMitglieder()) {

                    table.addCell(createDataCell(nullToEmpty(mitglied.getNachname()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getVorname()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getPlz()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getOrt()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getStrasseHausNr()), tableFont));

                    table.addCell(
                            createDataCell(
                                    mitglied.getGeburtsdatum() != null
                                            ? mitglied.getGeburtsdatum().format(dateFormatter)
                                            : "",
                                    tableFont
                            )
                    );

                    table.addCell(createDataCell(nullToEmpty(mitglied.getTelefonPrivat()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getTelefonGeschaeftlich()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getMobiltelefon()), tableFont));
                    table.addCell(createDataCell(nullToEmpty(mitglied.getEmail()), tableFont));
                }

                document.add(table);
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph(" "));
        }

        document.close();

        return outputStream.toByteArray();
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    private PdfPCell createGroupCell(String text, Font font) {
        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(4f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        return cell;
    }

    private PdfPCell createHeaderCell(String text, Font font) {
        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setBorder(PdfPCell.BOTTOM);

        cell.setPaddingTop(2f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        return cell;
    }

    private PdfPCell createDataCell(String text, Font font) {
        PdfPCell cell =
                new PdfPCell(new Phrase(text, font));

        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        return cell;
    }
}