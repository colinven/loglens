# LogLens

A command-line tool that parses web-server access logs and prints terminal bar-chart analytics — request counts by status code, top URIs, top IPs, and error rates.

## About

LogLens is a personal learning project. It's my sandbox for practicing Java, CLI design with [picocli](https://picocli.info/), file/stream parsing, and building a native binary with GraalVM.

## Features

- Recursively walks a directory (or reads a single file) looking for log files
- Auto-detects log format per file: Common Log Format and Combined Log Format are currently supported
- Transparently decompresses gzip-compressed log files
- Reports how many lines/files were skipped because they couldn't be parsed
- `--last "<duration>"` — filters analysis to entries within a relative time window (e.g. only the last 2 days)
- Renders ASCII bar charts for:
  - Request count by HTTP status
  - Top URIs by request count
  - Top URIs by bytes sent
  - Top 10 highest-requesting IPs
- Reports 4xx and 5xx error rate percentages

## How It Works

```
              <directory>  (CLI argument)
                       │
                       ▼
┌────────────────────────────────────────────┐
│          FileWalker.processFiles()         │
│      walk directory tree recursively       │
└────────────────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────┐
│         FileWalker.isGzipStream()          │
│          sniff gzip magic bytes,           │
│            decompress if needed            │
└────────────────────────────────────────────┘
                       │
                       ▼
┌────────────────────────────────────────────┐
│            FileWalker.parseFile()          │
│    detect log format (Common/Combined),    │
│     parse matching lines, track skips      │
└────────────────────────────────────────────┘
                       │  List<LogEntry>
                       ▼
┌────────────────────────────────────────────┐
│           LogAnalyzer.analyze()            │
│          filter by --last window,          │
│       aggregate by status / URI / IP       │
└────────────────────────────────────────────┘
                       │  LogAnalysis
                       ▼
┌────────────────────────────────────────────┐
│        LogAnalyzer.printAnalysis()         │
│             render bar charts,             │
│            compute error rates             │
└────────────────────────────────────────────┘
                       │
                       ▼
                   stdout  (bar charts + stats)
```

## Roadmap / Not yet implemented

These are designed but not built yet:

- JSON/JSONL log parsing (the parser is stubbed out)
- Absolute time filters: `--since` / `--until`
- Named calendar windows: `--today`, `--yesterday`

## Installation & Build

**Prerequisites:** JDK 17+, Maven.

Build the jar and run it with `java`:

```bash
mvn package
java -cp target/classes:$(find ~/.m2 -name "picocli-4.7.7.jar") loglens.App <directory>
```

### Optional (but recommended for snappy execution): native image (GraalVM)

If you have a GraalVM JDK installed, you can build a standalone native binary:

```bash
mvn -Pnative package
./target/loglens <directory>
```

## Usage

```
loglens [-l|--last "<duration>"] <directory>
```

- `<directory>` — path to a directory or file containing log entries to analyze
- `-l, --last "<duration>"` — relative time window, e.g. `"2d 12h 30m"` (days/hours/minutes, combinable; quote multi-part durations)

### Example

Run against the sample logs in `test_logs/` (includes Common Log Format, Combined Log Format, a gzip-compressed file, and an invalid file to demonstrate skip handling):

```bash
loglens test_logs/
```

```
[loglens]: Could not parse test_logs/bad.log, skipping file...
————— Request Count by HTTP Status ——————
200 OK                     |██████████████████████████████████████████████████  77
500 INTERNAL_SERVER_ERROR  |█████████  14
301 MOVED_PERMANENTLY      |█████  9
204 NO_CONTENT             |█████  8
403 FORBIDDEN              |████  7
201 CREATED                |████  7
401 UNAUTHORIZED           |████  7
404 NOT_FOUND              |███  6
304 NOT_MODIFIED           |███  5
———————————————————————————————————————
————— Top URIs by Request Count ——————
/missing.html          |██████████████████████████████████████████████████  14
/status                |██████████████████████████████████████████  12
/products/list         |███████████████████████████████████████  11
...
————————————————————————————————————
[loglens]: Requests with 4xx status code: 10.0 %
[loglens]: Requests with 5xx status code: 14.3 %

[loglens]: Analysis complete.
[loglens]: Reader skipped 2 lines (unable to parse format).
[loglens]: Reader skipped 1 invalid log files.
```

## Project Structure

```
src/main/java/loglens/
├── App.java          CLI entry point (picocli)
├── dto/               LogEntry, LogAnalysis, URITraffic, HttpMethod, HttpStatus
├── parser/            LogParser interface + Common/Combined/Json parsers
├── processing/        FileWalker (directory walk, gzip/format detection)
└── util/              LogAnalyzer (filtering/aggregation/rendering), TimeParser
```

## Testing

```bash
mvn test
```

Parsers (`CommonLogParser`, `CombinedLogParser`), `TimeParser`, and `FileWalker`'s gzip detection are covered by JUnit 5 + AssertJ tests.
