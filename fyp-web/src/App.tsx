import React from 'react';
import './App.css';

const logo = require('./logo_white.png');
const poster = require('./20093427_fyp_poster_small.jpg');
const report = require('./20093427_dylanfennelly_fyp_final_report.pdf')

function App() {
  return (
    <div className="App">
      <header className="App-header">
        {/* <img src={logo} className="App-logo" alt="logo" /> */}
        <h1>
          Dylan Fennelly
        </h1>
        <h5>BSc (Hons) in Applied Computing (Cloud & Networks) </h5>
        <h3>Final Year Project 2024</h3>

        <img src={String(logo)} alt='Logo'/>
        <h1 className='App-Name'>
          Q-Up
        </h1>
        <h2>
        Your Queuing Companion
        </h2>
        
      </header>
      <body className='App-body'>
        <h3>About</h3>
        <p>
        Q-Up is a virtual queuing system for attraction-based facilities that enables visitors to wait
        for an attraction in a 'virtual queue' rather than a physical queue. Using the app, a visitor can view
        details of attractions in a facility, with queue-time estimates. The user can enter a virtual queue for
        an attraction and enjoy other attractions while still in the queue for the first attraction. 
        <br/><br/>
        When it is time is to enter the attraction, the user's current distance from it is taken into account, and the notification sent accordingly early. This system allows visitors to queue for the attractions they want to experience while still being able to enjoy the rest of the facility, and acts to redirect traffic to attractions with shorter queues, better distributing visitors throughout the facility.
        </p>

        <h3>Project Poster</h3>
        <a
          className="App-link"
          href={String(poster)}
          target="_blank"
          rel="noopener noreferrer"
        >
          <img className="Poster" src={String(poster)} alt='Logo'/>
        </a>
        <h3>Demo Video</h3>
        <a
          className="App-link"
          href="https://www.youtube.com/watch?v=Q6tVN1AfIZ8"
          target="_blank"
          rel="noopener noreferrer"
        >
          <h5>YouTube Link</h5>
        </a>

        <h3>Project Report</h3>
        <a
          className="App-link"
          href={String(report)}
          target="_blank"
          rel="noopener noreferrer"
        >
          <h5>Download Link</h5>
        </a>

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
