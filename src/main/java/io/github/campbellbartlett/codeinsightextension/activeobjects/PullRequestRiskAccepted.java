package io.github.campbellbartlett.codeinsightextension.activeobjects;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

import java.util.Date;

@Table("PR_ACCEPTED")
public interface PullRequestRiskAccepted extends Entity {

    long getPullRequestId();
    void setPullRequestId(long pullRequestId);

    String getRepositorySlug();
    void setRepositorySlug(String repoSlug);

    String getAuthenticatingUserSlug();
    void setAuthenticatingUserSlug(String adminSlug);

    Date getAcceptedDate();
    void setAcceptedDate(Date date);
}
