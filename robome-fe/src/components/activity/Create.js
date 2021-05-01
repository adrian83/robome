import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import BackLink from '../tiles/BackLink';
import Title from '../tiles/Title';
import Base from '../Base';

import { securedPost } from '../../web/ajax';
import { activitiesBeUrl, editActivityUrl, showTableUrl } from '../../web/url';


class CreateActivity extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        userId: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
        this.hideError = this.hideError.bind(this);
    }

    activityFromState() {
        return (this.state && this.state.activity) ? this.state.activity : {};
    }

    handleNameChange(event) {
        var activity = this.activityFromState();
        activity.name = event.target.value;
        this.setState({activity: activity});
    }

    handleSubmit(event) {
        const self = this;
        const authToken = this.props.authToken;
        const activity = this.activityFromState();

        const editUrl = activitiesBeUrl(
            this.props.userId,
            this.props.match.params.tableId, 
            this.props.match.params.stageId);

        securedPost(editUrl, authToken, activity)
            .then(response => response.json())
            .then(data => self.setState({activity: data}))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {
        var activity = this.activityFromState();
        if(activity.key) {
            var editActUrl = editActivityUrl(
                activity.key.tableId, 
                activity.key.stageId, 
                activity.key.activityId);

            return (<Redirect to={editActUrl} />);
        }

        const showTabUrl = showTableUrl(this.props.match.params.tableId);

        return (
            <div>
                <Title title="Create new activity" description="Fill basic data"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                
                <div>
                    <BackLink to={showTabUrl} text="show table"></BackLink>
                </div>

                <br/>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">

                        <label htmlFor="nameInput">Name</label>

                        <input type="name" 
                                className="form-control" 
                                id="nameInput" 
                                placeholder="Enter name" 
                                value={activity.name}
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
    return {
        authToken: state.authToken,
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateActivity = connect(mapStateToProps, mapDispatchToProps)(CreateActivity);

export default CreateActivity;
