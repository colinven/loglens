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

    private final CombinedLogParser combinedLogParser;
    private final CommonLogParser commonLogParser;
    private final JsonLogParser jsonLogParser; // { JSON PARSING NOT YET IMPLEMENTED }

    private int totalFilesSkipped = 0;
    private long totalLinesSkipped = 0;

    public FileWalker() {
        combinedLogParser = new CombinedLogParser();
        commonLogParser = new CommonLogParser();
        jsonLogParser = new JsonLogParser();  // { JSON PARSING NOT YET IMPLEMENTED }
    }

    /**
     * Reads all valid log entries from all files/subdirectories within a provided directory and accumulates
     * them into a list.
     * @param directory the directory to traverse
     * @return a list containing all valid log entries within all subdirectories of the provided directory.
     * If no valid entries are found, this method returns an empty list.
     */
    public List<LogEntry> processFiles(Path directory) {

        List<LogEntry> allEntries = List.of();

        try (Stream<Path> paths = Files.walk(directory)) {

            allEntries = paths.filter(Files::isRegularFile)
                    .map(path -> {
                        try {
                            return parseFile(path);
                        } catch (IOException e) {
                            // If file is unreadable, skip it, keep moving
                            System.err.println("[loglens]: skipping " + path + ": " + e.getMessage());
                            return List.<LogEntry>of();
                        }
                    })
                    .filter(list -> list != null && !list.isEmpty())
                    .flatMap(Collection::stream)
                    .toList();

        } catch (IOException e) {
            System.err.println("[loglens]: Error: unable to walk " + directory + ": " + e.getMessage());
            System.exit(1);
        }
        return allEntries;
    }

    /**
     * Reads a single file at the provided path, parses all valid log line entries into LogEntry objects,
     * and accumulates them into a list.
     * <p>Side effect: increments totalLinesSkipped and totalFilesSkipped when a line/file is not parsable.</p>
     * @param path the path to the log file
     * @return a List<LogEntry> containing every valid log entry parsed from the provided path
     * @throws IOException when unable to read file
     */
    private List<LogEntry> parseFile(Path path) throws IOException {

        List<LogEntry> logEntries = List.of();

        InputStream rawStream = Files.newInputStream(path);
        BufferedInputStream sniffStream = new BufferedInputStream(rawStream);
        boolean gzipped = isGzipStream(sniffStream);
        try(
            InputStream finalStream = gzipped ? new GZIPInputStream(sniffStream) : sniffStream;
            BufferedReader reader = new BufferedReader(new InputStreamReader(finalStream));) {

            LogParser parser = detectLogParser(reader);

            // No valid log format was detected —> unable to parse file —> bail
            if (parser == null) {
                System.err.println("[loglens]: Could not parse " + path + ", skipping file...");
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

        }
        return logEntries;
    }

    /**
     * Reads first 2 bytes of a file input stream and attempts to match them against GZIP magic number (0x1F 0x8B)
     * @param in input stream to check
     * @return true if stream contains Gzip header, false otherwise
     * @throws IOException if unable to read stream
     */
    private boolean isGzipStream(BufferedInputStream in) throws IOException {
        in.mark(2);
        try {
            int byte1 = in.read();
            int byte2 = in.read();
            return (byte1 == 0x1F) && (byte2 == 0x8B);
        } finally {
            in.reset();
        }
    }

    /**
     * Sniffs first 5 lines from reader, detects which LogParser impl matches the format, and returns
     * that particular implementation.
     * @param reader BufferedReader to read from
     * @return LogParser impl corresponding to the log format read from reader, or NULL if no match is found.
     * @throws IOException if unable to read stream
     */
    private LogParser detectLogParser(BufferedReader reader) throws IOException {

        LogParser detectedParser = null;

        final int TEST_LINE_LIMIT = 5; // Max number of lines to attempt to read/assign before skipping file entirely
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

    public int getTotalFilesSkipped() {
        return totalFilesSkipped;
    }

    public long getTotalLinesSkipped() {
        return totalLinesSkipped;
    }
}

