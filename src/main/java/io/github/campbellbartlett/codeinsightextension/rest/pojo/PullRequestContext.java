package io.github.campbellbartlett.codeinsightextension.rest.pojo;

import lombok.*;
import org.codehaus.jackson.annotate.JsonAutoDetect;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@Builder
@ToString
@JsonAutoDetect
@NoArgsConstructor
@AllArgsConstructor
public class PullRequestContext {

    private Set<Map<String, Object>> codeInsightReports = new HashSet<>();

    private boolean isUserAdmin = false;
    private boolean isAdminOverride = false;

    private String commitHash;
    private String repositorySlug;
    private String projectKey;

}
