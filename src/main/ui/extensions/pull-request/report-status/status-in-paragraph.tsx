import React from 'react';
import { Status } from '@atlaskit/status/element';

type StatusInParagraphProps = {
    text: string;
    color: string;
};

const StatusInParagraph: React.FC<StatusInParagraphProps> = (props: StatusInParagraphProps) => {
    const { text, color } = props;
    return (
        <div style={{ width: '4em' }}>
            <Status text={text} color={color} />
        </div>
    );
};

export default StatusInParagraph;
