package io.github.campbellbartlett.codeinsightextension.service;

import io.github.campbellbartlett.codeinsightextension.activeobjects.InsightReportConfiguration;
import io.github.campbellbartlett.codeinsightextension.repository.InsightReportConfigurationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class InsightRepositorySettingsContextService {

    private final InsightReportConfigurationRepository configurationRepository;

    @Autowired
    public InsightRepositorySettingsContextService(InsightReportConfigurationRepository configurationRepository) {
        this.configurationRepository = configurationRepository;
    }

    public Set<InsightReportConfiguration> getContextForRepo(String projectKey, String repoSlug) {
        return new HashSet<>(configurationRepository.findAllForRepository(projectKey, repoSlug));
    }
}
