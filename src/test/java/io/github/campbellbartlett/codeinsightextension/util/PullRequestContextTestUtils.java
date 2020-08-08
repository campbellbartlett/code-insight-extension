package io.github.campbellbartlett.codeinsightextension.util;

import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import io.github.campbellbartlett.codeinsightextension.InsightReportStatus;
import io.github.campbellbartlett.codeinsightextension.rest.pojo.PullRequestContext;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PullRequestContextTestUtils {

    private PullRequestContextTestUtils() {
        // Private constructor for static utility class
    }

    public static PullRequestContext getPullRequestContextWithReports(Map<String, InsightResult> reportStatusByKey) {
        Set<Map<String, Object>> insightReportContext = getInsightReportContext(reportStatusByKey);

        return PullRequestContext.builder()
                .commitHash("baz")
                .isAdminOverride(false)
                .isUserAdmin(false)
                .projectKey("foo")
                .repositorySlug("bar")
                .codeInsightReports(insightReportContext)
                .build();
    }

    private static Set<Map<String, Object>> getInsightReportContext(Map<String, InsightResult> reportStatusByKey) {
        Set<Map<String, Object>> insightReportContext = new HashSet<>();
        for (String reportKey: reportStatusByKey.keySet()) {
            Map<String, Object> insightReportStatus = new HashMap<>();

            InsightReportStatus status = getStatusForResult(reportStatusByKey.get(reportKey));

            insightReportStatus.put("name", reportKey);
            insightReportStatus.put("status", status);

            insightReportContext.add(insightReportStatus);
        }
        return insightReportContext;
    }

    private static InsightReportStatus getStatusForResult(InsightResult insightResult){
        if (insightResult == null) {
            return InsightReportStatus.WAITING;
        }

        return insightResult.equals(InsightResult.FAIL) ? InsightReportStatus.FAIL : InsightReportStatus.PASS;
    }
}
