package de.emc.mitglieder.reporting.export.pdf;

import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LandscapeReportTemplate extends PdfPageEventHelper {

    private final String reportTimestamp =
            LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")
            );

    private PdfTemplate totalPageTemplate;

    private final String reportTitle;

    public LandscapeReportTemplate(String reportTitle) {
        this.reportTitle = reportTitle;
    }

    @Override
    public void onOpenDocument(
            PdfWriter writer,
            com.lowagie.text.Document document
    ) {
        totalPageTemplate =
                writer.getDirectContent().createTemplate(30, 12);
    }

    @Override
    public void onEndPage(
            PdfWriter writer,
            com.lowagie.text.Document document
    ) {

        try {
            URL headerUrl = getClass()
                    .getResource("/reporting/Kopfzeile_EMC-HWK-Logo.png");

            if (headerUrl == null) {
                throw new IllegalStateException(
                        "Kopfzeile_EMC-HWK-Logo.png nicht gefunden"
                );
            }

            Image headerImage =
                    Image.getInstance(headerUrl.toExternalForm());

            headerImage.scalePercent(20f);

            float imageX =
                    (document.right() - document.left()
                            - headerImage.getScaledWidth()) / 2
                            + document.leftMargin();

            float imageY =
                    document.top() + 20;

            headerImage.setAbsolutePosition(
                    imageX,
                    imageY
            );

            writer.getDirectContent().addImage(headerImage);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Kopfzeile konnte nicht erzeugt werden",
                    ex
            );
        }

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_LEFT,
                new Phrase(
                        reportTitle,
                        PdfFonts.REPORT_TITLE
                ),
                document.left(),
                document.top() + 10,
                0
        );

        float y = document.bottom() - 10;

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_LEFT,
                new Phrase(
                        "Stand: " + reportTimestamp,
                        PdfFonts.FOOTER
                ),
                document.left(),
                y,
                0
        );

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_CENTER,
                new Phrase(
                        "Erfurter Männerchor 1890 e.V.",
                        PdfFonts.FOOTER
                ),
                (document.right() - document.left()) / 2
                        + document.leftMargin(),
                y,
                0
        );

        String pageText =
                "Seite " + writer.getPageNumber() + " von ";

        float textBase =
                document.right() - 35;

        ColumnText.showTextAligned(
                writer.getDirectContent(),
                Element.ALIGN_RIGHT,
                new Phrase(
                        pageText,
                        PdfFonts.FOOTER
                ),
                textBase,
                y,
                0
        );

        writer.getDirectContent().addTemplate(
                totalPageTemplate,
                textBase + 2,
                y
        );
    }

    @Override
    public void onCloseDocument(
            PdfWriter writer,
            com.lowagie.text.Document document
    ) {

        ColumnText.showTextAligned(
                totalPageTemplate,
                Element.ALIGN_LEFT,
                new Phrase(
                        String.valueOf(
                                writer.getPageNumber() - 1
                        ),
                        PdfFonts.FOOTER
                ),
                0,
                0,
                0
        );
    }
}