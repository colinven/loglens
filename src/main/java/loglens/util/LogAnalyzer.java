package loglens.util;

import loglens.dto.LogAnalysis;
import loglens.dto.LogEntry;

import java.time.Instant;
import java.util.List;

public class LogAnalyzer {

    public LogAnalysis analyze(List<LogEntry> logEntries, Instant analysisWindowStartTime) {
        return null;
    }
    /*

      This class will be responsible for generating reports based on the list of log entries.
      All methods will take List<LogEntry> as a parameter, and stream/filter as necessary for each metric.

      Reporting methods:
      - EnumMap<HttpStatus, Long> getStatusCodeDistribution() —> return a statusCodeDistributionMap containing the
        number of requests associated with each present status code

      - Map<String, URITrafficData> getTrafficByURI() —> return a uriTrafficMetricsMap containing the total bytes and requests
        made to a given URI/endpoint

      - double getErrorRate() —>  return a double value representing the percentage of requests with 5xx status codes

      - Map<String, Long> getTopNIpsByReqCount(n) —> return a requestCountByIpMap with n entries representing the top Ips
        by request count (who made the most requests to the server)

     */
}
