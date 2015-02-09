package org.obicere.cc.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author Matt Zawislak
 */
public class PrintFormatter extends Formatter {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static final DateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss");

    @Override
    public String format(final LogRecord record) {
        final StringBuilder message = new StringBuilder();

        message.append(SIMPLE_DATE_FORMAT.format(new Date(record.getMillis())));
        message.append(" ");
        message.append(record.getLevel().getLocalizedName());
        message.append(": ");
        message.append(formatMessage(record));
        message.append(LINE_SEPARATOR);

        if (record.getThrown() != null) {
            try {
                final StringWriter sw = new StringWriter();
                final PrintWriter pw = new PrintWriter(sw);
                record.getThrown().printStackTrace(pw);
                pw.close();
                message.append(sw);
            } catch (final Exception ignored) {
            }
        }

        return message.toString();
    }
}
