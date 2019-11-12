import React, { Component } from 'react';
import { connect } from 'react-redux';
import PropTypes from 'prop-types';
import { Route, Redirect, Switch as RSwitch } from 'react-router-dom';

import Home from './../../Home';
import Health from './../../Health';
import Login from './../auth/Login';
import Logout from './../auth/Logout';
import Register from './../auth/Register';

import CreateTable from './../table/Create';
import ListTables from './../table/List';
import ShowTable from './../table/Show';
import UpdateTable from './../table/Update';

import CreateStage from './../stage/Create';
import UpdateStage from './../stage/Update';

import CreateActivity from './../activity/Create';
import UpdateActivity from './../activity/Update';

class Switch extends Component {

    static propTypes = {
        authenticated: PropTypes.bool
    };

    render() {

        const authenticated = this.props.authenticated;
        
        var renderListTables = authenticated ? () => <ListTables/> : () => <Redirect to='/login' />; 

        return (
            <RSwitch>
                <Route exact path="/" component={Home} />
                <Route path="/login" component={() => <Login/>} />
                <Route path="/logout" component={() => <Logout/>} />
                <Route path="/register" component={() => <Register/>} />
                <Route path="/health" component={() => <Health/>} />
                <Route path="/tables/list" component={ renderListTables } />
                <Route path="/tables/create" component={() => <CreateTable/>} />
                <Route path="/tables/show/:tableId/stages/show/:stageId/activities/create" render={(props) => <CreateActivity {...props}/>} />
                <Route path="/tables/show/:tableId/stages/show/:stageId/activities/edit/:activityId" render={(props) => <UpdateActivity {...props}/>} />
                <Route path="/tables/show/:tableId/stages/create" render={(props) => <CreateStage {...props}/>} />
                <Route path="/tables/show/:tableId/stages/edit/:stageId" render={(props) => <UpdateStage {...props}/>} />
                <Route path="/tables/show/:tableId" render={(props) =>  <ShowTable {...props}/>} />
                <Route path="/tables/edit/:tableId" render={(props) =>  <UpdateTable {...props}/>} />
                
                <Redirect to="/" />
            </RSwitch>
        );
    }
}

const mapStateToProps = (state) => {
    return { authenticated: state.authenticated };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

Switch = connect(mapStateToProps, mapDispatchToProps)(Switch);


export default Switch;
