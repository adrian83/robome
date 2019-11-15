import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Title from '../tiles/Title';

import securedGet, { securedDelete } from '../../web/ajax';
import { tablesBeUrl, tableBeUrl, editTableUrl, showTableUrl, createTableUrl } from '../../web/url';

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
        const authToken = self.props.authToken;
        securedGet(tablesBeUrl(), authToken)
            .then(response => response.json())
            .then(data => self.setState({tables: data}));
    }

    delete(tableId) {
        const self = this;

        return function(event) {

            securedDelete(tableBeUrl(tableId), self.props.authToken)
                .then(function(response){
                    var filtered = self.state.tables.filter((table, index, arr) => table.key.tableId != tableId);
                    self.setState({tables: filtered})
                })
                .catch(error => self.setState({error: error}));

            event.preventDefault();
        }
    }

    renderTableRow(no, table) {
        const tableId = table.key.tableId;
        const title = table.title;
        const description = table.description;

        const showTabUrl = showTableUrl(tableId);
        const editTabUrl = editTableUrl(tableId);

        return (
            <tr key={tableId}>
                <th scope="row">{no++}</th>
                <td><Link to={showTabUrl}>{title}</Link></td>
                <td>{description}</td>
                <td>
                    <Link to={editTabUrl} >edit</Link>&nbsp;&nbsp;&nbsp;
                    <Link to="" onClick={this.delete(tableId)}>delete</Link>
                </td>
            </tr>);
    }

    render() {
        const self = this;
        const createTabUrl = createTableUrl();

        var no = 1
        var rows = this.state.tables.map(table => self.renderTableRow(no++, table));

        return (
            <div>
                <Title title="List tables" description="list of all created tables"></Title>

                <div>
                    <Link to={createTabUrl}>Create table</Link>
                </div>
                <br/><br/>
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

const mapStateToProps = (state) =>{return {authToken: state.authToken};}
const mapDispatchToProps = (dispatch) => {}
ListTables = connect(mapStateToProps, mapDispatchToProps)(ListTables);

export default ListTables;
