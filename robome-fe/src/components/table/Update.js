import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';


class UpdateTable extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        console.log(JSON.stringify(props))

        this.state = {table: {}};

        this.handleSubmit = this.handleSubmit.bind(this);

        this.handleTitleChange = this.handleTitleChange.bind(this);
        this.handleDescriptionChange = this.handleDescriptionChange.bind(this);
    }


    handleTitleChange(event) {
        var table = this.state.table;
        table.title = event.target.value;
        this.setState({table: table});
    }

    handleDescriptionChange(event) {
        var table = this.state.table;
        table.description = event.target.value;
        this.setState({table: table});
    }

    handleSubmit(event) {

        const jwtToken = this.props.jwtToken;
        const self = this;
        const updateUrl = 'http://localhost:6060/tables/' + this.props.match.params.tableId;
        
        fetch(updateUrl, {
            method: 'PUT',
            mode: 'cors',
            body: JSON.stringify(self.state.table),
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({table: data}));

        event.preventDefault();
    }


    componentDidMount() {

        var jwtToken = this.props.jwtToken;
        var tableId = this.props.match.params.tableId;
        var self = this;

        fetch('http://localhost:6060/tables/' + tableId, {
            method: 'GET',
            mode: 'cors',
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({table: data}));
    }


    render() {

        var content = (<div>waiting for data</div>);
        if(this.state.table && this.state.table.title && this.state.table.description) {
            content = (<form onSubmit={this.handleSubmit}>

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

                    <label htmlFor="descriptionInput">Title</label>

                    <input type="description" 
                            className="form-control" 
                            id="descriptionInput" 
                            placeholder="Enter description" 
                            value={this.state.table.description}
                            onChange={this.handleDescriptionChange} />
                </div>

                <button type="submit" 
                        className="btn btn-primary">Submit</button>

            </form>);
        }

        return content;
    }
}

const mapStateToProps = (state) => {
    return { jwtToken: state.jwtToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

UpdateTable = connect(mapStateToProps, mapDispatchToProps)(UpdateTable);


export default UpdateTable;
