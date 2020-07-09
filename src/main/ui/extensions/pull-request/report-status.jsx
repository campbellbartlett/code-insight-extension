import React from 'react';
import PropTypes from 'prop-types';
import { Status } from '@atlaskit/status/element';

const StatusInParagraph = ({ text, color }) => (
    <div style={{ width: '4em' }}>
        <Status text={text} color={color} />
    </div>
);

const WaitingStatus = () => (
    <div>
        <StatusInParagraph text="Waiting" color="yellow" />
    </div>
);

const FailedStatus = () => (
    <div>
        <StatusInParagraph text="Failed" color="red" />
    </div>
);

const PassedStatus = () => (
    <div>
        <StatusInParagraph text="Passed" color="green" />
    </div>
);

const SingleReportStyle = { display: 'flex', width: '10rem', justifyContent: 'space-between' };

const SingleReportStatus = ({ report }) => {
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
            {report.name} {icon}
        </div>
    );
};

export const ReportStatus = ({ codeInsightReports }) => {
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

ReportStatus.propTypes = {
    codeInsightReports: PropTypes.arrayOf(
        PropTypes.oneOfType([PropTypes.string, PropTypes.string])
    ),
};

ReportStatus.defaultProps = {
    codeInsightReports: [],
};

StatusInParagraph.propTypes = {
    text: PropTypes.string,
    color: PropTypes.string,
};

StatusInParagraph.defaultProps = {
    text: '',
    color: 'neutral',
};

SingleReportStatus.propTypes = {
    report: PropTypes.objectOf([PropTypes.string, PropTypes.string]),
};

SingleReportStatus.defaultProps = {
    report: { name: '', status: '' },
};
