import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Title from '../tiles/Title';

import securedGet, { securedDelete } from '../../web/ajax';


class ListTables extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {tables: []};
    }

    componentDidMount() {

        const self = this;
        const authToken = this.props.authToken;

        securedGet("/tables", authToken)
            .then(response => response.json())
            .then(data => self.setState({tables: data}));
    }



    delete(id) {
        const self = this;

        return function(event) {
            
            const authToken = self.props.authToken;

            securedDelete("/tables/" + id, authToken)
                .then(function(response){
                    var filtered = self.state.tables.filter(function(table, index, arr){
                        return table.key.tableId != id;
                    });
                    self.setState({tables: filtered})
                })
                .catch(error => self.setState({error: error}));

            event.preventDefault();
        }
    }

    renderTableRow(no, id, title, description) {
        var tableUrl = "/tables/show/" + id;
        var editTableUrl = "/tables/edit/" + id;
        return (
            <tr key={id}>
                <th scope="row">{no++}</th>
                <td><Link to={tableUrl}>{title}</Link></td>
                <td>{description}</td>
                <td>
                    <Link to={editTableUrl} >edit</Link>&nbsp;&nbsp;&nbsp;
                    <Link to="" onClick={this.delete(id)}>delete</Link>
                </td>
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
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ListTables = connect(mapStateToProps, mapDispatchToProps)(ListTables);


export default ListTables;
