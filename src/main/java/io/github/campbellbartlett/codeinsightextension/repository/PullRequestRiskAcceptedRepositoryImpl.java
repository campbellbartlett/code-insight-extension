package io.github.campbellbartlett.codeinsightextension.repository;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import io.github.campbellbartlett.codeinsightextension.activeobjects.PullRequestRiskAccepted;
import org.apache.commons.lang.StringUtils;
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
    public PullRequestRiskAccepted add(String commitHash, String repoSlug, String projectId, String authenticatingUserSlug, Date createDate) {
        final PullRequestRiskAccepted riskAccepted = activeObjects.create(PullRequestRiskAccepted.class);

        riskAccepted.setCommitHash(commitHash);
        riskAccepted.setProjectId(projectId);
        riskAccepted.setRepositorySlug(repoSlug);
        riskAccepted.setAuthenticatingUserSlug(authenticatingUserSlug);
        riskAccepted.setAcceptedDate(createDate);
        riskAccepted.setRevoked(false);
        riskAccepted.save();

        return riskAccepted;
    }
    @Override
    public void delete(String commitHash, String repoSlug, String projectId) {
        List<PullRequestRiskAccepted> allForPullRequest = findAllForPullRequest(projectId, repoSlug, commitHash).stream()
                .filter(acceptance -> !acceptance.getRevoked())
                .collect(Collectors.toList());

        for (PullRequestRiskAccepted pullRequest: allForPullRequest) {
            pullRequest.setRevoked(true);
            pullRequest.save();
        }
    }

    @Override
    public List<PullRequestRiskAccepted> findAll() {
        return Arrays.asList(activeObjects.find(PullRequestRiskAccepted.class));
    }

    @Override
    public List<PullRequestRiskAccepted> findAllForPullRequest(String projectId, String repoSlug, String commitHash) {
        return findAll().stream()
                .filter(record -> StringUtils.equals(record.getCommitHash(), commitHash))
                .filter(record -> StringUtils.equals(record.getRepositorySlug(), repoSlug))
                .filter(record -> StringUtils.equals(record.getProjectId(), projectId))
                .filter(record -> !record.getRevoked())
                .collect(Collectors.toList());
    }
}
