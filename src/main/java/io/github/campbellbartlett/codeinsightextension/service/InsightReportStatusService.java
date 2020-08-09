package io.github.campbellbartlett.codeinsightextension.service;

import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightReportService;
import com.atlassian.bitbucket.codeinsights.report.SearchInsightReportRequest;
import com.atlassian.bitbucket.pull.PullRequest;
import com.atlassian.bitbucket.util.Page;
import com.atlassian.bitbucket.util.PageRequestImpl;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class InsightReportStatusService {

    private static final int PAGE_SIZE = 20;

    private final InsightReportService insightReportService;

    @Autowired
    public InsightReportStatusService(@ComponentImport InsightReportService insightReportService) {
        this.insightReportService = insightReportService;
    }

    public Set<InsightReport> getAllReportsForPullRequest(PullRequest pullRequest) {
        int pageFrom = 0;
        Page<InsightReport> insightReportPage = getPageOfInsightReports(pullRequest, pageFrom);
        Set<InsightReport> insightReports = insightReportPage.stream().collect(Collectors.toSet());

        while (isNotLastPage(insightReportPage)) {
            pageFrom = pageFrom + PAGE_SIZE;
            insightReportPage = getPageOfInsightReports(pullRequest, pageFrom);
            insightReports.addAll(insightReportPage.stream().collect(Collectors.toSet()));
        }

        return insightReports;
    }

    private boolean isNotLastPage(Page<InsightReport> insightReportPage) {
        return !insightReportPage.getIsLastPage();
    }

    private Page<InsightReport> getPageOfInsightReports(PullRequest pullRequest, int pageFrom) {
        SearchInsightReportRequest searchInsightReportRequest = new SearchInsightReportRequest
                .Builder(pullRequest).build();
        return insightReportService.search(searchInsightReportRequest, new PageRequestImpl(pageFrom, PAGE_SIZE));
    }

}
