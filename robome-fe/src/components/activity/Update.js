import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import BackLink from '../tiles/BackLink';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { activityBeUrl, showTableUrl } from '../../web/url';


class UpdateActivity extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        userId: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
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

        const updateActUrl = activityBeUrl(
            this.props.match.params.tableId, 
            this.props.match.params.stageId, 
            this.props.match.params.activityId);
        
        var act = {name: self.state.activity.name};

        securedPut(updateActUrl, authToken, act)
            .then(response => response.json())
            .then(data => self.setState({activity: data}))
            .then(_ => self.registerInfo("Activity updated"))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    componentDidMount() {
        const self = this;
        const authToken = this.props.authToken;
        
        const getActUrl = activityBeUrl(
            this.props.match.params.tableId,
            this.props.match.params.stageId,
            this.props.match.params.activityId);

        securedGet(getActUrl, authToken)
            .then(response => response.json())
            .then(data => self.setState({activity: data}))
            .catch(error => self.registerError(error));
    }


    render() {
        var activity = this.activityFromState();
        if(!activity.name){
            return (<div>waiting for data</div>);
        }

        const showTabUrl = showTableUrl(this.props.match.params.tableId);

        return (
            <div>
                <Title title={activity.name} description=""></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

                <div>
                    <BackLink to={showTabUrl} text="show table"></BackLink>
                </div>

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
            </div>);
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

UpdateActivity = connect(mapStateToProps, mapDispatchToProps)(UpdateActivity);

export default UpdateActivity;
