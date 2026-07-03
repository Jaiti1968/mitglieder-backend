package de.emc.mitglieder.reporting.export.pdf;

import com.lowagie.text.Font;

public final class PdfFonts {

    public static final Font REPORT_TITLE =
            new Font(Font.TIMES_ROMAN, 12, Font.BOLD);

    public static final Font GROUP_TITLE =
            new Font(Font.TIMES_ROMAN, 10, Font.BOLD);

    public static final Font TABLE_HEADER =
            new Font(Font.TIMES_ROMAN, 9, Font.BOLD);

    public static final Font TABLE_BODY =
            new Font(Font.HELVETICA, 8);

    public static final Font FOOTER =
            new Font(Font.TIMES_ROMAN, 9);

    private PdfFonts() {
    }
}