package loglens.processing;

import loglens.dto.LogEntry;
import loglens.parser.CombinedLogParser;
import loglens.parser.CommonLogParser;
import loglens.parser.JsonLogParser;
import loglens.parser.LogParser;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;

public class FileWalker {

    private int totalFilesSkipped = 0;
    private long totalLinesSkipped = 0;

    public List<LogEntry> processFiles(Path directory) {

        List<LogEntry> allEntries = List.of();

        try (Stream<Path> paths = Files.walk(directory)) {

            allEntries = paths.filter(Files::isRegularFile)
                    .map(this::parseFile)
                    .filter(list -> list != null && !list.isEmpty())
                    .flatMap(Collection::stream)
                    .toList();

        } catch (IOException e) {
            System.err.println("loglens: Error: could not process files");
            System.exit(1);
        }
        return allEntries;
    }

    private List<LogEntry> parseFile(Path path) {

        List<LogEntry> logEntries = List.of();

        try(InputStream rawStream = Files.newInputStream(path);
            InputStream finalStream = (isGzipStream(rawStream)) ? new GZIPInputStream(rawStream) : rawStream;
            BufferedReader reader = new BufferedReader(new InputStreamReader(finalStream));) {

            LogParser parser = detectLogParser(reader);

            // No valid log format was detected —> unable to parse file —> bail
            if (parser == null) {
                totalFilesSkipped++;
                return logEntries;
            }

            Stream<String> lines = reader.lines();

            List<Optional<LogEntry>> unfilteredEntries = lines.map(parser::parseLogEntry).toList();

            // Compute number of skipped lines in this file (lines which parser could not parse)
            long linesSkipped = unfilteredEntries.stream()
                    .filter(Optional::isEmpty)
                    .count();
            totalLinesSkipped += linesSkipped;

            // Filter all non-empty LogEntry objs
            logEntries = unfilteredEntries.stream()
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .toList();

        } catch (IOException e) {
            System.err.printf("[loglens]: Error: could not read file \"%s\"%n", path);
        }
        return logEntries;
    }

    private boolean isGzipStream(InputStream inputStream) throws IOException {

        try (PushbackInputStream pbStream = new PushbackInputStream(inputStream, 2)) {

            // Read first 2 bytes of stream
            int byte1 = pbStream.read();
            int byte2 = pbStream.read();

            // Check for EOF
            if (byte1 == -1 || byte2 == -1) {
                // Pushback any bytes we were able to read
                if (byte1 != -1) pbStream.unread(byte1);
                if (byte2 != -1) pbStream.unread(byte2);
                return false;
            }

            // Sniff for Gzip magic number
            boolean isGzip = (byte1 == 0x1F) && (byte2 == 0x8B);

            // Unread
            pbStream.unread(byte1);
            pbStream.unread(byte2);

            return isGzip;
        }
    }

    private LogParser detectLogParser(BufferedReader reader) throws IOException {

        var combinedLogParser = new CombinedLogParser();
        var commonLogParser = new CommonLogParser();
        var jsonLogParser = new JsonLogParser();

        LogParser detectedParser = null;

        final int TEST_LINE_LIMIT = 5; // Read maximum of 5 lines to determine LogParser impl
        final int READ_AHEAD_LIMIT = 8192; // mark() buffer size — 8KB (maybe overkill)

        int linesRead = 0;

        reader.mark(READ_AHEAD_LIMIT);

        // Sniff first n lines to detect format and assign parser
        String line;
        while ((line = reader.readLine()) != null && linesRead < TEST_LINE_LIMIT) {
            if (combinedLogParser.matches(line)) {
                detectedParser = combinedLogParser;
                break;
            }
            else if (commonLogParser.matches(line)) {
                detectedParser = commonLogParser;
                break;
            }
            else if (jsonLogParser.matches(line)) {
                detectedParser = jsonLogParser;
                break;
            }
            linesRead++;
        }
        reader.reset();
        return detectedParser;
    }
}

