import React, { Component } from 'react';
import { Link } from 'react-router-dom';

import { showTableUrl } from '../../web/url';


class TableLink extends Component {

    render() {
        const text = this.props.text;
        const showTabUrl = showTableUrl(this.props.tableId);

        return this.props.tableId ? (<Link to={showTabUrl}>{text}</Link>) : "";
    }
}

export default TableLink;
