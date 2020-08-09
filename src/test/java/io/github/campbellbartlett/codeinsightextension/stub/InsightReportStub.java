package io.github.campbellbartlett.codeinsightextension.stub;

import com.atlassian.bitbucket.codeinsights.report.InsightReport;
import com.atlassian.bitbucket.codeinsights.report.InsightReportData;
import com.atlassian.bitbucket.codeinsights.report.InsightResult;
import com.atlassian.bitbucket.repository.Repository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@NoArgsConstructor
@AllArgsConstructor
public class InsightReportStub implements InsightReport {
    private String key;
    private InsightResult result;

    @Nonnull
    @Override
    public String getCommitId() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Optional<String> getCoverageProviderKey() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Date getCreatedDate() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public List<InsightReportData> getData() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Optional<String> getDetails() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public String getKey() {
        return key;
    }

    @Nonnull
    @Override
    public Optional<URI> getLink() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Optional<URI> getLogoUrl() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Repository getRepository() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Optional<InsightResult> getResult() {
        return Optional.ofNullable(result);
    }

    @Nonnull
    @Override
    public String getTitle() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }

    @Nonnull
    @Override
    public Optional<String> getReporter() {
        throw new UnsupportedOperationException("Stub does not implement this method");
    }
}
