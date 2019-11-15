import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../error/Error';
import Title from '../tiles/Title';

import securedGet, { securedDelete } from '../../web/ajax';


class ShowTable extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {table: {}};

        this.hideError = this.hideError.bind(this);
    }

    componentDidMount() {

        const self = this;
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;

        const fetchTableUrl = "/tables/" + tableId;

        securedGet(fetchTableUrl, authToken)
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
        
        var tableData = (<div>waiting for table data</div>);
        if(this.state.table && this.state.table.key && this.state.table.key.tableId) {
            tableData = this.renderTable(this.state.table);
        }

        return (
            <div>
                {tableData}
            </div>
        );
    }

    renderTable(table) {

        var self = this;
        var editUrl = "/tables/edit/" + table.key.tableId;
        var newStageUrl = "/tables/show/" + table.key.tableId + "/stages/create";

        var stages = table.stages.map(stage => self.renderStage(stage));

        return (
            <div>
                <Title title={this.state.table ? this.state.table.title : "-"} 
                        description={this.state.table ? this.state.table.description : "-"} ></Title>

                <div>{this.showError()}</div>

                <div>
                    <Link to={editUrl}>edit</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to={newStageUrl}>new stage</Link>
                </div>

                {stages}

            </div>
            );
    }

    deleteStage(stageKey) {
        const self = this;

        return function(event) {
            
            const authToken = self.props.authToken;

            securedDelete("/tables/" + stageKey.tableId + "/stages/" + stageKey.stageId, authToken)
                .then(function(response){
                    var table = self.state.table;
                    var filtered = table.stages.filter(function(stage, index, arr){
                        return stage.key.stageId != stageKey.stageId;
                    });
                    table.stages = filtered;
                    self.setState({table: table});
                })
                .catch(error => self.setState({error: error}));

            event.preventDefault();
        }
    }

    deleteActivity(activityKey) {
        const self = this;

        return function(event) {
            
            const authToken = self.props.authToken;

            securedDelete("/tables/" + activityKey.tableId + "/stages/" + activityKey.stageId + "/activities/" + activityKey.activityId, authToken)
                .then(function(response){
                    var table = self.state.table;

                    table.stages.forEach(function(stage, index){
                        var filtered = stage.activities.filter(function(activity, index, arr){
                            return activity.key.activityId != activityKey.activityId;
                        });
                        stage.activities = filtered;
                    })
                    
                    self.setState({table: table});
                })
                .catch(error => self.setState({error: error}));

            event.preventDefault();
        }
    }

    renderStage(stage) {
        var self = this;
        const tableId = stage.key.tableId;
        const stageId = stage.key.stageId;

        var newActivityUrl = "/tables/show/" + tableId + "/stages/show/" + stageId + "/activities/create";
        var updateStageUrl = "/tables/show/" + tableId + "/stages/edit/" + stageId;

        var activities = stage.activities.map(act => self.renderActivity(act));
        
        return (
            <div key={stage.title}>
                <h3>{stage.title}</h3>
                <div>
                    <Link to={newActivityUrl}>new activity</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to={updateStageUrl}>update</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to="" onClick={this.deleteStage(stage.key)}>delete</Link>
                </div>
                <h4>
                    {activities}
                </h4>
                <br/><br/>
            </div>);
    }

    renderActivity(activity) {

        const tableId = activity.key.tableId;
        const stageId = activity.key.stageId;
        const activityId = activity.key.activityId;

        var updateActivityUrl = "/tables/show/" + tableId + "/stages/show/" + stageId + "/activities/edit/" + activityId;

        return (
            <span className="badge badge-light" 
                style={{marginLeft: '10px'}} 
                key={activity.key.activityId}>
                
                <div>
                    {activity.name}
                </div>
                <div>
                    <Link to={updateActivityUrl}>update</Link>&nbsp;&nbsp;
                    <Link to="" onClick={this.deleteActivity(activity.key)}>delete</Link>
                </div>
            </span>
        );
    }
}

const mapStateToProps = (state) => {
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ShowTable = connect(mapStateToProps, mapDispatchToProps)(ShowTable);


export default ShowTable;
