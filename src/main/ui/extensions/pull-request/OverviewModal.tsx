import React, { useEffect, useState } from 'react';
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
    const [loaded, setLoaded] = useState(false);
    const [updated, setUpdated] = useState(false);
    const [insightsContext, setInsightsContext] = useState<PullRequestInsightsContext | null>(null);

    useEffect(() => {
        setActionButtons();
        getCodeInsightExtensionsContext(context).then(
            (codeInsightContext: PullRequestInsightsContext) => {
                setInsightsContext(codeInsightContext);
                setLoaded(true);
                setActionButtons();
            }
        );
    }, [context]);

    const getOnMainClick = () => {
        return async () => {
            await toggleAdminOverride(context, loaded, insightsContext);
            const codeInsightContext = await getCodeInsightExtensionsContext(context);
            setInsightsContext(codeInsightContext);
            setLoaded(true);
            setUpdated(true);
            setActionButtons();
        };
    };

    const setActionButtons = () => {
        const onMainClick = getOnMainClick();
        const isOverridden = loaded && insightsContext && insightsContext.adminOverride;
        const mainButton = createMainButton(
            isOverridden,
            loaded,
            loaded && insightsContext && insightsContext.userAdmin,
            onMainClick
        );
        const closeButton = createCloseButton(updated);
        setIsOverride(loaded && insightsContext && insightsContext.adminOverride);
        setActions([mainButton, closeButton]);
    };

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
