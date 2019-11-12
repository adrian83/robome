import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Link } from 'react-router-dom';

import securedGet, { securedPut } from '../../web/ajax';

class UpdateActivity extends Component {

    constructor(props) { 
        super(props);

        this.state = {activity: {}};

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
    }

    handleNameChange(event) {
        var activity = this.state.activity;
        activity.name = event.target.value;
        this.setState({activity: activity});
    }

    handleSubmit(event) {

        const self = this;
        const backendHost = process.env.REACT_APP_BACKEND_HOST
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId

        const updateUrl = backendHost + "/tables/" + tableId + "/stages/" + stageId;
        
        securedPut(updateUrl, self.state.activity)
            .then(response => response.json())
            .then(data => self.setState({activity: data}));

        event.preventDefault();
    }


    componentDidMount() {

        const self = this;
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId;
        const activityId = this.props.match.params.activityId;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        
        const getActivityUrl = backendHost + "/tables/" + tableId + "/stages/" + stageId + "/activities/" + activityId;

        securedGet(getActivityUrl)
            .then(response => response.json())
            .then(data => self.setState({activity: data}));
    }


    render() {

        var tableId = this.props.match.params.tableId
        var showTableUrl = "/tables/show/" + tableId;

        var content = (<div>waiting for data</div>);
        if(this.state.activity && this.state.activity.name) {
            content = (
                <div>
                    <div>
                        <Link to={showTableUrl}>return to table</Link>
                    </div>
                    <form onSubmit={this.handleSubmit}>

                        <div className="form-group">

                            <label htmlFor="nameInput">Name</label>

                            <input type="name" 
                                    className="form-control" 
                                    id="nameInput" 
                                    placeholder="Enter name" 
                                    value={this.state.activity.name}
                                    onChange={this.handleNameChange} />
                        </div>

                        <button type="submit" 
                                className="btn btn-primary">Submit</button>

                    </form>
                </div>);
        }

        return content;
    }
}

const mapStateToProps = (state) => {
    return { };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpdateActivity = connect(mapStateToProps, mapDispatchToProps)(UpdateActivity);


export default UpdateActivity;
