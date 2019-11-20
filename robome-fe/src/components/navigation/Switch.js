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


import { listTablesUrl, loginUrl, logoutUrl, registerUrl, 
    healthUrl, createTableUrl, createActivityUrl,
    editActivityUrl, createStageUrl, editStageUrl, 
    editTableUrl, showTableUrl } from '../../web/url';

class Switch extends Component {

    static propTypes = {
        authToken: PropTypes.string
    };

    render() {

        const authenticated = this.props.authToken;
        const renderListTables = authenticated ? () => <ListTables/> : () => <Redirect to='/login' />; 

        return (
            <RSwitch>
                <Route exact path="/" component={Home} />
                <Route path={loginUrl()} component={() => <Login/>} />
                <Route path={logoutUrl()} component={() => <Logout/>} />
                <Route path={registerUrl()} component={() => <Register/>} />
                <Route path={healthUrl()} component={() => <Health/>} />
                <Route path={listTablesUrl()} component={ renderListTables } />
                <Route path={createTableUrl()} component={() => <CreateTable/>} />
                <Route path={createActivityUrl(":tableId", ":stageId")} render={(props) => <CreateActivity {...props}/>} />
                <Route path={editActivityUrl(":tableId", ":stageId", ":activityId")} render={(props) => <UpdateActivity {...props}/>} />
                <Route path={createStageUrl(":tableId")} render={(props) => <CreateStage {...props}/>} />
                <Route path={editStageUrl(":tableId", ":stageId")} render={(props) => <UpdateStage {...props}/>} />
                <Route path={showTableUrl(":tableId")} render={(props) =>  <ShowTable {...props}/>} />
                <Route path={editTableUrl(":tableId")} render={(props) =>  <UpdateTable {...props}/>} />
                
                <Redirect to="/" />
            </RSwitch>
        );
    }
}

const mapStateToProps = (state) => {
    return { authToken: state.authToken };
};

const mapDispatchToProps = (dispatch) => {
    return {}
};

Switch = connect(mapStateToProps, mapDispatchToProps)(Switch);

export default Switch;
