import React from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';

import Error from '../notification/Error';
import Info from '../notification/Info';
import Title from '../tiles/Title';
import BackLink from '../tiles/BackLink';
import Base from '../Base';

import securedGet, { securedPut } from '../../web/ajax';
import { tableBeUrl, showTableUrl } from '../../web/url';

class UpdateTable extends Base {

    static propTypes = {
        authToken: PropTypes.string,
        userId: PropTypes.string
    };

    constructor(props) { 
        super(props);

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
        const tableId = this.props.match.params.tableId;
        const authToken = this.props.authToken;
        const userId = this.props.userId;
        const table = this.tableFromState();

        const updatedTable = {
            title: table.title, 
            description: table.description
        };
        
        securedPut(tableBeUrl(userId, tableId), authToken, updatedTable)
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
        const userId = this.props.userId;
        
        securedGet(tableBeUrl(userId, tableId), authToken)
            .then(response => response.json())
            .then(data => self.setState({table: data}))
            .catch(error => self.registerError(error));
    }

    render() {
        var table = this.tableFromState();
        if(!table.key){
            return (<div>waiting for data</div>);
        }

        const showTabUrl = showTableUrl(this.props.match.params.tableId);

        return (
            <div>
                <Title title={table.title} description={table.description}></Title>

                <Error errors={this.errors()} hideError={this.hideError} ></Error>
                <Info info={this.info()} hideInfo={this.hideInfo} ></Info>

                <div>
                    <BackLink to={showTabUrl} text="show table"></BackLink>
                </div>
                <br/>

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

UpdateTable = connect(mapStateToProps, mapDispatchToProps)(UpdateTable);

export default UpdateTable;
