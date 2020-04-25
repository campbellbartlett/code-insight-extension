import { ModalExtension, renderElementAsReact } from '@atlassian/clientside-extensions';
import React from 'react';

/**
 * @clientside-extension
 *
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */

function getFetchOptions(methodType) {
    return {
        method: methodType,
        headers: {
            'Content-Type': 'application/json',
        },
    };
}

function getPullRequestInsightExtensionsContextUri(context) {
    if (!context) {
        console.error('Context is required');
        return '';
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/context`;
}

function putPullRequestAdminOverrideUri(context, state) {
    if (!context) {
        console.error('Context is required');
        return '';
    }

    const projectKey = context.project.key;
    const repoSlug = context.repository.slug;
    const commitHash = context.pullRequest.fromRef.latestCommit;
    const revoke = state && state.loaded && state.insightsContext.adminOverride ? 'true' : 'false';

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/override?revoke=${revoke}`;
}

export default ModalExtension.factory((api, context) => {
    return {
        label: `Code Insight Extension`,
        onAction(modalApi) {
            modalApi.setTitle('Code Insights Extension').setWidth(ModalExtension.Width.large);

            class ModalComponent extends React.Component {
                constructor(props) {
                    super(props);
                    this.state = { loaded: false, admin: false };
                }

                componentDidMount() {
                    this.setActionButtons(false);

                    this.getCodeInsightExtensionsContext();
                }

                setActionButtons() {
                    const isOverridden =
                        this.state.loaded && this.state.insightsContext.adminOverride;
                    const mainButton = {
                        text: isOverridden ? 'Disable merge override' : 'Enable merge override',
                        onClick: () => {
                            this.sendAdminOverride();
                        },
                        isDisabled: this.state.loaded && !this.state.insightsContext.userAdmin,
                    };
                    const closeButton = {
                        text: 'Close',
                        onClick: () => {
                            modalApi.closeModal();
                        },
                    };
                    modalApi.setAppearance(
                        this.state.loaded && this.state.insightsContext.adminOverride
                            ? ModalExtension.Appearance.warning
                            : ModalExtension.Appearance.danger
                    );
                    modalApi.setActions([mainButton, closeButton]);
                }

                getCodeInsightExtensionsContext() {
                    fetch(
                        getPullRequestInsightExtensionsContextUri(context),
                        getFetchOptions('GET')
                    )
                        .then(res => res.json())
                        .then(insightsContext => {
                            console.log(insightsContext);
                            this.setState({ insightsContext });
                            this.setState({ loaded: true });
                            this.setActionButtons();
                        });
                }

                sendAdminOverride() {
                    fetch(
                        putPullRequestAdminOverrideUri(context, this.state),
                        getFetchOptions('PUT')
                    )
                        .then(res =>
                            console.log(
                                `Put call to adminOverride complete. Response was [${res.status}]`
                            )
                        )
                        .then(() => this.getCodeInsightExtensionsContext());
                }

                render() {
                    const { loaded } = this.state;
                    const { insightsContext } = this.state;
                    return (
                        <div data-testid="modal-with-action-callback">
                            {loaded &&
                                insightsContext.userAdmin &&
                                insightsContext.adminOverride && (
                                    <h3>
                                        An administrator has provided authorisation to enable
                                        merging of this pull request before all code insight status
                                        reports have passed.
                                    </h3>
                                )}
                            {loaded &&
                                insightsContext.userAdmin &&
                                !insightsContext.adminOverride && (
                                    <h3>
                                        This Pull Request cannot be merged until all code insight
                                        quality reports have passed.
                                    </h3>
                                )}
                            <br />
                            {loaded &&
                                insightsContext.codeInsightReports.some(
                                    report => report.status === 'WAITING'
                                ) &&
                                `Some code insight quality reports for this pull request are still waiting to report.`}
                            <br />
                            {loaded &&
                                insightsContext.codeInsightReports.some(
                                    report => report.status === 'FAIL'
                                ) &&
                                `Some code insight quality reports for this pull request have failed.`}
                            <br />
                            {loaded &&
                                insightsContext.userAdmin &&
                                insightsContext.codeInsightReports.every(
                                    report => report.status === 'PASS'
                                ) &&
                                `All code insight quality reports for this pull request have passed.`}
                            {loaded &&
                                insightsContext.userAdmin &&
                                !insightsContext.adminOverride &&
                                !insightsContext.codeInsightReports.every(
                                    report => report.status === 'PASS'
                                ) && (
                                    <p>
                                        As administrator of this pull request you can enable merging
                                        before all code insight quality reports have passed.
                                    </p>
                                )}
                        </div>
                    );
                }
            }

            renderElementAsReact(modalApi, ModalComponent);
        },
    };
});
