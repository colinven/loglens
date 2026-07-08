package loglens;

import loglens.dto.LogAnalysis;
import loglens.dto.LogEntry;
import loglens.processing.FileWalker;
import loglens.util.LogAnalyzer;
import loglens.util.TimeParser;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@CommandLine.Command(name = "loglens", version = "loglens 1.0", mixinStandardHelpOptions = true)
public class App implements Runnable {

    private final FileWalker fileWalker;
    private final LogAnalyzer logAnalyzer;
    private final TimeParser timeParser;

    public App() {
        this.fileWalker = new FileWalker();
        this.logAnalyzer = new LogAnalyzer();
        this.timeParser = new TimeParser();
    }

    @CommandLine.Option(names = { "-l", "--last" }, defaultValue = "",
            description = """
                    Relative time duration specifying how far in the past the log-analysis window should begin.
                    Acceptable formats: 'd' - days, 'h' - hours, 'm' - minutes, preceded by a non-negative integer.
                    Multiple time measurements must be wrapped in double quotes.
                    (e.g. "2d 12h 30m" — only analyze logs younger than 2 days, 12 hours, and 30 minutes old.)""")
    private String inputDuration;

    @CommandLine.Parameters(paramLabel = "<directory>", defaultValue = "",
        description = "relative path to a directory/file containing log entries to analyze")
    private String directory;

    @Override
    public void run() {

        Path filePath = Path.of(directory).toAbsolutePath().normalize(); // Resolve filepath
        if (Files.notExists(filePath)) {
            System.err.println("[loglens]: Error: No such file or directory");
            System.exit(1);
        }
        System.out.println("[loglens]: Reading files from: " + filePath);

        Instant resolvedStartTime = null;
        // If '--last' arg passed, resolve time window start
        if (!inputDuration.isBlank()) {
            if ((resolvedStartTime = timeParser.parseAndSubtract(inputDuration, Instant.now())) == null) {
                // Parser rejected duration input
                System.err.printf("[loglens]: Unable to parse input duration: \"%s\". Defaulting to no time window...%n", inputDuration);
            }
        }
        List<LogEntry> logEntries = fileWalker.processFiles(filePath); // Process all log entries
        LogAnalysis analysis = logAnalyzer.analyze(logEntries, resolvedStartTime); // Generate full analysis

        analysis.print(); // Print results
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }


}
