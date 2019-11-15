import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import TableLink from '../navigation/TableLink';
import Error from '../error/Error';
import Title from '../tiles/Title';

import { securedPost } from '../../web/ajax';


class CreateStage extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {name: ''};

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);

        this.hideError = this.hideError.bind(this);
    }

    handleNameChange(event) {
        this.setState({name: event.target.value});
    }


    handleSubmit(event) {

        const self = this;
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;

        const editUrl = "/tables/" + tableId + "/stages" 

        var form = {
            name: this.state.name
        };

        securedPost(editUrl, authToken, form)
            .then(response => response.json())
            .then(data => self.setState({key: data.key}))
            .catch(error => self.setState({error: error}));
            
        event.preventDefault();
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

        if(this.state.key && this.state.key.tableId && this.state.key.stageId) {
            var editUrl = "/tables/show/" + this.state.key.tableId + "/stages/edit/" + this.state.key.stageId;
            return (<Redirect to={editUrl} />);
        }

        return (
            <div>
                <Title title="Create new stage" description="Fill basic data"></Title>

                <div>{this.showError()}</div>

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

CreateStage = connect(mapStateToProps, mapDispatchToProps)(CreateStage);


export default CreateStage;
