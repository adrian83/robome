import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';


class ShowTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        console.log(JSON.stringify(props))

        this.state = {table: {}, stages: []};
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

        fetch("http://localhost:6060/tables/" + tableId + "/stages", {
            method: 'GET',
            mode: 'cors',
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({stages: data}))
        .catch(error => console.log("getting stages error", error));
    }


    render() {

        var tableData = (<div>waiting for table data</div>);
        if(this.state.table && this.state.table.key && this.state.table.key.tableId) {
            var editUrl = "/tables/edit/" + this.state.table.key.tableId;
            var newStageUrl = "/tables/show/" + this.state.table.key.tableId + "/stages/create";
            tableData = (<div>
                    <div>Title: {this.state.table.title}</div>
                    <div>Description: {this.state.table.description}</div>
                    <Link to={editUrl}>edit</Link>
                    <br/>
                    <Link to={newStageUrl}>new stage</Link>
                </div>);
        }

        var stages = [];
        if(this.state.stages) {
            stages = this.state.stages.map((stage) => {
                return (<div>{stage.title}</div>);
            });
        }

        return (
            <div>
                {tableData}
                <br/>
                {stages}
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
