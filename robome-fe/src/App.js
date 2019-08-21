import React, { Component } from 'react';
import { BrowserRouter } from 'react-router-dom';
import { Provider } from 'react-redux';
import storage from 'redux-persist/lib/storage'
import { persistStore, persistReducer } from 'redux-persist'
import { PersistGate } from 'redux-persist/integration/react'


import Switch from './components/navigation/Switch';
import UpperMenu from './components/navigation/UpperMenu';

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

            <UpperMenu></UpperMenu>

            <div className="pricing-header px-3 py-3 pt-md-5 pb-md-4 mx-auto text-center">
              <h1 className="display-4">qwerty</h1>
              <p className="lead">lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum lorem ipsum </p>
            </div>

            <div className="container">

              <div className="App">
                <main role="main" className="inner cover">
                  <Switch></Switch>
                </main>
              </div>

              <footer className="pt-4 my-md-5 pt-md-5 border-top">
                <div className="row">
                  <div className="col-12 col-md">
                    <img className="mb-2" src="/docs/4.3/assets/brand/bootstrap-solid.svg" alt="" width="24" height="24"></img>
                    <small className="d-block mb-3 text-muted">&copy; 2017-2019</small>
                  </div>
                </div>
              </footer>

            </div>

          </BrowserRouter>
        </PersistGate>
      </Provider>
    );
  }
}

export default App;
