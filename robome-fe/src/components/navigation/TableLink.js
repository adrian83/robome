import React, { Component } from 'react';
import { Link } from 'react-router-dom';

class TableLink extends Component {

    render() {

        const tableId = this.props.tableId;
        const text = this.props.text;
        const showTableUrl = "/tables/show/" + tableId;

        return tableId ? (<Link to={showTableUrl}>{text}</Link>) : "";
    }
}

export default TableLink;
