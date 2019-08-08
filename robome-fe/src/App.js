import React, { Component } from 'react';
import { BrowserRouter, Route, Redirect, Link, Switch } from 'react-router-dom';
import { Provider } from 'react-redux';
import storage from 'redux-persist/lib/storage'
import { persistStore, persistReducer } from 'redux-persist'
import { PersistGate } from 'redux-persist/integration/react'


import Home from './Home';
import Health from './Health';
import Login from './components/auth/Login';
import Register from './components/auth/Register';

import CreateTable from './components/table/Create';
import ListTables from './components/table/List';
import ShowTable from './components/table/Show';

import { createStore } from 'redux';

import Reducer from './Reducer'; 

import './App.css';

const persistConfig = {
  key: 'root',
  storage,
}

const persistedReducer = persistReducer(persistConfig, Reducer)




const store = createStore(persistedReducer, { jwtToken: "" });
const persistor = persistStore(store)

class App extends Component {

  render() {

    return (
      <Provider store={store}>
        <PersistGate loading={null} persistor={persistor}>
          <BrowserRouter>
            <div className="App">

              <header>
                <ul>
                  <li><Link to="/login/">Login</Link></li>
                  <li><Link to="/register/">Register</Link></li>
                  <li><Link to="/health/">Health</Link></li>
                  <li><Link to="/tables/list/">List tables</Link></li>
                </ul>
              </header>

              <main role="main" className="inner cover">
                <Switch>
                  <Route exact path="/" component={Home} />
                  <Route path="/login" component={() => <Login/>} />
                  <Route path="/register" component={() => <Register/>} />
                  <Route path="/health" component={() => <Health/>} />
                  <Route path="/tables/list" component={() => <ListTables/>} />
                  <Route path="/tables/create" component={() => <CreateTable/>} />
                  <Route path="/tables/show/:tableId" render={(props) =>  <ShowTable {...props}/>} />
                  <Redirect to="/" />
                </Switch>
              </main>

            </div>
          </BrowserRouter>
        </PersistGate>
      </Provider>
    );
  }
}

export default App;
