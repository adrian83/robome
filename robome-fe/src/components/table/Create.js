import React, { Component } from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../error/Error';
import Title from '../tiles/Title';

import { securedPost } from '../../web/ajax';
import { tablesBeUrl, editTableUrl } from '../../web/url';


class CreateTable extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {title: '', description: ''};

        this.hideError = this.hideError.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTitleChange = this.handleTitleChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    }

    handleTitleChange(event) {
        this.setState({title: event.target.value});
    }

    handleDescriptionChange(event) {
        this.setState({description: event.target.value});
    }

    handleSubmit(event) {

        const self = this;
        const authToken = this.props.authToken;

        const form = {
            title: this.state.title,
            description: this.state.description
        }

        securedPost(tablesBeUrl(), authToken, form)
            .then(response => response.json())
            .then(data => self.setState({table: data}));

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

        if(this.state.table) {
            var editUrl = editTableUrl(this.state.key.tableId);
            return (<Redirect to={editUrl} />);
        }

        return (
            <div>
                <Title title="Create table" description="create table"></Title>

                <div>{this.showError()}</div>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">
                        <label htmlFor="titleInput">Title</label>
                        <input type="title" 
                                className="form-control" 
                                id="titleInput" 
                                placeholder="Enter title" 
                                value={this.state.title}
                                onChange={this.handleTitleChange} />
                    </div>

                    <div className="form-group">
                        <label htmlFor="descriptionInput">Title</label>
                        <input type="description" 
                                className="form-control" 
                                id="descriptionInput" 
                                placeholder="Enter description" 
                                value={this.state.description}
                                onChange={this.handleDescriptionChange} />
                    </div>

                    <button type="submit" 
                            className="btn btn-primary">Create</button>
                </form>
            </div>
        );
    }
}

const mapStateToProps = (state) => {return {authToken: state.authToken};};
const mapDispatchToProps = (dispatch) => {}
CreateTable = connect(mapStateToProps, mapDispatchToProps)(CreateTable);

export default CreateTable;
