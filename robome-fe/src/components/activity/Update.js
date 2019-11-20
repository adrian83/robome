import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../notification/Error';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { activityBeUrl } from '../../web/url';


class UpdateActivity extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

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
        const authToken = this.props.authToken;

        const updateActUrl = activityBeUrl(
            this.props.match.params.tableId, 
            this.props.match.params.stageId, 
            this.props.match.params.activityId);
        
        var act = {name: self.state.activity.name};

        securedPut(updateActUrl, authToken, act)
            .then(response => response.json())
            .then(data => self.setState({activity: data}))
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

        if(!this.state || ! this.state.activity){
            return (<div>waiting for data</div>);
        }

        return (
            <div>
                <Title title={this.state.activity.name} description=""></Title>

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
                                value={this.state.activity.name}
                                onChange={this.handleNameChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Submit</button>
                </form>
            </div>);
    }
}

const mapStateToProps = (state) => {
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpdateActivity = connect(mapStateToProps, mapDispatchToProps)(UpdateActivity);

export default UpdateActivity;
