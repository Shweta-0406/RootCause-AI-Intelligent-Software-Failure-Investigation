
package com.rootcauseai.rootcause_ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final LogRepository logRepository;

    public AnalysisService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public AnalysisSummary analyzeLogs() {

        List<Log> logs = logRepository.findAll();
        List<Map<String, Object>> results = new ArrayList<>();
        int errorCount = 0;

        for (Log log : logs) {

            Map<String, Object> result = new HashMap<>();

            String message = log.getMessage() == null
                    ? ""
                    : log.getMessage().toLowerCase(Locale.ROOT);

            String severity = log.getSeverity() == null
                    ? "UNKNOWN"
                    : log.getSeverity().toUpperCase(Locale.ROOT);

            if ("ERROR".equals(severity)
                    || "FATAL".equals(severity)) {
                errorCount++;
            }

            result.put("logId", log.getId());
            result.put("timestamp", log.getTimestamp());
            result.put("serviceName", log.getServiceName());
            result.put("severity", severity);
            result.put("evidence", log.getMessage());
            result.put("traceId", log.getTraceId());

            if ("INFO".equals(severity)
                    || "DEBUG".equals(severity)
                    || "TRACE".equals(severity)) {

                result.put("rootCause",
                        "No error analysis needed for this informational log");
                result.put("recommendation",
                        "No action required unless related errors or warnings appear");
                result.put("confidence",
                        "Not applicable - informational log");

            } else if ("WARN".equals(severity)
                    || "WARNING".equals(severity)) {

                result.put("rootCause",
                        "Warning detected - the message needs further investigation");
                result.put("recommendation",
                        "Review this warning and check related logs for possible issues");
                result.put("confidence",
                        "Low - warning-based assessment");

            } else if ("ERROR".equals(severity)
                    || "FATAL".equals(severity)) {

                if (message.contains("timeout")) {

                    result.put("rootCause",
                            "Possible timeout involving a dependency or database");
                    result.put("recommendation",
                            "Check database availability, connection pool, and network connectivity");
                    result.put("confidence",
                            "Low - rule-based result");

                } else if (message.contains("connection refused")) {

                    result.put("rootCause",
                            "Possible target service or port is unavailable");
                    result.put("recommendation",
                            "Check whether the target service is running and the port is correct");
                    result.put("confidence",
                            "Low - rule-based result");

                } else if (message.contains("outofmemory")
                        || message.contains("out of memory")
                        || message.contains("heap space")) {

                    result.put("rootCause",
                            "Possible memory exhaustion");
                    result.put("recommendation",
                            "Inspect memory usage, heap settings, and application logs");
                    result.put("confidence",
                            "Low - rule-based result");

                } else {

                    result.put("rootCause",
                            "Unknown cause - more evidence is needed");
                    result.put("recommendation",
                            "Inspect related logs, stack traces, and events before this error");
                    result.put("confidence",
                            "Low - insufficient evidence");
                }

            } else {

                result.put("rootCause",
                        "Cannot classify this log because its severity is unknown");
                result.put("recommendation",
                        "Check the log format and severity level");
                result.put("confidence",
                        "Low - insufficient evidence");
            }

            results.add(result);
        }

        return new AnalysisSummary(logs.size(), errorCount, results);
    }
}
