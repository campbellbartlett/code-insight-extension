package io.github.campbellbartlett.codeinsightextension.activeobjects;

import net.java.ao.Entity;
import net.java.ao.schema.Table;

import java.util.Date;

@Table("CI_EXT_CONFIG")
public interface InsightReportConfiguration extends Entity {

    String getRepositorySlug();
    void setRepositorySlug(String repoSlug);

    String getProjectId();
    void setProjectId(String projectId);

    String getTargetBranches();
    void setTargetBranches(String targetBranches);

    Date getDateCreated();
    void setDateCreated(Date dateCreated);

    String getReportKey();
    void setReportKey(String reportKey);

    String getDisplayName();
    void setDisplayName(String displayName);

    boolean getRequired();
    void setRequired(boolean required);

    boolean getDeleted();
    void setDeleted(boolean deleted);
}
