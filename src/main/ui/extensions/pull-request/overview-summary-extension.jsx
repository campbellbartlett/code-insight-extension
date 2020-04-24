import {ModalExtension, renderElementAsReact} from '@atlassian/clientside-extensions';
import React, {useState} from 'react';

/**
 * @clientside-extension
 *
 * @extension-point bitbucket.ui.pullrequest.overview.summary
 */

function getFetchOptions(methodType) {
    return {
        method: methodType,
        headers: {
            'Content-Type': 'application/json'
        }
    }
}

function getPullRequestInsightExtensionsContextUri(context) {
    if (!context) {
        console.error('Context is required');
        return;
    }

    let projectKey = context.project.key;
    let repoSlug = context.repository.slug;
    let commitHash = context.pullRequest.fromRef.latestCommit;

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/context`
}

function putPullRequestAdminOverrideUri(context) {
    if (!context) {
        console.error('Context is required');
        return;
    }

    let projectKey = context.project.key;
    let repoSlug = context.repository.slug;
    let commitHash = context.pullRequest.fromRef.latestCommit;

    return `/rest/code-insight-extension/1.0/pullRequest/${projectKey}/${repoSlug}/${commitHash}/override`
}

export default ModalExtension.factory((api, context) => {
    return {
        label: `Code Insight Extension`,
        onAction(modalApi) {
            modalApi
                .setTitle('Code Insights Extension')
                .setWidth(ModalExtension.Width.large)

            class ModalComponent extends React.Component {
                constructor(props) {
                    super(props);
                    this.state = {loaded: false, admin: false};
                }

                componentDidMount() {
                    this.setActionButtons(false);

                    this.getCodeInsightExtensionsContext();
                }

                setActionButtons() {
                    let mainButton = {
                        text: this.state.loaded && this.state.insightsContext.adminOverride ? 'Disable merge override' : 'Enable merge override',
                        onClick: () => {
                            this.sendAdminOverride();
                        },
                        isDisabled: this.state.loaded && !this.state.insightsContext.userAdmin,
                    };
                    let closeButton = {
                        text: 'Close',
                        onClick: () => {
                            modalApi.closeModal();
                        },
                    };
                    modalApi.setAppearance(this.state.loaded && this.state.insightsContext.adminOverride ? ModalExtension.Appearance.warning : ModalExtension.Appearance.danger)
                    modalApi.setActions([mainButton, closeButton]);
                }

                getCodeInsightExtensionsContext() {
                    fetch(getPullRequestInsightExtensionsContextUri(context), getFetchOptions('GET'))
                        .then(res => res.json())
                        .then(insightsContext => {
                            console.log(insightsContext);
                            this.setState({insightsContext: insightsContext});
                            this.setState({loaded: true});
                            this.setActionButtons();
                        });
                }

                sendAdminOverride() {
                    fetch(putPullRequestAdminOverrideUri(context), getFetchOptions('PUT'))
                        .then(res => console.log(`Put call to adminOverride complete. Response was [${res.status}]`))
                        .then(() => this.getCodeInsightExtensionsContext());
                }

                render() {
                    return (
                        <div data-testid="modal-with-action-callback">
                            { this.state.loaded
                                && this.state.insightsContext.userAdmin
                                && this.state.insightsContext.adminOverride
                                && <h3>An administrator has provided authorisation to enable merging of this pull
                                    request before all code insight status reports have passed.</h3>
                            }
                            { this.state.loaded
                                && this.state.insightsContext.userAdmin
                                && !this.state.insightsContext.adminOverride
                                && <h3>This Pull Request cannot be merged until all code insight quality reports have
                                    passed.</h3>
                            }
                            <br/>
                            { this.state.loaded
                                && this.state.insightsContext.codeInsightReports.some(report => report.status === 'WAITING')
                                && `Some code insight quality reports for this pull request are still waiting to report.`
                            }
                            <br/>
                            { this.state.loaded
                                && this.state.insightsContext.codeInsightReports.some(report => report.status === 'FAIL')
                                && `Some code insight quality reports for this pull request have failed.`
                            }
                            <br/>
                            { this.state.loaded
                                && this.state.insightsContext.userAdmin
                                && this.state.insightsContext.codeInsightReports.every(report => report.status === 'PASS')
                                && `All code insight quality reports for this pull request have passed.`
                            }
                            { this.state.loaded
                                && this.state.insightsContext.userAdmin
                                && !this.state.insightsContext.adminOverride
                                && !this.state.insightsContext.codeInsightReports.every(report => report.status === 'PASS')
                                && <p style="margin-top: 1rem;">As administrator of this pull request you can enable merging before all code insight quality reports have passed.</p>
                            }

                            {/*{this.state.loaded && this.state.insightsContext.codeInsightReports.map(report => (*/}
                            {/*        <React.Fragment key={report.name}>*/}
                            {/*            <ul>{report.name}: {report.status}</ul>*/}
                            {/*        </React.Fragment>*/}
                            {/*    )*/}
                            {/*)}*/}
                        </div>
                    );
                }
            }

            renderElementAsReact(modalApi, ModalComponent);
        },
    };
});