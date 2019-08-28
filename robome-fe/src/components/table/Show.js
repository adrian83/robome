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

        this.state = {table: {}, stages: []};

        this.hideError = this.hideError.bind(this);
    }

    fetchActivities(stage, jwtToken) {
        var stageId = stage.key.stageId;
        var tableId = stage.key.tableId;

        var fetchActivitiesUrl = "http://localhost:6060/tables/" + tableId + "/stages/" + stageId + "/activities";

        return securedGet(fetchActivitiesUrl, jwtToken)
        .then(response => response.json());
    }

    componentDidMount() {

        var jwtToken = this.props.jwtToken;
        var tableId = this.props.match.params.tableId;

        var fetchTableUrl = "http://localhost:6060/tables/" + tableId;
        var fetchStagesUrl = "http://localhost:6060/tables/" + tableId + "/stages";

        var self = this;

        securedGet(fetchTableUrl, jwtToken)
        .then(response => response.json())
        .then(data => self.setState({table: data}))
        .catch(error => self.setState({error: error}));

        securedGet(fetchStagesUrl, jwtToken)
        .then(response => response.json())
        .then(function(data){
            self.setState({stages: data});
            return data;
        })
        .then(function(data){

            Promise.all(data.map(stage => self.fetchActivities(stage, jwtToken)))
            .then(function(iter){

                var activities = {};
                iter.forEach(function(act){
                    if(act && act.length){
                        activities[act[0].key.stageId] = act;
                    }
                });
                self.setState({activities: activities});
            });
        })
        .catch(error => self.setState({error: error}));
    }

    renderStage(stage) {

        const tableId = stage.key.tableId;
        const stageId = stage.key.stageId;

        var newActivityUrl = "/tables/show/" + tableId + "/stages/show/" + stageId + "/activities/create";


        var activities = [];
        if(this.state.activities){
            var stageActivities = this.state.activities[stageId];

            if(stageActivities){
                activities = stageActivities.map(act => (
                    <span className="badge badge-light" key={act.key.activityId}>{act.name}</span>));
            }
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
        
        var tableData = (<div>waiting for table data</div>);
        if(this.state.table && this.state.table.key && this.state.table.key.tableId) {
            var editUrl = "/tables/edit/" + this.state.table.key.tableId;
            var newStageUrl = "/tables/show/" + this.state.table.key.tableId + "/stages/create";
            tableData = (<div>
                    <Link to={editUrl}>edit</Link>
                    <br/>
                    <Link to={newStageUrl}>new stage</Link>
                </div>);
        }

        var stages = [];
        if(this.state.stages){
            stages = this.state.stages.map(stage => self.renderStage(stage));
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
}

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ShowTable = connect(mapStateToProps, mapDispatchToProps)(ShowTable);


export default ShowTable;
