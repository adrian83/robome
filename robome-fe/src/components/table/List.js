
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

class ListTables extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {tables: []};
    }

    componentDidMount() {

        const { jwtToken } = this.props;
        if (!jwtToken) {
            return;
        }

        var self = this;

        fetch('http://localhost:6060/tables', {
            method: 'GET',
            mode: 'cors',
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({tables: data}));
    }

    render() {

        var no = 1
        var rows = this.state.tables.map((table) => {
            var tableUrl = "/tables/show/" + table.key.tableId;
            return (<tr key={table.key.tableId}>
                        <th scope="row">{no++}</th>
                        <td><Link to={tableUrl}>{table.title}</Link></td>
                        <td>{table.description}</td>
                    </tr>);
        });

        return (
            <div>
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
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ListTables = connect(mapStateToProps, mapDispatchToProps)(ListTables);


export default ListTables;
