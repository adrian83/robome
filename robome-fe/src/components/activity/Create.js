import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Redirect, Link } from 'react-router-dom';
import PropTypes from 'prop-types';

import { securedPost } from '../../web/ajax';
import Error from '../error/Error';


class CreateActivity extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {
            activity: {name: ""},
            error: {}
        };

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);

        this.hideError = this.hideError.bind(this);
    }

    handleNameChange(event) {
        var activity = this.state.activity;
        activity.name = event.target.value;
        this.setState({activity: activity});
    }


    handleSubmit(event) {

        event.preventDefault();

        const self = this;
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId;
        const backendHost = process.env.REACT_APP_BACKEND_HOST;
        const authToken = this.props.authToken;

        const editUrl = backendHost + "/tables/" + tableId + "/stages/" + stageId + "/activities";

        securedPost(editUrl, authToken, this.state.activity)
        .then(response => response.json())
        .then(data => self.setState({key: data.key}))
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

        if(this.state.key && this.state.key.tableId && this.state.key.stageId && this.state.key.activityId) {
            var editUrl = "/tables/show/" + this.state.key.tableId + "/stages/show/" + this.state.key.stageId + "/activities/edit/" + this.state.key.activityId;
            return (<Redirect to={editUrl} />);
        }

        var tableId = this.props.match.params.tableId;
        var showTableUrl = "/tables/show/" + tableId;



        return (
            <div>
                <div>
                    <Link to={showTableUrl}>return to table</Link>
                </div>

                <div>{this.showError()}</div>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">

                        <label htmlFor="nameInput">Name</label>

                        <input type="name" 
                                className="form-control" 
                                id="nameInput" 
                                placeholder="Enter name" 
                                value={this.state.name}
                                onChange={this.handleNameChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Submit</button>

                </form>
            </div>

        );
    }
}

const mapStateToProps = (state) => {
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateActivity = connect(mapStateToProps, mapDispatchToProps)(CreateActivity);


export default CreateActivity;
