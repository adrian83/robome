import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { tableBeUrl } from '../../web/url';

class UpdateTable extends Base {

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
            .then(data => self.setState({table: data}))
            .then(data => self.registerInfo("Table updated"))
            .catch(error => self.registerError(error));

        event.preventDefault();
    }

    componentDidMount() {
        const self = this;
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;
        
        securedGet(tableBeUrl(tableId), authToken)
            .then(response => response.json())
            .then(data => self.setState({table: data}))
            .catch(error => self.registerError(error));
    }

    render() {

        if(!this.state || ! this.state.table){
            return (<div>waiting for data</div>);
        }

        return (
            <div>
                <Title title={this.state.table.title} description={this.state.table.description}></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

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
