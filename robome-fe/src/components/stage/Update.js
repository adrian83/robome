import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';


class UpdateStage extends Component {

    static propTypes = {
        jwtToken: PropTypes.string
    };

    constructor(props) { 
        super(props);

        console.log(JSON.stringify(props))

        this.state = {stage: {}};

        this.handleSubmit = this.handleSubmit.bind(this);
        this.handleNameChange = this.handleNameChange.bind(this);
    }

    handleNameChange(event) {
        var stage = this.state.stage;
        stage.title = event.target.value;
        this.setState({stage: stage});
    }

    handleSubmit(event) {

        const jwtToken = this.props.jwtToken;
        const self = this;

        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId

        const updateUrl = "http://localhost:6060/tables/" + tableId + "/stages/" + stageId;
        
        fetch(updateUrl, {
            method: 'PUT',
            mode: 'cors',
            body: JSON.stringify(self.state.stage),
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({stage: data}));

        event.preventDefault();
    }


    componentDidMount() {

        const jwtToken = this.props.jwtToken;
        const tableId = this.props.match.params.tableId;
        const stageId = this.props.match.params.stageId
        const self = this;

        const getUrl = "http://localhost:6060/tables/" + tableId + "/stages/" + stageId;

        fetch(getUrl, {
            method: 'GET',
            mode: 'cors',
            headers: {
                "Content-type": "application/json; charset=UTF-8",
                "Authorization": jwtToken
            }
        })
        .then(response => response.json())
        .then(data => self.setState({stage: data}));
    }


    render() {

        var content = (<div>waiting for data</div>);
        if(this.state.stage && this.state.stage.title) {
            content = (<form onSubmit={this.handleSubmit}>

                <div className="form-group">

                    <label htmlFor="nameInput">Name</label>

                    <input type="name" 
                            className="form-control" 
                            id="nameInput" 
                            placeholder="Enter name" 
                            value={this.state.stage.title}
                            onChange={this.handleNameChange} />
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

UpdateStage = connect(mapStateToProps, mapDispatchToProps)(UpdateStage);


export default UpdateStage;
