import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Redirect } from 'react-router-dom';

import Title from '../tiles/Title';

import { securedPost } from '../../web/ajax';


class CreateTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state = {title: '', description: ''};

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

        event.preventDefault();

        const self = this;
        const jwtToken = this.props.jwtToken;

        const form = {
            title: this.state.title,
            description: this.state.description
        }

        securedPost('http://localhost:6060/tables', jwtToken, form)
        .then(response => response.json())
        .then(data => self.setState({key: data.key}));
    }

    render() {

        if(this.state.key && this.state.key.tableId) {
            var editUrl = "/tables/edit/" + this.state.key.tableId;
            return (<Redirect to={editUrl} />);
        }

        return (
            <div>
                <Title title="Create table" description="create table"></Title>

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

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

CreateTable = connect(mapStateToProps, mapDispatchToProps)(CreateTable);


export default CreateTable;
