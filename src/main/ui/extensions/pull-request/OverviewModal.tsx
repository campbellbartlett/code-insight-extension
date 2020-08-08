import React, { useCallback, useEffect, useState } from 'react';
import { ModalAction } from '@atlassian/clientside-extensions-components/lib/handlers/ModalHandler';
import { Context } from '@atlassian/clientside-extensions-registry/lib/types';
import { PullRequestInsightsContext } from './PullRequestInsightsContext';
import getCodeInsightExtensionsContext from './insight-context-api/get-insight-context';
import toggleAdminOverride from './insight-context-api/toggle-admin-override';
import { AdminOverrideStatus } from './admin-override-status';
import { ReportStatus } from './report-status/report-status';
import { OverviewAdmonition } from './overview-admonition';

const createMainButton = (
    isOverridden: boolean,
    loaded: boolean,
    userAdmin: boolean,
    onOverride: () => void
) => ({
    text: isOverridden ? 'Disable merge override' : 'Enable merging',
    onClick: () => {
        onOverride();
    },
    isDisabled: loaded && !userAdmin,
});

type ModalComponentProps = {
    context: Context<{}>;
    setActions: (actions: ModalAction[]) => void;
    createCloseButton: (refreshOnClose: boolean) => ModalAction;
    setIsOverride: (isOverride: boolean) => void;
};

const OverviewModal: React.FC<ModalComponentProps> = (props: ModalComponentProps) => {
    const { context, setActions, createCloseButton, setIsOverride } = props;
    const [insightsContext, setInsightsContext] = useState<PullRequestInsightsContext | null>(null);

    useEffect(() => {
        getCodeInsightExtensionsContext(context).then(
            (codeInsightContext: PullRequestInsightsContext) => {
                setInsightsContext(codeInsightContext);
                setActionButtons(true, codeInsightContext, false);
            }
        );
    }, [context]);

    const setActionButtons = useCallback(
        (isLoaded: boolean, insightContext: PullRequestInsightsContext, isUpdated: boolean) => {
            const isOverridden = insightContext && insightContext.adminOverride;
            const mainButton = createMainButton(
                isOverridden,
                isLoaded,
                isLoaded && insightContext && insightContext.userAdmin,
                async () => {
                    await toggleAdminOverride(context, insightContext);
                    const codeInsightContext = await getCodeInsightExtensionsContext(context);
                    setInsightsContext(codeInsightContext);
                    setActionButtons(true, codeInsightContext, true);
                }
            );
            const closeButton = createCloseButton(isUpdated);
            setIsOverride(isLoaded && insightContext && insightContext.adminOverride);
            setActions([mainButton, closeButton]);
        },
        [setIsOverride, setActions]
    );

    return (
        <div id="modal-div">
            {insightsContext && <AdminOverrideStatus isOverride={insightsContext.adminOverride} />}
            {insightsContext && (
                <ReportStatus codeInsightReports={insightsContext.codeInsightReports} />
            )}
            {insightsContext && <OverviewAdmonition userAdmin={insightsContext.userAdmin} />}
        </div>
    );
};

export default OverviewModal;
