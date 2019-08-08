import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';


class ShowTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        console.log(JSON.stringify(props))

        this.state = {table: {}};
    }

    componentDidMount() {

        var jwtToken = this.props.jwtToken;
        var tableId = this.props.match.params.tableId;

        var self = this;

        fetch('http://localhost:6060/tables/' + tableId, {
            method: 'GET',
            mode: 'cors',
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({table: data}));

    }


    render() {

        var info = (<div>waiting for data</div>);
        if(this.state.table) {
            info = (<div>
                    <div>Title: {this.state.table.title}</div>
                    <div>Description: {this.state.table.description}</div>
                </div>);
        }

        return (
            <div>
                {info}
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

ShowTable = connect(mapStateToProps, mapDispatchToProps)(ShowTable);


export default ShowTable;
