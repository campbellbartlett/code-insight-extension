import React from 'react';

type AdminOverrideStatusProps = {
    isOverride: boolean;
};

export const AdminOverrideStatus: React.FC<AdminOverrideStatusProps> = (
    props: AdminOverrideStatusProps
) => {
    const { isOverride } = props;
    return (
        <div id="admin-override-status">
            {isOverride ? (
                <h3 id="admin-message">
                    An administrator has provided authorisation to enable merging of this pull
                    request request before all code insight status reports have passed.
                </h3>
            ) : (
                <h3 id="standard-message">
                    This Pull Request cannot be merged until all code insight quality reports have
                    passed.
                </h3>
            )}
        </div>
    );
};
