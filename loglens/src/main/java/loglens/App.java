package loglens;

import loglens.processing.FileWalker;
import picocli.CommandLine;

import java.nio.file.Path;

@CommandLine.Command(name = "loglens", version = "loglens 1.0", mixinStandardHelpOptions = true)
public class App implements Runnable {

    @CommandLine.Option(names = { "-l", "--last" }, paramLabel = "last",
            description = """
                    Relative time measurement specifying how far in the past the log-analysis window should begin.
                    Acceptable formats: 'd' - days, 'h' - hours, 'm' - minutes.
                    (e.g. 2d 12h 30m — only process logs younger than 2 days, 12 hours, and 30 minutes old.)""")
    private String[] last;

    @CommandLine.Parameters(paramLabel = "<directory>", defaultValue = ".",
        description = "relative path to a directory/file containing log entries to analyze")
    private String directory = ".";

    @Override
    public void run() {
        FileWalker fileWalker = new FileWalker();
        Path cwd = Path.of("").toAbsolutePath();
        Path fullPath = cwd.resolve(directory);
        fileWalker.processFiles(fullPath);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new App()).execute(args);
        System.exit(exitCode);
    }


}
