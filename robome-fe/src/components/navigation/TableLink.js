import React, { Component } from 'react';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

class TableLink extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    render() {

        const tableId = this.props.tableId;
        const text = this.props.text;
        const showTableUrl = "/tables/show/" + tableId;

        return tableId ? (<Link to={showTableUrl}>{text}</Link>) : "";
    }
}

export default TableLink;
