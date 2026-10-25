
package com.rootcauseai.rootcause_ai;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "*")
public class LogController {

    private final LogRepository logRepository;

    public LogController(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    private static final Pattern TIMESTAMP_PATTERN = Pattern.compile(
        "^(\\d{4}-\\d{2}-\\d{2}[ T]\\d{2}:\\d{2}:\\d{2})"
    );

    private static final Pattern SERVICE_PATTERN = Pattern.compile(
        "\\b(?:service|application|app)\\s*[=:]\\s*([a-zA-Z0-9._-]+)",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern BRACKET_SERVICE_PATTERN = Pattern.compile(
        "\\[([a-zA-Z0-9._-]+)\\]"
    );

    private static final Pattern SEVERITY_PATTERN = Pattern.compile(
        "\\b(ERROR|FATAL|WARN|WARNING|INFO|DEBUG|TRACE)\\b",
        Pattern.CASE_INSENSITIVE
    );

    private static final DateTimeFormatter SPACE_TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final DateTimeFormatter ISO_TIMESTAMP_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> uploadLog(
            @RequestParam("file") MultipartFile file) {

        Map<String, Object> response = new HashMap<>();

        if (file == null || file.isEmpty()) {
            response.put("success", false);
            response.put("message", "Please select a log file.");
            return ResponseEntity.badRequest().body(response);
        }

        int savedCount = 0;
        int skippedCount = 0;
        int invalidCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                    file.getInputStream(),
                    StandardCharsets.UTF_8))) {

            String line;

            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.isEmpty()) {
                    continue;
                }

                LocalDateTime timestamp = parseTimestamp(line);

                // Skip lines with missing or invalid timestamps.
                if (timestamp == null) {
                    invalidCount++;
                    continue;
                }

                String serviceName = parseServiceName(line);
                String severity = parseSeverity(line);

                // Skip an exact duplicate message with the same timestamp.
                boolean alreadyExists =
                    logRepository.existsByMessageAndTimestamp(
                        line, timestamp
                    );

                if (alreadyExists) {
                    skippedCount++;
                    continue;
                }

                Log log = new Log();
                log.setMessage(line);
                log.setTimestamp(timestamp);
                log.setServiceName(serviceName);
                log.setSeverity(severity);
                log.setTraceId(null);

                logRepository.save(log);
                savedCount++;
            }

            response.put("success", true);
            response.put("message", "Log file processed successfully.");
            response.put("savedCount", savedCount);
            response.put("skippedCount", skippedCount);
            response.put("invalidCount", invalidCount);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "Unable to read the uploaded file.");

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
        }
    }

    private LocalDateTime parseTimestamp(String line) {
        Matcher matcher = TIMESTAMP_PATTERN.matcher(line);

        if (!matcher.find()) {
            return null;
        }

        String timestampText = matcher.group(1);

        try {
            if (timestampText.contains("T")) {
                return LocalDateTime.parse(
                    timestampText,
                    ISO_TIMESTAMP_FORMATTER
                );
            }

            return LocalDateTime.parse(
                timestampText,
                SPACE_TIMESTAMP_FORMATTER
            );

        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String parseServiceName(String line) {
        // Prefer an explicitly named service/application/app field.
        Matcher matcher = SERVICE_PATTERN.matcher(line);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Otherwise, inspect bracketed values such as [billing-service].
        Matcher bracketMatcher = BRACKET_SERVICE_PATTERN.matcher(line);

        while (bracketMatcher.find()) {
            String candidate = bracketMatcher.group(1);

            // Avoid interpreting timestamps or severity levels as services.
            if (candidate.matches("\\d{4}-\\d{2}-\\d{2}")
                    || candidate.matches(
                        "(?i)ERROR|FATAL|WARN|WARNING|INFO|DEBUG|TRACE"
                    )) {
                continue;
            }

            return candidate;
        }

        return "unknown-service";
    }

    private String parseSeverity(String line) {
        Matcher matcher = SEVERITY_PATTERN.matcher(line);

        if (matcher.find()) {
            String severity = matcher.group(1).toUpperCase();

            if ("WARNING".equals(severity)) {
                return "WARN";
            }

            return severity;
        }

        return "UNKNOWN";
    }
}
