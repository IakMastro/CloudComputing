const app = require('express')();
const http = require('http').Server(app);
var path = require('path');
var io = require('socket.io')(http);

app.get('/', (req, res) => {
    res.send('<h1>Hello World!</h1>');
});

app.get('/users', (req, res) => {
    res.send('<h1>user: user1, user2</h1>');
});

io.on('connection', s => {
    console.error('socket connection');

    s.on('log', (data, room) => {
        console.log('broadcast', data);
    })
})

http.listen(8084, () => {
    console.log('Listening on localhost:8084');
});