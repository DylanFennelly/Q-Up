import React from 'react';
import logo from './logo.svg';
import './App.css';

function App() {
  return (
    <div className="App">
      <header className="App-header">
        {/* <img src={logo} className="App-logo" alt="logo" /> */}
        <h1>
          Dylan Fennelly
        </h1>
        <h4>BSc (Hons) in Applied Computing (Cloud & Networks) </h4>
        <h2>Final Year Project 2024</h2>
        
      </header>
      <body className='App-body'>
        <h3>Project Poster</h3>
        {/* <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        > */}
          <h5>T.B.A.</h5>
        {/* </a> */}
        <h3>Demo Video</h3>
        {/* <a
          className="App-link"
          href="https://reactjs.org"
          target="_blank"
          rel="noopener noreferrer"
        > */}
          <h5>T.B.A.</h5>
        {/* </a> */}
        <h3>Project Code Repository</h3>
        <a
          className="App-link"
          href="https://github.com/DylanFennelly/Q-Up"
          target="_blank"
          rel="noopener noreferrer"
        >
          <h5>GitHub</h5>
        </a>
      </body>
    </div>
  );
}

export default App;
