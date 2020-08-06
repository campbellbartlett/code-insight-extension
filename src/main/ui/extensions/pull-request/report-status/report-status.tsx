import React from 'react';
import { CodeInsightReport } from '../PullRequestInsightsContext';
import SingleReportStatus from './single-report-status';

type ReportStatusProps = {
    codeInsightReports: [CodeInsightReport];
};

export const ReportStatus: React.FC<ReportStatusProps> = (props: ReportStatusProps) => {
    const { codeInsightReports } = props;
    return (
        <div style={{ marginTop: '1em', marginBottom: '1em' }}>
            <ul style={{ listStyleType: 'none' }}>
                {codeInsightReports.map(report => (
                    <li key={report.name}>
                        <SingleReportStatus report={report} />
                    </li>
                ))}
            </ul>
        </div>
    );
};
