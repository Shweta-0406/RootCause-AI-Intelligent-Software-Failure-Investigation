
package com.rootcauseai.rootcause_ai;

import java.util.List;
import java.util.Map;

public class AnalysisSummary {

    private int totalLogs;
    private int errorCount;
    private List<Map<String, Object>> results;

    public AnalysisSummary(int totalLogs, int errorCount,
                           List<Map<String, Object>> results) {
        this.totalLogs = totalLogs;
        this.errorCount = errorCount;
        this.results = results;
    }

    public int getTotalLogs() {
        return totalLogs;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public List<Map<String, Object>> getResults() {
        return results;
    }
}
