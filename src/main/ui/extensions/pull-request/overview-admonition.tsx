import React from 'react';
import Admonition from 'react-admonitions';

type OverviewAdmonitionProps = {
    userAdmin: boolean;
};

export const OverviewAdmonition: React.FC<OverviewAdmonitionProps> = (
    props: OverviewAdmonitionProps
) => {
    const { userAdmin } = props;
    return (
        <div id="overview-admonition">
            {userAdmin && (
                <Admonition type="tip">
                    As an administrator of this repository you can enable merging of this pull
                    request before all code insight quality reports have passed.
                </Admonition>
            )}
        </div>
    );
};
