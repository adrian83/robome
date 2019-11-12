import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

import Error from '../error/Error';
import Title from '../tiles/Title';

import securedGet from '../../web/ajax';


class ShowTable extends Component {

    constructor(props) { 
        super(props);

        this.state = {table: {}};

        this.hideError = this.hideError.bind(this);
    }

    componentDidMount() {

        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const tableId = this.props.match.params.tableId;

        const fetchTableUrl = backendHost + "/tables/" + tableId;

        securedGet(fetchTableUrl)
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
                    <Link to={updateStageUrl}>update</Link>
                </div>
                <h4>
                    {activities}
                </h4>
                <br/><br/>
            </div>);
    }

    renderActivity(activity) {
        return (
            <span className="badge badge-light" 
                style={{marginLeft: '10px'}} 
                key={activity.key.activityId}>{activity.name}</span>
        );
    }
}

const mapStateToProps = (state) => {
    return {};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

ShowTable = connect(mapStateToProps, mapDispatchToProps)(ShowTable);


export default ShowTable;
