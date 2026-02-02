const WebSocket = require('ws');

const wss = new WebSocket.Server({ port: 3000 });

const rooms = {}; // Fiecare cameră are participanții săi

wss.on('connection', (ws) => {
  ws.on('message', (message) => {
    const data = JSON.parse(message);

    switch (data.type) {
      case 'join':
        const room = data.room || 'default';
        if (!rooms[room]) rooms[room] = [];
        rooms[room].push(ws);
        ws.room = room;
        console.log(`User joined room: ${room}`);
        break;

      case 'offer':
      case 'answer':
      case 'candidate':
        // Trimite mesajul la toți ceilalți participanți din cameră
        const participants = rooms[ws.room] || [];
        participants.forEach((client) => {
          if (client !== ws && client.readyState === WebSocket.OPEN) {
            client.send(JSON.stringify(data));
          }
        });
        break;
    }
  });

  ws.on('close', () => {
    // Elimină utilizatorul din cameră
    const room = ws.room;
    if (rooms[room]) {
      rooms[room] = rooms[room].filter((client) => client !== ws);
      if (rooms[room].length === 0) {
        delete rooms[room];
      }
    }
  });
});

console.log('Signaling server running on ws://localhost:3000');
