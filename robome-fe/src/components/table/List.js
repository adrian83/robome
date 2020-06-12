import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import EditLink from '../tiles/EditLink';
import DeleteLink from '../tiles/DeleteLink';
import CreateLink from '../tiles/CreateLink';
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

    renderTableRow(table) {
        const tableId = table.key.tableId;
        const title = table.title;
        const description = table.description;

        const showTabUrl = showTableUrl(tableId);
        const editTabUrl = editTableUrl(tableId);

        return (
            <tr key={tableId}>
                <td><Link to={showTabUrl}>{title}</Link></td>
                <td>{description}</td>
                <td>
                    <EditLink to={editTabUrl}></EditLink>
                    <DeleteLink to="" onClick={this.delete(table)}></DeleteLink>
                </td>
            </tr>);
    }

    render() {
        const self = this;
        const createTabUrl = createTableUrl();

        const tables = (this.state && this.state.tables) ? this.state.tables : [];
        var rows = tables.map(table => self.renderTableRow(table));

        return (
            <div>
                <Title title="List tables" description="list of all created tables"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

                <div>
                    <CreateLink text="create table" to={createTabUrl}></CreateLink>
                </div>
                <br/><br/>
                <div>
                    <table className="table table-striped">
                        <thead>
                            <tr>
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
