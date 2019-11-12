import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Link } from 'react-router-dom';

import Error from '../error/Error';
import Title from '../tiles/Title';

import securedGet from '../../web/ajax';


class ShowTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {table: {}};

        this.hideError = this.hideError.bind(this);
    }

    componentDidMount() {

        const self = this;
        const jwtToken = this.props.jwtToken;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const tableId = this.props.match.params.tableId;

        const fetchTableUrl = backendHost + "/tables/" + tableId;

        securedGet(fetchTableUrl, jwtToken)
            .then(response => response.json())
            .then(data => self.setState({table: data}))
            .catch(error => self.setState({error: error}));
    }

    isErrorPresent(){
        return this.state.error && this.state.error !== {};
    }

    hideError(event){
        this.setState({error: null});
        event.preventDefault();
    }

    showError(){
        return this.isErrorPresent() ? (<Error error={this.state.error} onClose={this.hideError}></Error>) : "";
    }

    render() {

        var self = this;

        var stages = [];
        
        var tableData = (<div>waiting for table data</div>);
        if(this.state.table && this.state.table.key && this.state.table.key.tableId) {
            var editUrl = "/tables/edit/" + this.state.table.key.tableId;
            var newStageUrl = "/tables/show/" + this.state.table.key.tableId + "/stages/create";
            tableData = (<div>
                    <Link to={editUrl}>edit</Link>
                    <br/>
                    <Link to={newStageUrl}>new stage</Link>
                </div>);

            stages = this.state.table.stages.map(stage => self.renderStage(stage));
        }


        return (
            <div>
                <Title title={this.state.table ? this.state.table.title : "-"} 
                        description={this.state.table ? this.state.table.description : "-"} ></Title>

                <div>{this.showError()}</div>

                {tableData}
                <br/>
                {stages}
            </div>
        );
    }

    renderStage(stage) {

        const tableId = stage.key.tableId;
        const stageId = stage.key.stageId;

        var newActivityUrl = "/tables/show/" + tableId + "/stages/show/" + stageId + "/activities/create";

        var activities = [];
        if(stage.activities){
            activities = stage.activities.map(act => (
                <span className="badge badge-light" 
                        style={{marginLeft: '10px'}} 
                        key={act.key.activityId}>{act.name}</span>));
        }

        return (
            <div key={stage.title}>
                <h3>{stage.title}</h3>
                <div><Link to={newActivityUrl}>new activity</Link></div>
                <h4>
                    {activities}
                </h4>
                <br/><br/>
            </div>);
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