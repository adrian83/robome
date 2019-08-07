
import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import { Redirect } from 'react-router-dom';

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

        const { jwtToken } = this.props;

        if (!jwtToken) {
            return <Redirect to='/login' />
        }

        var no = 1
        var rows = this.state.tables.map((table) => {
            return (<tr>
                        <th scope="row">{no++}</th>
                        <td>{table.title}</td>
                        <td>{table.description}</td>
                    </tr>);
        });

        return (
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
            </table>);
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
