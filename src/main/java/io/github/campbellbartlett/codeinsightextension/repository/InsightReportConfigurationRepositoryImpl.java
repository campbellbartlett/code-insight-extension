package io.github.campbellbartlett.codeinsightextension.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.activeobjects.InsightReportConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class InsightReportConfigurationRepositoryImpl implements InsightReportConfigurationRepository {

    private final ActiveObjects activeObjects;

    @Autowired
    public InsightReportConfigurationRepositoryImpl(@ComponentImport ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }


    @Override
    public InsightReportConfiguration add(String projectKey, String repoSlug, String targetBranches, String reportKey, String displayName, boolean required) {
        final InsightReportConfiguration configuration = activeObjects.create(InsightReportConfiguration.class);

        configuration.setProjectId(projectKey);
        configuration.setRepositorySlug(repoSlug);
        configuration.setTargetBranches(targetBranches);
        configuration.setReportKey(reportKey);
        configuration.setDisplayName(displayName);
        configuration.setRequired(required);
        configuration.setDateCreated(new Date());
        configuration.setDeleted(false);

        configuration.save();

        return configuration;
    }

    @Override
    public void delete(String projectKey, String repoSlug, String reportKey) {
        List<InsightReportConfiguration> allForRepoAndReport = findAllForRepository(projectKey, repoSlug)
                .stream()
                .filter(config -> StringUtils.equals(config.getReportKey(), reportKey))
                .filter(config -> !config.getDeleted())
                .collect(Collectors.toList());

        for (InsightReportConfiguration configuration : allForRepoAndReport) {
            configuration.setDeleted(true);
            configuration.save();
        }
    }

    @Override
    public List<InsightReportConfiguration> findAll() {
        return Arrays.asList(activeObjects.find(InsightReportConfiguration.class));
    }

    @Override
    public List<InsightReportConfiguration> findAllForRepository(String projectKey, String repoSlug) {
        return findAll().stream()
                .filter(config -> StringUtils.equals(config.getProjectId(), projectKey))
                .filter(config -> StringUtils.equals(config.getRepositorySlug(), repoSlug))
                .filter(config -> !config.getDeleted())
                .collect(Collectors.toList());
    }
}
