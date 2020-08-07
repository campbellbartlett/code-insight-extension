import React from 'react';
import { CodeInsightReport } from '../PullRequestInsightsContext';
import SingleReportStatus from './single-report-status';

type ReportStatusProps = {
    codeInsightReports: Array<CodeInsightReport>;
};

export const ReportStatus: React.FC<ReportStatusProps> = (props: ReportStatusProps) => {
    const { codeInsightReports } = props;
    return (
        <div id="report-status" style={{ marginTop: '1em', marginBottom: '1em' }}>
            <ul style={{ listStyleType: 'none' }}>
                {codeInsightReports.map(report => {
                    // Use report name as id (will be unque due to BitBucketServer contraint but remove whitespace)
                    const id = report.name.replace(' ', '');
                    return (
                        <li id={id} key={id}>
                            <SingleReportStatus report={report} />
                        </li>
                    );
                })}
            </ul>
        </div>
    );
};
