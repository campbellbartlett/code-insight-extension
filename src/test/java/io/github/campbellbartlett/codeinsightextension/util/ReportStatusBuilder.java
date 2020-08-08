package io.github.campbellbartlett.codeinsightextension.util;

import com.atlassian.bitbucket.codeinsights.report.InsightResult;

import java.util.HashMap;
import java.util.Map;

public class ReportStatusBuilder {

    private final Map<String, InsightResult> resultsByReportKey = new HashMap<>();

    private ReportStatusBuilder() {
        // Private constructor for static builder class.
    }

    public static ReportStatusBuilder builder() {
        return new ReportStatusBuilder();
    }

    public ReportStatusBuilder addReport(String key, InsightResult result) {
        resultsByReportKey.put(key, result);
        return this;
    }

    public ReportStatusBuilder addWaitingReport(String key) {
        resultsByReportKey.put(key, null);
        return this;
    }

    public Map<String, InsightResult> build() {
        return resultsByReportKey;
    }
}
