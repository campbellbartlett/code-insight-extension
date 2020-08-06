export type CodeInsightReport = {
    name: string;
    status: string;
};

export type PullRequestInsightsContext = {
    codeInsightReports: [CodeInsightReport];
    commitHash: string;
    repositorySlug: string;
    projectKey: string;
    userAdmin: boolean;
    adminOverride: boolean;
};
