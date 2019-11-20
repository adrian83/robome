import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedDelete } from '../../web/ajax';
import { tablesBeUrl, tableBeUrl, editTableUrl, showTableUrl, createTableUrl } from '../../web/url';

class ListTables extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    componentDidMount() {

        const self = this;
        const authToken = self.props.authToken;

        securedGet(tablesBeUrl(), authToken)
            .then(response => response.json())
            .then(data => self.setState({tables: data}))
            .catch(error => self.registerError(error));
    }

    delete(table) {
        const self = this;
        const deleteTabUrl = tableBeUrl(table.key.tableId)

        return function(event) {

            securedDelete(deleteTabUrl, self.props.authToken)
                .then(function(response){
                    var filtered = self.state.tables.filter((tab, index, arr) => tab.key.tableId !== table.key.tableId);
                    self.setState({tables: filtered})
                })
                .then(data => self.registerInfo(`Table '${table.title}' removed`))
                .catch(error => self.registerError(error));

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
                    <Link to="" onClick={this.delete(table)}>delete</Link>
                </td>
            </tr>);
    }

    render() {
        const self = this;
        const createTabUrl = createTableUrl();

        var no = 1
        const tables = (this.state && this.state.tables) ? this.state.tables : [];
        var rows = tables.map(table => self.renderTableRow(no++, table));

        return (
            <div>
                <Title title="List tables" description="list of all created tables"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

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

const mapStateToProps = (state) =>{
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

ListTables = connect(mapStateToProps, mapDispatchToProps)(ListTables);

export default ListTables;
