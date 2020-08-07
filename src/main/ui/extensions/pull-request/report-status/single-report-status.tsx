import React from 'react';
import { CodeInsightReport } from '../PullRequestInsightsContext';
import WaitingStatus from './waiting-status';
import FailedStatus from './failed-status';
import PassedStatus from './passed-status';

const SingleReportStyle = { display: 'flex', width: '10rem', justifyContent: 'space-between' };

type SingleReportProps = {
    report: CodeInsightReport;
};

const SingleReportStatus: React.FC<SingleReportProps> = (props: SingleReportProps) => {
    const { report } = props;
    let icon;
    switch (report.status) {
        case 'WAITING':
            icon = <WaitingStatus />;
            break;
        case 'FAIL':
            icon = <FailedStatus />;
            break;
        case 'PASS':
            icon = <PassedStatus />;
            break;
        default:
            return <div />;
    }
    return (
        <div style={SingleReportStyle}>
            <span id="report-name">{report.name}</span> {icon}
        </div>
    );
};

export default SingleReportStatus;
