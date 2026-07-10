package loglens.util;

import loglens.dto.HttpStatus;
import loglens.dto.LogAnalysis;
import loglens.dto.LogEntry;
import loglens.dto.URITraffic;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class LogAnalyzer {

    /**
     * This method renders all bar charts and displays 4xx/5xx error rate percentages.
     * @param logAnalysis analysis to render.
     */
    public void printAnalysis(LogAnalysis logAnalysis) {

        final long TOP_N_IP_LIMIT = 10; // Limit for number of highest-requesting IP addresses

        Map<String, Long> requestCountByStatusMap = logAnalysis.requestCountByStatusMap().entrySet().stream()
                .map(entry -> {
                    String key = String.format("%d %s", entry.getKey().getCode(), entry.getKey().name());
                    return new AbstractMap.SimpleEntry<>(key, entry.getValue());
                })
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Set<Map.Entry<String, URITraffic>> trafficByURIMapEntries = logAnalysis.trafficByURIMap().entrySet();

        Map<String, Long> requestCountByURIMap = trafficByURIMapEntries.stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getRequestCount()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Long> byteCountByURIMap = trafficByURIMapEntries.stream()
                .map(entry -> new AbstractMap.SimpleEntry<>(entry.getKey(), entry.getValue().getByteCount()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        Map<String, Long> requestCountByIpMap = logAnalysis.requestCountByIpMap().entrySet().stream()
                .limit(TOP_N_IP_LIMIT)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        renderBarChart(requestCountByStatusMap, "Request Count by HTTP Status");
        renderBarChart(requestCountByURIMap, "Top URIs by Request Count");
        renderBarChart(byteCountByURIMap, "Top URIs by Bytes Sent");
        renderBarChart(requestCountByIpMap, "Top 10 Highest-Requesting IPs");
        System.out.println();

        System.out.printf("[loglens]: Requests with 4xx status code: %.1f %s%n", (logAnalysis.errorRate4xx() * 100), "%");
        System.out.printf("[loglens]: Requests with 5xx status code: %.1f %s%n", (logAnalysis.errorRate5xx() * 100), "%");
    }

    private void renderBarChart(Map<String, Long> data, String chartTitle) {

        // List of all data entries sorted in descending order
        List<Map.Entry<String, Long>> entries = data.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .toList();

        final int MAX_LABEL_WIDTH = 30; // The max amount of chars that a bar label can be before truncation
        final String GRAPH_HEADER = "————— " + chartTitle + " ——————";
        final String GRAPH_FOOTER = "—".repeat(GRAPH_HEADER.length() - 2);

        // Fixed label width for the entire chart: length of longest label or MAX_LABEL_WIDTH
        int adjustedLabelWidth = Math.min(MAX_LABEL_WIDTH,
                entries.stream().mapToInt(e -> e.getKey().length()).max().orElse(0));

        // Row format with proper offset determined by adjustedLabelWidth
        final String GRAPH_ROW_FORMAT = "%-" + adjustedLabelWidth + "s  |%s  %d%n";

        // The largest value within the data set. Used to scale bars to proportionate lengths
        long maxNumericalValue = entries.stream().mapToLong(Map.Entry::getValue).max().orElse(0);

        // Begin render...
        System.out.println(GRAPH_HEADER);
        for (var entry : entries) {
            String label = entry.getKey();
            if (label.length() > adjustedLabelWidth) {
                label = label.substring(0, adjustedLabelWidth - 1) + "…";
            }
            String bar = "█".repeat(getScaledBarWidth(entry.getValue(), maxNumericalValue));
            System.out.printf(GRAPH_ROW_FORMAT, label, bar, entry.getValue());
        }
        System.out.println(GRAPH_FOOTER);
    }

    /**
     * This method determines the correct renderable bar length based on the proportion of (value/maxValue).
     * @param value the value of the bar to render.
     * @param maxValue the maximum value within the dataset (used to compute proportion).
     * @return an integer representing the correct width of the bar.
     */
    private int getScaledBarWidth(long value, long maxValue) {
        final int MAX_BAR_WIDTH = 50;
        double proportion = (double) value / maxValue;
        double scaledBarWidth = MAX_BAR_WIDTH * proportion;
        return (scaledBarWidth > 0 && scaledBarWidth < 1) ? 1 : (int) scaledBarWidth;
    }

    /**
     * This method filters a list of LogEntries after analysisWindowStartTime,analyzes them,
     * and constructs a LogAnalysis object.
     * @param logEntries list of log entries to analyze.
     * @param analysisWindowStartTime Instant value representing the beginning of the analysis window. LogAnalysis
     *                                will be filtered based on entries AFTER this timestamp.
     * @return a LogAnalysis object containing analyzed data on logEntries.
     */
    public LogAnalysis analyze(List<LogEntry> logEntries, Instant analysisWindowStartTime) {

        List<LogEntry> timeFiltered = analysisWindowStartTime == null ? logEntries
                : logEntries.stream()
                    .filter(entry -> entry.timestamp().isAfter(analysisWindowStartTime))
                    .toList();

        return new LogAnalysis(
                buildRequestCountByStatusMap(timeFiltered),
                buildTrafficByURIMap(timeFiltered),
                buildRequestCountByIpMap(timeFiltered),
                get4xxErrorRate(timeFiltered),
                get5xxErrorRate(timeFiltered)
        );
    }

    private EnumMap<HttpStatus, Long> buildRequestCountByStatusMap(List<LogEntry> logEntries) {
        EnumMap<HttpStatus, Long> map = new EnumMap<>(HttpStatus.class);
        for (LogEntry logEntry : logEntries) {
            map.merge(logEntry.status(), 1L, Long::sum);
        }
        return map;
    }

    private Map<String, URITraffic> buildTrafficByURIMap(List<LogEntry> logEntries) {
        Map<String, URITraffic> map = new HashMap<>();
        for (LogEntry logEntry : logEntries) {
            map.computeIfAbsent(logEntry.uri(), uri -> new URITraffic())
                    .add(logEntry.bytesSent(), 1);
        }
        return map;
    }

    private Map<String, Long> buildRequestCountByIpMap(List<LogEntry> logEntries) {
        Map<String, Long> map = new HashMap<>();
        for (LogEntry logEntry : logEntries) {
            map.merge(logEntry.ip(), 1L, Long::sum);
        }
        return map;
    }

    private double get4xxErrorRate(List<LogEntry> logEntries) {
        long totalEntries = logEntries.size();
        long total4xxEntries = logEntries.stream()
                .map(LogEntry::status)
                .filter(status -> status.getCode() >= 400 && status.getCode() < 500)
                .count();
        return (double) total4xxEntries / totalEntries;
    }

    private double get5xxErrorRate(List<LogEntry> logEntries) {
        long totalEntries = logEntries.size();
        long total5xxEntries = logEntries.stream()
                .map(LogEntry::status)
                .filter(status -> status.getCode() >= 500)
                .count();
        return (double) total5xxEntries / totalEntries;
    }
}
