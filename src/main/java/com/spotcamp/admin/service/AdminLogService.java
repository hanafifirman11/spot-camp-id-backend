package com.spotcamp.admin.service;

import com.spotcamp.admin.api.dto.LogEntry;
import com.spotcamp.admin.api.dto.LogListResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class AdminLogService {

    @Value("${logging.file.name:logs/spot-camp-backend.log}")
    private String logFilePath;

    // Pattern to match: 2024-01-20 14:30:00 [main] INFO  c.s.SpotCampApplication - Message
    // Matches: Timestamp | [Thread] | Level | Logger | - | Message
    private static final Pattern LOG_PATTERN = Pattern.compile("^(\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}) \\[(.*)\\] (\\w+)\\s+(\\S+) - (.*)$");

    public LogListResponse getLogs(int page, int size, String level, String search) {
        File logFile = new File(logFilePath);
        
        // Try absolute path if relative fails (for dev environment quirks)
        if (!logFile.exists()) {
            logFile = new File(System.getProperty("user.dir"), logFilePath);
        }
        
        if (!logFile.exists()) {
            log.warn("Log file not found at: {}", logFile.getAbsolutePath());
            return LogListResponse.builder().content(List.of()).build();
        }

        List<LogEntry> logs = new ArrayList<>();
        int skip = page * size;
        int count = 0; // Number of matching logs found (processed)
        int collected = 0; // Number of logs added to result
        int scanned = 0; // Number of lines read (safety)
        int maxScan = 20000; // Limit scan to prevent timeout on large files

        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(logFile, StandardCharsets.UTF_8)) {
            String line;
            List<String> stackBuffer = new ArrayList<>();

            while ((line = reader.readLine()) != null && scanned < maxScan) {
                scanned++;
                Matcher matcher = LOG_PATTERN.matcher(line);

                if (matcher.find()) {
                    // This is a log header line
                    LogEntry entry = LogEntry.builder()
                            .timestamp(matcher.group(1))
                            .thread(matcher.group(2))
                            .level(matcher.group(3))
                            .logger(matcher.group(4))
                            .message(matcher.group(5))
                            .build();

                    // If we have accumulated stack trace lines, attach them
                    if (!stackBuffer.isEmpty()) {
                        StringBuilder fullMessage = new StringBuilder(entry.getMessage());
                        // Append stack trace (iterate reverse buffer in reverse order to restore top-down)
                        for (int i = stackBuffer.size() - 1; i >= 0; i--) {
                            fullMessage.append("\n").append(stackBuffer.get(i));
                        }
                        entry.setMessage(fullMessage.toString());
                        stackBuffer.clear();
                    }
                    entry.setRaw(line); // Keep at least the header as raw

                    // --- Filtering ---
                    boolean matches = true;

                    // Level Filter
                    if (level != null && !level.isEmpty()) {
                        if (!entry.getLevel().equalsIgnoreCase(level)) {
                            matches = false;
                        }
                    }

                    // Search Filter (checks message, logger, thread)
                    if (matches && search != null && !search.isEmpty()) {
                        String searchLower = search.toLowerCase();
                        boolean contentMatch = (entry.getMessage() != null && entry.getMessage().toLowerCase().contains(searchLower)) ||
                                (entry.getLogger() != null && entry.getLogger().toLowerCase().contains(searchLower));
                        if (!contentMatch) {
                            matches = false;
                        }
                    }

                    if (matches) {
                        // Pagination: Skip first 'skip' items
                        if (count < skip) {
                            count++;
                        } else {
                            // Collect item
                            logs.add(entry);
                            collected++;
                            if (collected >= size) {
                                break;
                            }
                        }
                    }

                } else {
                    // Likely a stack trace or multi-line message part
                    // Since we read in reverse, these appear BEFORE the header.
                    // We just buffer them until we hit a header.
                    stackBuffer.add(line);
                }
            }
        } catch (Exception e) {
            log.error("Error reading log file", e);
        }

        return LogListResponse.builder()
                .content(logs)
                .page(page)
                .size(size)
                .totalElements(scanned >= maxScan ? -1 : count + collected) // Rough estimate if hit limit
                .build();
    }
}
