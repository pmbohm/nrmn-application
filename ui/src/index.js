import React from 'react';
import ReactDOM from 'react-dom';
import './index.css';
import App from './App';
import * as serviceWorker from './serviceWorker';
import {Provider} from 'react-redux';
import store from './components/store';
import {apiDefinition} from './axios/api';
import config from 'react-global-configuration';

window.setApplicationError = (message, error) => {
  if (message) {
    document.write(`<h1>National Reef Monitoring Network</h1>`);
    document.write(`<p>The server may be experiencing problems. Please wait a moment and try again.<br>`);
    document.write(`If this problem persists, please contact info@aodn.org.au.</p>`);
    document.write(`<b>Error: ${message}</b><br>`);
    document.write('<hr><small>');
    document.write(JSON.stringify(error));
  }
};

apiDefinition().then((result) => {
  config.set({api: result.data.components.schemas});
  ReactDOM.render(
    <React.StrictMode>
      <Provider store={store}>
        <App />
      </Provider>
    </React.StrictMode>,
    document.getElementById('root')
  );
});

// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
