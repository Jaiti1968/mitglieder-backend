package de.emc.mitglieder.reporting.export.pdf;

import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import de.emc.mitglieder.reporting.dto.BirthdayReportMemberDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportMonthGroupDto;
import de.emc.mitglieder.reporting.dto.BirthdayReportResponse;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

@Service
public class BirthdayReportPdfService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static final Font JUBILEE_AGE_FONT =
            new Font(
                    Font.HELVETICA,
                    8,
                    Font.BOLD,
                    new Color(192, 0, 0)
            );

    public byte[] createPdf(BirthdayReportResponse report) {
        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        70.87f,
                        56.69f,
                        150,
                        45
                );

        PdfWriter writer =
                PdfWriter.getInstance(document, outputStream);

        writer.setPageEvent(
                new PortraitReportTemplate(
                        "Geburtstagsliste "
                                + report.getGeschaeftsjahr()
                )
        );

        document.open();

        PdfPTable table = createTable();

        addHeader(table);

        for (BirthdayReportMonthGroupDto group
                : report.getMonthGroups()) {

            addMonthHeading(table, group);

            for (BirthdayReportMemberDto member
                    : group.getMitglieder()) {

                addMemberRow(table, member);
            }
        }

        document.add(table);
        document.close();

        return outputStream.toByteArray();
    }

    private PdfPTable createTable() {
        PdfPTable table = new PdfPTable(6);

        table.setWidthPercentage(100);

        table.setWidths(
                new float[]{
                        19f,
                        11f,
                        17f,
                        20f,
                        18f,
                        8f
                }
        );

        return table;
    }

    private void addHeader(PdfPTable table) {
        table.addCell(
                createHeaderCell("Status")
        );
        table.addCell(
                createHeaderCell("Stimme")
        );
        table.addCell(
                createHeaderCell("Vorname")
        );
        table.addCell(
                createHeaderCell("Nachname")
        );
        table.addCell(
                createHeaderCell("Geburtsdatum")
        );
        table.addCell(
                createHeaderCell("Alter")
        );

        table.setHeaderRows(1);
    }

    private void addMonthHeading(
            PdfPTable table,
            BirthdayReportMonthGroupDto group
    ) {
        String monthName =
                group.getMonatsname() == null
                        || group.getMonatsname().isBlank()
                        ? "Unbekannt"
                        : group.getMonatsname();

        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                monthName,
                                PdfFonts.GROUP_TITLE
                        )
                );

        cell.setColspan(6);
        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingTop(8f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        table.addCell(cell);
    }

    private void addMemberRow(
            PdfPTable table,
            BirthdayReportMemberDto member
    ) {
        table.addCell(
                createDataCell(
                        nullToEmpty(
                                member.getMitgliedsstatus()
                        ),
                        PdfFonts.TABLE_BODY
                )
        );

        table.addCell(
                createDataCell(
                        nullToEmpty(
                                member.getStimme()
                        ),
                        PdfFonts.TABLE_BODY
                )
        );

        table.addCell(
                createDataCell(
                        nullToEmpty(
                                member.getVorname()
                        ),
                        PdfFonts.TABLE_BODY
                )
        );

        table.addCell(
                createDataCell(
                        nullToEmpty(
                                member.getNachname()
                        ),
                        PdfFonts.TABLE_BODY
                )
        );

        table.addCell(
                createDataCell(
                        member.getGeburtsdatum() != null
                                ? member.getGeburtsdatum()
                                .format(DATE_FORMATTER)
                                : "unbekannt",
                        PdfFonts.TABLE_BODY
                )
        );

        Font ageFont =
                Boolean.TRUE.equals(
                        member.getJubilaeumsgeburtstag()
                )
                        ? JUBILEE_AGE_FONT
                        : PdfFonts.TABLE_BODY;

        table.addCell(
                createDataCell(
                        member.getAlterImGeschaeftsjahr()
                                != null
                                ? String.valueOf(
                                member.getAlterImGeschaeftsjahr()
                        )
                                : "",
                        ageFont
                )
        );
    }

    private PdfPCell createHeaderCell(String text) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                text,
                                PdfFonts.TABLE_HEADER
                        )
                );

        cell.setBorder(PdfPCell.BOTTOM);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(3f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        return cell;
    }

    private PdfPCell createDataCell(
            String text,
            Font font
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(text, font)
                );

        cell.setBorder(PdfPCell.NO_BORDER);
        cell.setPaddingTop(2f);
        cell.setPaddingBottom(2f);
        cell.setPaddingLeft(1f);
        cell.setPaddingRight(1f);

        return cell;
    }

    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }
}