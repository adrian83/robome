import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

import Title from '../tiles/Title';

import securedGet from '../../web/ajax';


class ListTables extends Component {

    constructor(props) { 
        super(props);

        this.state = {tables: []};
    }

    componentDidMount() {

        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;

        securedGet(backendHost + "/tables")
            .then(response => response.json())
            .then(data => self.setState({tables: data}));
    }

    renderTableRow(no, id, title, description) {
        var tableUrl = "/tables/show/" + id;
        return (
            <tr key={id}>
                <th scope="row">{no++}</th>
                <td><Link to={tableUrl}>{title}</Link></td>
                <td>{description}</td>
                <td>delete &nbsp;&nbsp;&nbsp; edit</td>
            </tr>);
    }

    render() {

        var self = this;

        var no = 1
        var rows = this.state.tables
            .map(table => self.renderTableRow(no++, table.key.tableId, table.title, table.description));

        return (
            <div>
                <Title title="List tables" description="list of all created tables"></Title>

                <div>
                    <Link to="/tables/create/">Create table</Link>
                </div>
                <br/>
                <div>
                    <table className="table table-striped">
                        <thead>
                            <tr>
                                <th scope="col">#</th>
                                <th scope="col">Title</th>
                                <th scope="col">Description</th>
                                <th scope="col">Operations</th>
                            </tr>
                        </thead>
                        <tbody>
                            {rows}
                        </tbody>
                    </table>
                </div>

            </div>
        );
    }
}

const mapStateToProps = (state) => {
    return {};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ListTables = connect(mapStateToProps, mapDispatchToProps)(ListTables);


export default ListTables;
