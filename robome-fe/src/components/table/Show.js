import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import EditLink from '../tiles/EditLink';
import DeleteLink from '../tiles/DeleteLink';
import CreateLink from '../tiles/CreateLink';
import MoveActLink from '../tiles/MoveActLink';

import Base from '../Base';

import securedGet, { securedDelete, securedPost } from '../../web/ajax';
import { editActivityUrl, createActivityUrl, editStageUrl, tableBeUrl, editTableUrl, 
    createStageUrl, stageBeUrl, activityBeUrl, activitiesBeUrl } from '../../web/url';

class ShowTable extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.hideError = this.hideError.bind(this);
    }

    tableFromState() {
        return (this.state && this.state.table) ? this.state.table : {};
    }

    componentDidMount() {
        const self = this;
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;

        securedGet(tableBeUrl(tableId), authToken)
            .then(response => response.json())
            .then(data => self.setState({table: data}))
            .catch(error => self.registerError(error));
    }

    render() {
        var table = this.tableFromState();
        if(!table.title) {
            return (<div>waiting for table data</div>);
        }

        var tableData = this.renderTable(table);
        return (<div>{tableData}</div>);
    }

    renderTable(table) {
        var self = this;

        var editTabUrl = editTableUrl(table.key.tableId);
        var newStgUrl = createStageUrl(table.key.tableId);

        var stages = table.stages.map(stage => self.renderStage(stage));

        return (
            <div>
                <Title title={this.state.table.title} description={this.state.table.description} ></Title>
                
                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>
                
                <div>
                    <EditLink to={editTabUrl} text="edit table"></EditLink>
                    <CreateLink to={newStgUrl} text="new stage"></CreateLink>
                </div>
                <br/><br/>
                {stages}
            </div>
            );
    }

    deleteStage(stageKey) {
        const self = this;
        const authToken = this.props.authToken;

        return function(event) {
            securedDelete(stageBeUrl(stageKey.tableId, stageKey.stageId), authToken)
                .then(function(_){
                    var table = self.tableFromState();
                    table.stages = table.stages.filter((stage, index, arr) => stage.key.stageId !== stageKey.stageId);
                    self.setState({table: table});
                })
                .catch(error => self.registerError(error));

            event.preventDefault();
        }
    }

    deleteActivity(activityKey) {
        const self = this;
        const authToken = self.props.authToken;
        const delStgUrl = activityBeUrl(activityKey.tableId, activityKey.stageId, activityKey.activityId);

        return function(event) {
            securedDelete(delStgUrl, authToken)
                .then(function(response){
                    var table = self.tableFromState();
                    table.stages.forEach(function(stage, index){
                        stage.activities = stage.activities.filter((activity, index, arr) => activity.key.activityId !== activityKey.activityId);
                    })
                    self.setState({table: table});
                })
                .catch(error => self.registerError(error));

            event.preventDefault();
        }
    }

    selectActivity(activity) {
        const self = this;
        return function(event) {
            self.setState({move: activity});
            event.preventDefault();
        }
    }

    moveActivity(destStage) {
        const self = this;
        const authToken = this.props.authToken;

        return function(event) {
    
            var act = self.state.move;

            const editUrl = activitiesBeUrl(
                act.key.tableId, 
                destStage.key.stageId);
    
            var activ = {name: act.name};

            securedPost(editUrl, authToken, activ)
                .then(response => response.json())
                .then(function(newAct){

                var delActUrl = activityBeUrl(  
                    act.key.tableId, 
                    act.key.stageId,
                    act.key.activityId);

                return securedDelete(delActUrl, authToken)
                .then(function(data){
                    var sourceStageId = act.key.stageId;

                    var table = self.state.table;
                    var stage = table.stages.find(s => s.key.stageId === destStage.key.stageId);
                    
                    stage.activities.push(newAct);
                    
                    var sourceStage = table.stages.find(s => s.key.stageId === sourceStageId);
                    var activities = sourceStage.activities.filter((activity, index, arr) => activity.key.activityId !== act.key.activityId);
                    sourceStage.activities = activities;

                    self.setState({table: table});
                    self.setState({move: null});
                })
                .catch(error => self.registerError(error));
            })
            .catch(error => self.registerError(error));
    

            event.preventDefault();
        }
    }

    renderStagesDropdown(stageId) {
        var stagesLinks = this.state.table.stages.filter((stage, index, arr) => stage.key.stageId !== stageId).map(stage => (<span key={stage.key.stageId} className="dropdown-item" onClick={this.moveActivity(stage)}>{stage.title}</span>));

        return (
            <div className="btn-group">
                <button className="btn btn-secondary btn-sm dropdown-toggle" type="button" data-toggle="dropdown" aria-haspopup="true" aria-expanded="false">
                    Select stage
                </button>
                <div className="dropdown-menu">
                    {stagesLinks}
                </div>
            </div>);
    }

    renderStage(stage) {
        const self = this;
        const stgKey = stage.key;

        var newActivityUrl = createActivityUrl(stgKey.tableId, stgKey.stageId);
        var updateStageUrl = editStageUrl(stgKey.tableId, stgKey.stageId);

        var activities = stage.activities.map(act => self.renderActivity(act));
        
        return (
            <div key={stage.title}>
                <div className="border border-primary rounded">
                    <br/>

                    <h3>{stage.title}</h3>

                    <div>
                        <CreateLink to={newActivityUrl} text="new activity"></CreateLink>
                        <EditLink to={updateStageUrl} text="update stage"></EditLink>
                        <DeleteLink onClick={this.deleteStage(stage.key)} text="delete stage"></DeleteLink>
                    </div>
                    <br/>
                </div>

                <br/>

                <h4>{activities}</h4>

                <br/><br/>
            </div>);
    }

    renderActivity(activity) {
        const actKey = activity.key;
        const updateActUrl = editActivityUrl(actKey.tableId, actKey.stageId, actKey.activityId);


        var h = (this.state && this.state.move && (this.state.move.key.activityId === actKey.activityId)) ? this.renderStagesDropdown(this.state.move.key.stageId) : "";


        return (
            <span className="badge badge-light" 
                style={{marginLeft: '10px'}} 
                key={actKey.activityId}>
                
                <div>
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    {activity.name}
                    &nbsp;&nbsp;&nbsp;&nbsp;
                    {h}
                    <MoveActLink onClick={this.selectActivity(activity)}></MoveActLink>
                    <EditLink to={updateActUrl}></EditLink>
                    <DeleteLink onClick={this.deleteActivity(actKey)}></DeleteLink>
                </div>
            </span>
        );
    }
}

const mapStateToProps = (state) =>{
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

ShowTable = connect(mapStateToProps, mapDispatchToProps)(ShowTable);

export default ShowTable;
