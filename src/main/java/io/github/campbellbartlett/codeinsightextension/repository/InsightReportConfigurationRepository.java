package io.github.campbellbartlett.codeinsightextension.repository;

import com.atlassian.activeobjects.tx.Transactional;
import io.github.campbellbartlett.codeinsightextension.activeobjects.InsightReportConfiguration;

import java.util.List;

@Transactional
public interface InsightReportConfigurationRepository {

    InsightReportConfiguration add(
            String projectKey,
            String repoSlug,
            String targetBranches,
            String reportKey,
            String displayName,
            boolean required
    );

    /**
     * Soft deletes a record by marking it as revoked.
     * Records that are revoked are not used to determine the 'mergability' of a pull request
     */
    void delete(
            String projectKey,
            String repoSlug,
            String reportKey
    );

    List<InsightReportConfiguration> findAll();

    List<InsightReportConfiguration> findAllForRepository(
            String projectKey,
            String repoSlug
    );
}
