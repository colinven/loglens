package loglens.dto;

import java.util.EnumMap;
import java.util.Map;

public record LogAnalysis(
        EnumMap<HttpStatus, Long> requestCountByStatusMap,
        Map<String, URITraffic> trafficByURIMap,
        Map<String, Long> requestCountByIpMap,
        double errorRate5xx,
        double errorRate4xx
) {}
