import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import { securedPost } from '../../web/ajax';
import { activitiesBeUrl, editActivityUrl } from '../../web/url';


class CreateActivity extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {
            activity: {name: ""}
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
        const self = this;
        const authToken = this.props.authToken;

        const editUrl = activitiesBeUrl(
            this.props.match.params.tableId, 
            this.props.match.params.stageId);

        securedPost(editUrl, authToken, this.state.activity)
            .then(response => response.json())
            .then(data => self.setState({key: data.key}))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {

        if(this.state && this.state.key) {
            var editActUrl = editActivityUrl(
                this.state.key.tableId, 
                this.state.key.stageId, 
                this.state.key.activityId);

            return (<Redirect to={editActUrl} />);
        }

        return (
            <div>
                <Title title="Create new activity" description="Fill basic data"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                
                <div>
                    <TableLink text="show table" tableId={this.props.match.params.tableId}></TableLink>
                </div>

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
