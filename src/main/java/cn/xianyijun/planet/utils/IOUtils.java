package cn.xianyijun.planet.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Io utils.
 */
public class IOUtils {
    private static final int BUFFER_SIZE = 1024 * 8;

    private IOUtils() {
    }

    /**
     * Write long.
     *
     * @param is the is
     * @param os the os
     * @return the long
     * @throws IOException the io exception
     */
    public static long write(InputStream is, OutputStream os) throws IOException {
        return write(is, os, BUFFER_SIZE);
    }

    /**
     * Write long.
     *
     * @param is         the is
     * @param os         the os
     * @param bufferSize the buffer size
     * @return the long
     * @throws IOException the io exception
     */
    public static long write(InputStream is, OutputStream os, int bufferSize) throws IOException {
        int read;
        long total = 0;
        byte[] buff = new byte[bufferSize];
        while (is.available() > 0) {
            read = is.read(buff, 0, buff.length);
            if (read > 0) {
                os.write(buff, 0, read);
                total += read;
            }
        }
        return total;
    }

    /**
     * Read string.
     *
     * @param reader the reader
     * @return the string
     * @throws IOException the io exception
     */
    public static String read(Reader reader) throws IOException {
        StringWriter writer = new StringWriter();
        try {
            write(reader, writer);
            return writer.getBuffer().toString();
        } finally {
            writer.close();
        }
    }

    /**
     * Write long.
     *
     * @param writer the writer
     * @param string the string
     * @return the long
     * @throws IOException the io exception
     */
    public static long write(Writer writer, String string) throws IOException {
        Reader reader = new StringReader(string);
        try {
            return write(reader, writer);
        } finally {
            reader.close();
        }
    }

    /**
     * Write long.
     *
     * @param reader the reader
     * @param writer the writer
     * @return the long
     * @throws IOException the io exception
     */
    public static long write(Reader reader, Writer writer) throws IOException {
        return write(reader, writer, BUFFER_SIZE);
    }

    /**
     * Write long.
     *
     * @param reader     the reader
     * @param writer     the writer
     * @param bufferSize the buffer size
     * @return the long
     * @throws IOException the io exception
     */
    public static long write(Reader reader, Writer writer, int bufferSize) throws IOException {
        int read;
        long total = 0;
        char[] buf = new char[BUFFER_SIZE];
        while ((read = reader.read(buf)) != -1) {
            writer.write(buf, 0, read);
            total += read;
        }
        return total;
    }

    /**
     * Read lines string [ ].
     *
     * @param file the file
     * @return the string [ ]
     * @throws IOException the io exception
     */
    public static String[] readLines(File file) throws IOException {
        if (file == null || !file.exists() || !file.canRead()) {
            return new String[0];
        }

        return readLines(new FileInputStream(file));
    }

    /**
     * Read lines string [ ].
     *
     * @param is the is
     * @return the string [ ]
     * @throws IOException the io exception
     */
    public static String[] readLines(InputStream is) throws IOException {
        List<String> lines = new ArrayList<String>();
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
            return lines.toArray(new String[0]);
        } finally {
            reader.close();
        }
    }

    /**
     * Write lines.
     *
     * @param os    the os
     * @param lines the lines
     * @throws IOException the io exception
     */
    public static void writeLines(OutputStream os, String[] lines) throws IOException {
        PrintWriter writer = new PrintWriter(new OutputStreamWriter(os));
        try {
            for (String line : lines) {
                writer.println(line);
            }
            writer.flush();
        } finally {
            writer.close();
        }
    }

    /**
     * Write lines.
     *
     * @param file  the file
     * @param lines the lines
     * @throws IOException the io exception
     */
    public static void writeLines(File file, String[] lines) throws IOException {
        if (file == null) {
            throw new IOException("File is null.");
        }
        writeLines(new FileOutputStream(file), lines);
    }

    /**
     * Append lines.
     *
     * @param file  the file
     * @param lines the lines
     * @throws IOException the io exception
     */
    public static void appendLines(File file, String[] lines) throws IOException {
        if (file == null) {
            throw new IOException("File is null.");
        }
        writeLines(new FileOutputStream(file, true), lines);
    }
}
