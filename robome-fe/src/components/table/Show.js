import React from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedDelete } from '../../web/ajax';
import { editActivityUrl, createActivityUrl, editStageUrl, tableBeUrl, editTableUrl, 
    createStageUrl, stageBeUrl, activityBeUrl } from '../../web/url';

class ShowTable extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);
        this.hideError = this.hideError.bind(this);
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
        if(!this.state || !this.state.table) {
            return (<div>waiting for table data</div>);
        }

        var tableData = this.renderTable(this.state.table);

        return (
            <div>
                {tableData}
            </div>
        );
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
                    <Link to={editTabUrl}>edit</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to={newStgUrl}>new stage</Link>
                </div>
                <br/><br/>
                {stages}
            </div>
            );
    }

    deleteStage(stageKey) {
        const self = this;
        const authToken = this.props.authToken;
        const table = this.state.table;

        return function(event) {
            securedDelete(stageBeUrl(stageKey.tableId, stageKey.stageId), authToken)
                .then(function(response){
                    var filtered = table.stages.filter((stage, index, arr) => stage.key.stageId !== stageKey.stageId);
                    table.stages = filtered;
                    self.setState({table: table});
                })
                .catch(error => self.registerError(error));

            event.preventDefault();
        }
    }

    deleteActivity(activityKey) {
        const self = this;
        const authToken = self.props.authToken;
        const table = self.state.table;

        const delStgUrl = activityBeUrl(activityKey.tableId, activityKey.stageId, activityKey.activityId);

        return function(event) {
            securedDelete(delStgUrl, authToken)
                .then(function(response){
                    table.stages.forEach(function(stage, index){
                        var filtered = stage.activities.filter((activity, index, arr) => activity.key.activityId !== activityKey.activityId);
                        stage.activities = filtered;
                    })
                    self.setState({table: table});
                })
                .catch(error => self.registerError(error));

            event.preventDefault();
        }
    }

    renderStage(stage) {
        const self = this;
        const stgKey = stage.key;

        var newActivityUrl = createActivityUrl(stgKey.tableId, stgKey.stageId);
        var updateStageUrl = editStageUrl(stgKey.tableId, stgKey.stageId);

        var activities = stage.activities.map(act => self.renderActivity(act));
        
        return (
            <div key={stage.title}>
                <h3>{stage.title}</h3>
                <div>
                    <Link to={newActivityUrl}>new activity</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to={updateStageUrl}>update</Link>&nbsp;&nbsp;&nbsp;&nbsp;
                    <Link to="" onClick={this.deleteStage(stage.key)}>delete</Link>
                </div>
                <br/>
                <h4>
                    {activities}
                </h4>
                <br/><br/>
            </div>);
    }

    renderActivity(activity) {
        const actKey = activity.key;
        const updateActUrl = editActivityUrl(actKey.tableId, actKey.stageId, actKey.activityId);

        return (
            <span className="badge badge-light" 
                style={{marginLeft: '10px'}} 
                key={actKey.activityId}>
                
                <div>
                    {activity.name}&nbsp;&nbsp;
                    <Link to={updateActUrl}>[e]</Link>&nbsp;&nbsp;
                    <Link to="" onClick={this.deleteActivity(actKey)}>[x]</Link>
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
