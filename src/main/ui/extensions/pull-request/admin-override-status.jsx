import React from 'react';
import PropTypes from 'prop-types';

export const AdminOverrideStatus = ({ isOverride }) => (
    <div>
        {isOverride && (
            <h3>
                An administrator has provided authorisation to enable merging of this pull request
                before all code insight status reports have passed.
            </h3>
        )}
        {!isOverride && (
            <h3>
                This Pull Request cannot be merged until all code insight quality reports have
                passed.
            </h3>
        )}
    </div>
);

AdminOverrideStatus.propTypes = {
    isOverride: PropTypes.bool,
};

AdminOverrideStatus.defaultProps = {
    isOverride: false,
};
