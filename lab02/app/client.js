const app = require('express')();
const http = require('http').Server(app);
var path = require('path');
var io = require('socket.io');

options = {
    secure: true,
    reconnect: true,
    rejectUnauthorized: false
}

app.get('/log', (req, res) => {
    socket.emit('log', 'send from get');
    res.send('<h1>send</h1>');
});

var io2 = require('socket.io-client');
var socket = io2.connect('http://localhost:8084', options);

var msg = "c = 37";
socket.emit('log', msg);

http.listen(8085, () => {
    console.log("Listening on localhost:8085");
})