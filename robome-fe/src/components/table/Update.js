import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../error/Error';
import Title from '../tiles/Title';

import securedGet, { securedPut } from '../../web/ajax';
import { tableBeUrl } from '../../web/url';

class UpdateTable extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleTitleChange = this.handleTitleChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    }

    handleTitleChange(event) {
        var table = this.state.table ? this.state.table : {};
        table.title = event.target.value;
        this.setState({table: table});
    }

    handleDescriptionChange(event) {
        var table = this.state.table ? this.state.table : {};
        table.description = event.target.value;
        this.setState({table: table});
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

    handleSubmit(event) {
        const self = this;
        const tableId = this.props.match.params.tableId
        const authToken = this.props.authToken;

        const tab = {
            title: self.state.table.title, 
            description: self.state.table.description
        };
        
        securedPut(tableBeUrl(tableId), authToken, tab)
            .then(response => response.json())
            .then(data => self.setState({table: data}));

        event.preventDefault();
    }

    componentDidMount() {
        const self = this;
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;
        
        securedGet(tableBeUrl(tableId), authToken)
            .then(response => response.json())
            .then(data => self.setState({table: data}));
    }

    render() {

        if(!this.state || ! this.state.table){
            return (<div>waiting for data</div>);
        }

        return (
            <div>
                <Title title={this.state.table.title} description={this.state.table.description}></Title>

                <div>{this.showError()}</div>

                <form onSubmit={this.handleSubmit}>

                    <div className="form-group">

                        <label htmlFor="titleInput">Title</label>

                        <input type="title" 
                                className="form-control" 
                                id="titleInput" 
                                placeholder="Enter title" 
                                value={this.state.table.title}
                                onChange={this.handleTitleChange} />
                    </div>

                    <div className="form-group">

                        <label htmlFor="descriptionInput">Description</label>

                        <input type="description" 
                                className="form-control" 
                                id="descriptionInput" 
                                placeholder="Enter description" 
                                value={this.state.table.description}
                                onChange={this.handleDescriptionChange} />
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

UpdateTable = connect(mapStateToProps, mapDispatchToProps)(UpdateTable);


export default UpdateTable;
