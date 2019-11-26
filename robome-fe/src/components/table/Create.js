import React from 'react';
import { connect } from 'react-redux';
import { Redirect } from 'react-router-dom';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import Base from '../Base';

import { securedPost } from '../../web/ajax';
import { tablesBeUrl, editTableUrl } from '../../web/url';


class CreateTable extends Base {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.state =  {
            title: '', 
            description: ''
        };

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
            .then(data => self.setState({table: data}))
            //.then(data => self.registerInfo("Table added"))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {

        if(this.state && this.state.table) {
            var editUrl = editTableUrl(
                this.state.table.key.tableId);

            return (<Redirect to={editUrl} />);
        }

        return (
            <div>
                <Title title="Create table" description="create table"></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

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
    return {authToken: state.authToken};
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

CreateTable = connect(mapStateToProps, mapDispatchToProps)(CreateTable);

export default CreateTable;
