package loglens.processing;

import loglens.dto.LogEntry;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class FileWalker {

    public void processFiles(Path directory) {

        if (Files.notExists(directory)) {
            System.err.println("loglens: error: No such file or directory");
            System.exit(1);
        }

        List<LogEntry> allEntries;

        try (Stream<Path> paths = Files.walk(directory)) {

            allEntries = paths.filter(Files::isRegularFile)
                    .map(this::parseFile)
                    .filter(Objects::nonNull)
                    .flatMap(List::stream)
                    .toList();

        } catch (IOException e) {
            System.err.println("loglens: error: could not process files");
            System.exit(1);
        }
    }

    private List<LogEntry> parseFile(Path path) {
        /*
            1. Open files InputStream
            2. use PushbackInputStream to sniff 0x1F 0x8B (if match, file is gzipped, wrap in GZipInputStream)
            3. wrap in InputStreamReader
            4. wrap in BufferedReader (allows us to use mark()/reset()) for detection loop.
            5. detection loop:
                - read first n lines, checking each line to see if any LogParser impl matches format.
                - if n don't match, skip file entirely.
                - if match is found, read remainder of file with that Parser impl.
            6. parse line by line -> LogEntry -> accumulate into List
         */
        return null;
    }
}

