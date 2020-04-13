package io.github.campbellbartlett.codeinsightextension.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PullRequestRiskAcceptedRepositoryImpl implements PullRequestRiskAcceptedRepository {

    private final ActiveObjects activeObjects;

    @Autowired
    public PullRequestRiskAcceptedRepositoryImpl(@ComponentImport ActiveObjects activeObjects) {
        this.activeObjects = activeObjects;
    }

    @Override
    public PullRequestRiskAccepted add(long pullRequestId, String repoSlug, String authenticatingUserSlug, Date createDate) {
        final PullRequestRiskAccepted riskAccepted = activeObjects.create(PullRequestRiskAccepted.class);

        riskAccepted.setPullRequestId(pullRequestId);
        riskAccepted.setRepositorySlug(repoSlug);
        riskAccepted.setAuthenticatingUserSlug(authenticatingUserSlug);
        riskAccepted.setAcceptedDate(createDate);
        riskAccepted.save();

        return riskAccepted;
    }

    @Override
    public List<PullRequestRiskAccepted> findAll() {
        return Arrays.asList(activeObjects.find(PullRequestRiskAccepted.class));
    }

    @Override
    public List<PullRequestRiskAccepted> findAllForPullRequest(long pullRequestId) {
        return findAll().stream()
                .filter(record -> record.getPullRequestId() == pullRequestId)
                .collect(Collectors.toList());
    }
}
