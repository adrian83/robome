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
        authToken: PropTypes.string,
        userId: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.hideError = this.hideError.bind(this);
        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTitleChange = this.handleTitleChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    }

    tableFromState() {
        return (this.state && this.state.table) ? this.state.table : {};
    }

    handleTitleChange(event) {
        var table = this.tableFromState();
        table.title = event.target.value;
        this.setState({table: table});
    }

    handleDescriptionChange(event) {
        var table = this.tableFromState();
        table.description = event.target.value;
        this.setState({table: table});
    }

    handleSubmit(event) {
        const self = this;
        const authToken = this.props.authToken;
        const userId = this.props.userId;
        const table = this.tableFromState();

        securedPost(tablesBeUrl(userId), authToken, table)
            .then(response => response.json())
            .then(data => self.setState({table: data}))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    render() {
        const table = this.tableFromState();
        if(table.key) {
            var editUrl = editTableUrl(table.key.tableId);
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
                                value={table.title}
                                onChange={this.handleTitleChange} />
                    </div>

                    <div className="form-group">
                        <label htmlFor="descriptionInput">Description</label>
                        <input type="description" 
                                className="form-control" 
                                id="descriptionInput" 
                                placeholder="Enter description" 
                                value={table.description}
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
    return {
        authToken: state.authToken,
        userId: state.userId
    };
};

const mapDispatchToProps = (dispatch) => {
    return {};
};

CreateTable = connect(mapStateToProps, mapDispatchToProps)(CreateTable);

export default CreateTable;
